package com.secretsanta.controller;

import com.secretsanta.model.Event;
import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.repository.MatchRepository;
import com.secretsanta.repository.ParticipantRepository;
import com.secretsanta.service.EmailService;
import com.secretsanta.service.JwtService;
import com.secretsanta.service.RequestAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventRepository eventRepo;
    private final ParticipantRepository participantRepo;
    private final MatchRepository matchRepo;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final RequestAuthService authService;

    public EventController(
            EventRepository eventRepo,
            ParticipantRepository participantRepo,
            MatchRepository matchRepo,
            EmailService emailService,
            JwtService jwtService,
            RequestAuthService authService
    ) {
        this.eventRepo = eventRepo;
        this.participantRepo = participantRepo;
        this.matchRepo = matchRepo;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    // POST /api/events
    // Body: { name, drawDate, budget, currency, rules, organizerEmail, organizerName, organizerPassword }
    // 1. Save event
    // 2. Auto-add organizer as first participant with their password
    // 3. Generate JWT for organizer
    // 4. Send confirmation email
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        // Build event
        Event event = new Event();
        event.setName(body.get("name"));
        event.setDrawDate(body.get("drawDate"));
        event.setBudget(body.get("budget"));
        event.setCurrency(body.getOrDefault("currency", "KES"));
        event.setRules(body.get("rules"));
        String organizerEmail = body.get("organizerEmail") == null
                ? null
                : body.get("organizerEmail").trim().toLowerCase();
        event.setOrganizerEmail(organizerEmail);
        event.setStatus("active");
        Event saved = eventRepo.save(event);

        // Auto-add organizer as first participant
        String organizerName     = body.getOrDefault("organizerName", organizerEmail.split("@")[0]);
        String organizerPassword = body.get("organizerPassword");

        Participant organizer = new Participant();
        organizer.setName(organizerName);
        organizer.setEmail(organizerEmail);
        organizer.setStatus("Joined");
        organizer.setWishlistStatus("Pending");
        organizer.setAvatarColor("#c8453a");
        organizer.setHasSpun(false);
        organizer.setEventId(saved.getId());

        if (organizerPassword != null && organizerPassword.length() >= 6) {
            organizer.setPasswordHash(
                    org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
                            organizerPassword,
                            org.springframework.security.crypto.bcrypt.BCrypt.gensalt()
                    )
            );
        }

        Participant savedOrganizer = participantRepo.save(organizer);

        // Generate JWT for organizer so they're immediately logged in
        String token = jwtService.generateToken(
                savedOrganizer.getId(),
                savedOrganizer.getEmail(),
                saved.getId()
        );

        // Send confirmation email
        try {
            String joinLink = "Use the invite screen to send tokenized participant invite links.";
            emailService.sendEventCreatedConfirmation(
                    saved.getOrganizerEmail(),
                    saved.getName(),
                    saved.getDrawDate(),
                    saved.getBudget(),
                    saved.getCurrency(),
                    joinLink
            );
        } catch (Exception e) {
            log.warn("Event confirmation email failed eventId={}", saved.getId(), e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("event",       saved);
        response.put("token",       token);
        response.put("userId",      savedOrganizer.getId());
        response.put("name",        savedOrganizer.getName());
        response.put("email",       savedOrganizer.getEmail());
        response.put("eventId",     saved.getId());
        response.put("joinLink",    "Use the invite screen to send tokenized participant invite links.");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(HttpServletRequest request) {
        Long eventId = authService.currentEventId(request);
        return eventRepo.findById(eventId)
                .<ResponseEntity<?>>map(event -> ResponseEntity.ok(List.of(event)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        if (!authService.canAccessEvent(request, id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this event"));
        }
        return eventRepo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(
            @RequestParam(required = false) Long eventId,
            HttpServletRequest request
    ) {
        Long scopedEventId = eventId != null ? eventId : authService.currentEventId(request);
        if (!authService.canAccessEvent(request, scopedEventId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this event"));
        }

        List<Participant> participants = participantRepo.findByEventId(scopedEventId);

        long total     = participants.size();
        long wishlists = participants.stream()
                .filter(p -> "Submitted".equals(p.getWishlistStatus()))
                .count();
        long matches = matchRepo.findAll().stream()
                .filter(m -> scopedEventId.equals(m.getEventId()))
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("total",     total);
        result.put("wishlists", wishlists);
        result.put("matches",   matches);

        return ResponseEntity.ok(result);
    }
}
