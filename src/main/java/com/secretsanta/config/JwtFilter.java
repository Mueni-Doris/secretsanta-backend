package com.secretsanta.config;

import com.secretsanta.model.Participant;
import com.secretsanta.repository.ParticipantRepository;
import com.secretsanta.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ParticipantRepository participantRepo;

    public JwtFilter(JwtService jwtService, ParticipantRepository participantRepo) {
        this.jwtService = jwtService;
        this.participantRepo = participantRepo;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (request.getMethod().equals("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        // Public endpoints: creating an event, login, and accepting an invite.
        if ((path.equals("/api/events") && request.getMethod().equals("POST")) ||
                path.equals("/api/auth/login") ||
                path.equals("/api/auth/accept-invite") ||
                path.equals("/api/auth/forgot-password") ||
                path.equals("/api/auth/reset-password")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"No token provided\"}");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token expired or invalid\"}");
            return;
        }

        Long userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);
        Long eventId = jwtService.extractEventId(token);

        Participant participant = participantRepo.findById(userId).orElse(null);
        if (participant == null ||
                !email.equalsIgnoreCase(participant.getEmail()) ||
                !eventId.equals(participant.getEventId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token user no longer valid\"}");
            return;
        }

        request.setAttribute("userId", userId);
        request.setAttribute("email", email);
        request.setAttribute("eventId", eventId);

        chain.doFilter(request, response);
    }
}
