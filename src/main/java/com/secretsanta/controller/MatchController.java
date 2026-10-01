package com.secretsanta.controller;

import com.secretsanta.model.Match;
import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.model.Event;
import com.secretsanta.repository.MatchRepository;
import com.secretsanta.repository.ParticipantRepository;
import com.secretsanta.service.EmailService;
import com.secretsanta.service.RequestAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

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
            @RequestParam(required = false) Integer round,
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

        int effectiveRound = (round != null) ? round : getCurrentRoundForEvent(scopedEventId);

        Optional<Match> match = matchRepo.findByGiverIdAndRoundAndEventId(tokenUserId, effectiveRound, scopedEventId);

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

        // Guard: no receiver can be assigned more than once per round
        if (matchRepo.existsByReceiverIdAndRoundAndEventId(receiver.getId(), currentRound, eventId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "This participant is already assigned as a receiver this round"));
        }

        // Guard: do not repeat a giver→receiver pair until the event has exhausted all unique pairs.
        if (matchRepo.existsByGiverIdAndReceiverIdAndEventId(giver.getId(), receiver.getId(), eventId)
                && !eventHasExhaustedUniquePairs(eventId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "This participant was already assigned to the same receiver in a previous round"));
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
            @RequestParam(required = false) Integer round,
            HttpServletRequest request
    ) {
        Long scopedEventId = eventId != null ? eventId : authService.currentEventId(request);
        if (!authService.canAccessEvent(request, scopedEventId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden for this event"));
        }

        List<Participant> participants = participantRepo.findByEventId(scopedEventId);
        long total = participants.size();

        int currentRound = round != null ? round : getCurrentRoundForEvent(scopedEventId);
        long spun = matchRepo.countByEventIdAndRound(scopedEventId, currentRound);

        return ResponseEntity.ok(Map.of(
                "round",    currentRound,
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
        // Prefer an explicit round stored on the Event row (nullable). Fall back
        // to computing from match rows if it's not set.
        return eventRepo.findById(eventId)
                .map(event -> {
                    Integer evRound = event.getRound();
                    if (evRound != null) {
                        return evRound;
                    }

                    // fallback: compute from matches
                    List<Match> eventMatches = matchRepo.findByEventId(eventId);
                    if (eventMatches.isEmpty()) {
                        return 1;
                    }

                    int latestRound = eventMatches.stream()
                            .mapToInt(Match::getRound)
                            .max()
                            .orElse(1);

                    long totalParticipants = participantRepo.countByEventId(eventId);
                    long latestRoundMatches = matchRepo.countByEventIdAndRound(eventId, latestRound);

                    // When the latest round is complete, advance to the next round.
                    if (latestRoundMatches >= totalParticipants) {
                        return latestRound + 1;
                    }

                    return latestRound;
                })
                .orElse(1);
    }

    private boolean eventHasExhaustedUniquePairs(Long eventId) {
        List<Participant> eventParticipants = participantRepo.findByEventId(eventId);
        int participantCount = eventParticipants.size();
        if (participantCount < 2) {
            return true;
        }

        List<Match> eventMatches = matchRepo.findByEventId(eventId);
        Set<String> uniquePairs = new HashSet<>();
        for (Match existing : eventMatches) {
            uniquePairs.add(existing.getGiverId() + ":" + existing.getReceiverId());
        }

        long possiblePairs = (long) participantCount * (participantCount - 1);
        return uniquePairs.size() >= possiblePairs;
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
                    log.info("All participants spun eventId={} round={}", eventId, round);

                    // Reset participant `hasSpun` for the next round and update
                    // the event's stored round so the system uses the event row
                    // as the authoritative current round going forward.
                    try {
                        eventParticipants.forEach(p -> p.setHasSpun(false));
                        participantRepo.saveAll(eventParticipants);
                        log.info("Reset hasSpun flags for eventId={} after round={}", eventId, round);

                        event.setRound(round + 1);
                        eventRepo.save(event);
                        log.info("Advanced Event.round to {} for eventId={}", round + 1, eventId);
                    } catch (Exception e) {
                        log.warn("Failed to reset hasSpun flags or update event round for eventId={} after round={}", eventId, round, e);
                    }
                });
            } catch (Exception e) {
                log.warn("All-spun email failed eventId={} round={}", eventId, round, e);
            }
        }
    }
}
