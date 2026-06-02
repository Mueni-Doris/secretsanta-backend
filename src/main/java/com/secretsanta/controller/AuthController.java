package com.secretsanta.controller;

import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.repository.ParticipantRepository;
import com.secretsanta.service.EmailService;
import com.secretsanta.service.JwtService;
import com.secretsanta.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final ParticipantRepository participantRepo;
    private final EventRepository eventRepo;
    private final JwtService jwtService;
    private final RateLimitService rateLimitService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public AuthController(
            ParticipantRepository participantRepo,
            EventRepository eventRepo,
            JwtService jwtService,
            RateLimitService rateLimitService,
            EmailService emailService
    ) {
        this.participantRepo = participantRepo;
        this.eventRepo = eventRepo;
        this.jwtService = jwtService;
        this.rateLimitService = rateLimitService;
        this.emailService = emailService;
    }

    // =========================
    // ACCEPT INVITE
    // =========================
    @PostMapping("/accept-invite")
    public ResponseEntity<?> acceptInvite(@RequestBody Map<String, String> body, HttpServletRequest request) {

        String inviteToken = body.get("inviteToken");
        String password = body.get("password");
        String name = body.get("name");

        if (!rateLimitService.allow("accept-invite", request.getRemoteAddr(), 10, 60 * 60 * 1000)) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many attempts"));
        }

        if (inviteToken == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        Participant participant = participantRepo.findByInviteToken(inviteToken)
                .orElse(null);

        if (participant == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid invite token"));
        }

        participant.setName(name != null ? name : participant.getName());

        participant.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        participant.setStatus("Joined");
        participant.setInviteToken(null);

        Participant saved = participantRepo.save(participant);

        String token = jwtService.generateToken(
                saved.getId(),
                saved.getEmail(),
                saved.getEventId()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", saved.getId(),
                "name", saved.getName(),
                "email", saved.getEmail(),
                "eventId", saved.getEventId(),
                "avatarColor", saved.getAvatarColor() != null ? saved.getAvatarColor() : "#c8453a"
        ));
    }

    // =========================
    // LOGIN (FIXED 500 CRASH)
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {

        String email = body.get("email");
        String password = body.get("password");

        if (!rateLimitService.allow("login", request.getRemoteAddr() + ":" + email, 10, 15 * 60 * 1000)) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many attempts"));
        }

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        List<Participant> candidates = participantRepo.findAllByEmailIgnoreCase(email.trim());

        if (candidates.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        Participant participant = null;
        for (Participant candidate : candidates) {
            String hash = candidate.getPasswordHash();
            if (hash == null || hash.isBlank()) {
                continue;
            }
            if (!hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
                continue;
            }
            if (BCrypt.checkpw(password, hash)) {
                participant = candidate;
                break;
            }
        }

        if (participant == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtService.generateToken(
                participant.getId(),
                participant.getEmail(),
                participant.getEventId()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", participant.getId(),
                "name", participant.getName(),
                "email", participant.getEmail(),
                "eventId", participant.getEventId(),
                "avatarColor", participant.getAvatarColor() != null ? participant.getAvatarColor() : "#c8453a"
        ));
    }

    // =========================
    // FORGOT PASSWORD
    // =========================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email required"));
        }

        List<Participant> participants = participantRepo.findAllByEmailIgnoreCase(email.trim());

        for (Participant p : participants) {

            String token = generateSecureToken();

            p.setResetPasswordToken(token);
            p.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));

            participantRepo.save(p);

            String resetLink = frontendBaseUrl + "/reset-password?token=" + token;

            try {
                emailService.sendPasswordReset(p.getEmail(), p.getName(), resetLink);
            } catch (Exception e) {
                log.warn("Password reset email failed participantId={}", p.getId(), e);
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "If email exists, reset link sent"
        ));
    }

    // =========================
    // RESET PASSWORD (HARDENED)
    // =========================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {

        String token = body.get("token");
        String password = body.get("password");

        if (!rateLimitService.allow("reset-password", request.getRemoteAddr(), 5, 60 * 60 * 1000)) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many attempts"));
        }

        if (token == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        Participant participant = participantRepo.findByResetPasswordToken(token).orElse(null);

        if (participant == null ||
                participant.getResetPasswordExpiresAt() == null ||
                participant.getResetPasswordExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }

        participant.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        participant.setResetPasswordToken(null);
        participant.setResetPasswordExpiresAt(null);

        participantRepo.save(participant);

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    // =========================
    // ME (SAFE)
    // =========================
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "No token"));
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        Long userId = jwtService.extractUserId(token);

        return participantRepo.findById(userId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "userId", p.getId(),
                        "name", p.getName(),
                        "email", p.getEmail(),
                        "eventId", p.getEventId(),
                        "avatarColor", p.getAvatarColor() != null ? p.getAvatarColor() : "#c8453a"
                )))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "User not found")));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
