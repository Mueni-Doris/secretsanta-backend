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
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String name = trimToNull(body.get("name"));
        String budget = trimToNull(body.get("budget"));
        String organizerEmail = normalizeEmail(body.get("organizerEmail"));
        String organizerPassword = body.get("organizerPassword");

        if (name == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Event name is required"));
        }
        if (budget == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Budget is required"));
        }
        if (organizerEmail == null || !organizerEmail.contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid organizer email is required"));
        }
        if (organizerPassword == null || organizerPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Organizer password must be at least 6 characters"));
        }

        // Build event
        Event event = new Event();
        event.setName(name);
        event.setDrawDate(body.get("drawDate"));
        event.setBudget(budget);
        event.setCurrency(body.getOrDefault("currency", "KES"));
        event.setRules(body.get("rules"));
        event.setOrganizerEmail(organizerEmail);
        event.setStatus("active");
        Event saved = eventRepo.save(event);

        // Auto-add organizer as first participant
        String organizerName = trimToNull(body.get("organizerName"));
        if (organizerName == null) {
            organizerName = organizerEmail.split("@")[0];
        }

        Participant organizer = new Participant();
        organizer.setName(organizerName);
        organizer.setEmail(organizerEmail);
        organizer.setStatus("Joined");
        organizer.setWishlistStatus("Pending");
        organizer.setAvatarColor("#c8453a");
        organizer.setHasSpun(false);
        organizer.setEventId(saved.getId());

        organizer.setPasswordHash(
                org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
                        organizerPassword,
                        org.springframework.security.crypto.bcrypt.BCrypt.gensalt()
                )
        );

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

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
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
