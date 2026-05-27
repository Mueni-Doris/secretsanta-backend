package com.secretsanta.controller;

import com.secretsanta.model.Match;
import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.repository.MatchRepository;
import com.secretsanta.repository.ParticipantRepository;
import com.secretsanta.service.EmailService;
import com.secretsanta.service.RequestAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchRepository matchRepo;
    private final ParticipantRepository participantRepo;
    private final EventRepository eventRepo;
    private final EmailService emailService;
    private final RequestAuthService authService;

    public MatchController(
            MatchRepository matchRepo,
            ParticipantRepository participantRepo,
            EventRepository eventRepo,
            EmailService emailService,
            RequestAuthService authService
    ) {
        this.matchRepo       = matchRepo;
        this.participantRepo = participantRepo;
        this.eventRepo       = eventRepo;
        this.emailService    = emailService;
        this.authService     = authService;
    }

    // GET /api/matches/my?round=2&eventId=1
    // SECRET — only returns THIS user's match for this event
    @GetMapping("/my")
    public ResponseEntity<?> getMyMatch(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "2") int round,
            @RequestParam(required = false) Long eventId,
            HttpServletRequest request
    ) {
        Long tokenUserId = authService.currentUserId(request);
        if (userId != null && !userId.equals(tokenUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this user"));
        }
        Long scopedEventId = eventId != null ? eventId : authService.currentEventId(request);
        if (!authService.canAccessEvent(request, scopedEventId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this event"));
        }

        Optional<Match> match = matchRepo.findByGiverIdAndRoundAndEventId(tokenUserId, round, scopedEventId);

        return match.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/matches
    // Body: { receiverId }
    @PostMapping
    public ResponseEntity<?> saveMatch(@RequestBody Match match, HttpServletRequest request) {
        Long eventId = authService.currentEventId(request);
        Long giverId = authService.currentUserId(request);

        if (eventId == null || giverId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authenticated event and user are required"));
        }

        Participant giver = participantRepo.findById(giverId).orElse(null);
        Participant receiver = match.getReceiverId() == null
                ? null
                : participantRepo.findById(match.getReceiverId()).orElse(null);

        if (giver == null || !eventId.equals(giver.getEventId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this giver"));
        }
        if (receiver == null || !eventId.equals(receiver.getEventId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Receiver must belong to your event"));
        }

        // Guard: no self-matching
        if (giver.getId().equals(receiver.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You cannot be your own Santa!"));
        }

        // Determine current round for this event
        int currentRound = getCurrentRoundForEvent(eventId);
        match.setRound(currentRound);
        match.setEventId(eventId);
        match.setGiverId(giver.getId());
        match.setGiverName(giver.getName());
        match.setReceiverId(receiver.getId());
        match.setReceiverName(receiver.getName());
        match.setAvatarColor(receiver.getAvatarColor());

        // Guard: no spinning twice in same round for same event
        if (matchRepo.existsByGiverIdAndRoundAndEventId(giver.getId(), currentRound, eventId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You have already spun this round"));
        }

        Match saved;
        try {
            saved = matchRepo.save(match);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(Map.of("error", "You have already spun this round"));
        }

        // Mark participant as having spun
        giver.setHasSpun(true);
        participantRepo.save(giver);

        // Check if all participants in this event have spun
        checkIfAllSpun(eventId, currentRound);

        return ResponseEntity.ok(saved);
    }

    // GET /api/matches/status?eventId=1
    @GetMapping("/status")
    public ResponseEntity<?> status(
            @RequestParam(required = false) Long eventId,
            HttpServletRequest request
    ) {
        Long scopedEventId = eventId != null ? eventId : authService.currentEventId(request);
        if (!authService.canAccessEvent(request, scopedEventId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this event"));
        }

        List<Participant> participants = participantRepo.findByEventId(scopedEventId);

        long total = participants.size();
        long spun  = participants.stream()
                .filter(Participant::isHasSpun)
                .count();

        return ResponseEntity.ok(Map.of(
                "total",    total,
                "spun",     spun,
                "complete", spun >= total
        ));
    }

    // GET /api/matches/round/{round}?eventId=1
    @GetMapping("/round/{round}")
    public ResponseEntity<?> getRound(
            @PathVariable int round,
            @RequestParam(required = false) Long eventId,
            HttpServletRequest request
    ) {
        Long scopedEventId = eventId != null ? eventId : authService.currentEventId(request);
        if (!authService.isEventAdmin(request, scopedEventId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        List<Match> matches = matchRepo.findByRoundAndEventId(round, scopedEventId);

        return ResponseEntity.ok(matches);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private int getCurrentRoundForEvent(Long eventId) {
        List<Match> eventMatches = matchRepo.findAll().stream()
                .filter(m -> eventId.equals(m.getEventId()))
                .toList();
        if (eventMatches.isEmpty()) return 1;
        return eventMatches.stream()
                .mapToInt(Match::getRound)
                .max()
                .orElse(1);
    }

    private void checkIfAllSpun(Long eventId, int round) {
        List<Participant> eventParticipants = participantRepo.findByEventId(eventId);
        List<Match> roundMatches = matchRepo.findByRoundAndEventId(round, eventId);

        if (roundMatches.size() >= eventParticipants.size()) {
            try {
                eventRepo.findById(eventId).ifPresent(event -> {
                    emailService.sendAllSpunNotification(
                            event.getOrganizerEmail(),
                            event.getName(),
                            round
                    );
                    System.out.println("All spun for event " + eventId + " round " + round);
                });
            } catch (Exception e) {
                System.err.println("Could not send all-spun email: " + e.getMessage());
            }
        }
    }
}
