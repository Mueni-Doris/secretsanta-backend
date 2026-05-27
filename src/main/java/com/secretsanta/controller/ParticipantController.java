package com.secretsanta.controller;

import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.repository.ParticipantRepository;
import com.secretsanta.service.EmailService;
import com.secretsanta.service.RateLimitService;
import com.secretsanta.service.RequestAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantRepository repo;
    private final EventRepository eventRepo;
    private final EmailService emailService;
    private final RequestAuthService authService;
    private final RateLimitService rateLimitService;

    public ParticipantController(
            ParticipantRepository repo,
            EventRepository eventRepo,
            EmailService emailService,
            RequestAuthService authService,
            RateLimitService rateLimitService
    ) {
        this.repo = repo;
        this.eventRepo = eventRepo;
        this.emailService = emailService;
        this.authService = authService;
        this.rateLimitService = rateLimitService;
    }

    // GET /api/participants?eventId=1
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Long eventId,
            HttpServletRequest request
    ) {
        Long scopedEventId = resolveEventId(eventId, request);
        if (scopedEventId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this event"));
        }

        boolean admin = authService.isEventAdmin(request, scopedEventId);
        List<Participant> list = repo.findByEventId(scopedEventId);
        return ResponseEntity.ok(list.stream().map(p -> toSafeMap(p, admin)).toList());
    }

    // GET /api/participants/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        return repo.findById(id)
                .map(p -> {
                    if (!authService.canAccessEvent(request, p.getEventId())) {
                        return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this participant"));
                    }
                    boolean admin = authService.isEventAdmin(request, p.getEventId());
                    return ResponseEntity.ok(toSafeMap(p, admin));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/participants/remind?eventId=1
    @PostMapping("/remind")
    public ResponseEntity<?> remind(
            @RequestParam(required = false) Long eventId,
            HttpServletRequest request
    ) {
        Long scopedEventId = resolveEventId(eventId, request);
        if (scopedEventId == null || !authService.isEventAdmin(request, scopedEventId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        if (!rateLimitService.allow("remind", request.getRemoteAddr() + ":" + scopedEventId, 5, 60 * 60 * 1000)) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many reminder requests"));
        }

        List<Participant> all = repo.findByEventId(scopedEventId);

        List<Participant> pending = all.stream()
                .filter(p -> "Pending".equals(p.getWishlistStatus()))
                .toList();

        pending.forEach(p -> {
            try {
                emailService.sendReminder(p.getEmail(), p.getName());
            } catch (Exception e) {
                System.err.println("Failed: " + e.getMessage());
            }
        });

        return ResponseEntity.ok(Map.of(
                "message", "Reminders sent to " + pending.size() + " participants"
        ));
    }

    // GET /api/participants/stats?eventId=1
    @GetMapping("/stats")
    public ResponseEntity<?> stats(
            @RequestParam(required = false) Long eventId,
            HttpServletRequest request
    ) {
        Long scopedEventId = resolveEventId(eventId, request);
        if (scopedEventId == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this event"));
        }

        List<Participant> all = repo.findByEventId(scopedEventId);

        Map<String, Long> result = new HashMap<>();
        result.put("total",     (long) all.size());
        result.put("wishlists", all.stream().filter(p -> "Submitted".equals(p.getWishlistStatus())).count());
        result.put("matches",   all.stream().filter(Participant::isHasSpun).count());

        return ResponseEntity.ok(result);
    }

    private Long resolveEventId(Long requestedEventId, HttpServletRequest request) {
        Long tokenEventId = authService.currentEventId(request);
        if (requestedEventId == null) {
            return tokenEventId;
        }
        return requestedEventId.equals(tokenEventId) ? requestedEventId : null;
    }

    private Map<String, Object> toSafeMap(Participant p, boolean includeEmail) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",             p.getId());
        map.put("name",           p.getName());
        map.put("nickname",       p.getNickname());
        map.put("status",         p.getStatus());
        map.put("wishlistStatus", p.getWishlistStatus());
        map.put("avatarColor",    p.getAvatarColor());
        map.put("eventId",        p.getEventId());
        map.put("createdAt",      p.getCreatedAt());
        if (includeEmail) {
            map.put("email", p.getEmail());
        }
        return map;
    }
}
