package com.secretsanta.service;

import com.secretsanta.model.Event;
import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.repository.ParticipantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RequestAuthService {

    private final ParticipantRepository participantRepo;
    private final EventRepository eventRepo;

    public RequestAuthService(ParticipantRepository participantRepo, EventRepository eventRepo) {
        this.participantRepo = participantRepo;
        this.eventRepo = eventRepo;
    }

    public Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    public Long currentEventId(HttpServletRequest request) {
        return (Long) request.getAttribute("eventId");
    }

    public String currentEmail(HttpServletRequest request) {
        return (String) request.getAttribute("email");
    }

    public Optional<Participant> currentParticipant(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Optional.empty();
        }
        return participantRepo.findById(userId);
    }

    public boolean canAccessEvent(HttpServletRequest request, Long eventId) {
        Long tokenEventId = currentEventId(request);
        return eventId != null && eventId.equals(tokenEventId);
    }

    public boolean isEventAdmin(HttpServletRequest request, Long eventId) {
        String email = currentEmail(request);
        if (email == null || !canAccessEvent(request, eventId)) {
            return false;
        }
        return eventRepo.findById(eventId)
                .map(Event::getOrganizerEmail)
                .map(email::equalsIgnoreCase)
                .orElse(false);
    }
}
