package com.secretsanta.controller;

import com.secretsanta.model.Event;
import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.repository.ParticipantRepository;
import com.secretsanta.service.InviteService;
import com.secretsanta.service.RateLimitService;
import com.secretsanta.service.RequestAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;
    private final ParticipantRepository participantRepo;
    private final EventRepository eventRepo;
    private final RequestAuthService authService;
    private final RateLimitService rateLimitService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public InviteController(
            InviteService inviteService,
            ParticipantRepository participantRepo,
            EventRepository eventRepo,
            RequestAuthService authService,
            RateLimitService rateLimitService
    ) {
        this.inviteService = inviteService;
        this.participantRepo = participantRepo;
        this.eventRepo = eventRepo;
        this.authService = authService;
        this.rateLimitService = rateLimitService;
    }

    // ── POST /api/invites/send ────────────────────────────────────────────
    // Called by: InviteParticipants.jsx → handleSend()
    // Body: { emails: [...], eventId: 1 }
    // Returns: { sent, message }
    @PostMapping("/send")
    public ResponseEntity<?> sendInvites(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        @SuppressWarnings("unchecked")
        List<String> emails   = (List<String>) body.get("emails");
        Long eventId = body.get("eventId") instanceof Number
                ? ((Number) body.get("eventId")).longValue()
                : authService.currentEventId(request);

        if (emails == null || emails.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No emails provided"));
        }
        if (!authService.isEventAdmin(request, eventId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        if (!rateLimitService.allow("send-invites", request.getRemoteAddr() + ":" + eventId, 5, 60 * 60 * 1000)) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many invite requests"));
        }

        Event event = eventRepo.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Event not found"));
        }

        int sent = 0;
        for (String rawEmail : emails) {
            String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
            if (email.isBlank()) {
                continue;
            }

            Participant participant = participantRepo.findByEmailIgnoreCaseAndEventId(email, eventId)
                    .orElseGet(() -> {
                        Participant p = new Participant();
                        p.setEmail(email);
                        p.setName(email.split("@")[0]);
                        p.setEventId(eventId);
                        p.setStatus("Invited");
                        p.setWishlistStatus("Pending");
                        p.setAvatarColor("#c8453a");
                        p.setHasSpun(false);
                        return p;
                    });
            participant.setInviteToken(generateInviteToken());
            participantRepo.save(participant);

            String joinLink = frontendBaseUrl + "/accept-invite?token=" + participant.getInviteToken();
            try {
                inviteService.sendInvite(email, event.getName(), joinLink);
                sent++;
            } catch (Exception e) {
                System.err.println("Failed to send invite to " + email + ": " + e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "sent",    sent,
                "total",   emails.size(),
                "message", "Successfully sent " + sent + " of " + emails.size() + " invites"
        ));
    }

    private String generateInviteToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
