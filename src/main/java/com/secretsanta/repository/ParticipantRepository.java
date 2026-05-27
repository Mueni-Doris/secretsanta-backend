package com.secretsanta.repository;

import com.secretsanta.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEventId(Long eventId);
    Optional<Participant> findByEmailAndEventId(String email, Long eventId);
    Optional<Participant> findByEmailIgnoreCaseAndEventId(String email, Long eventId);
    Optional<Participant> findByEmail(String email);
    Optional<Participant> findByEmailIgnoreCase(String email);
    List<Participant> findAllByEmailIgnoreCase(String email);
    Optional<Participant> findByInviteToken(String inviteToken);
    Optional<Participant> findByResetPasswordToken(String resetPasswordToken);
    boolean existsByEmailAndEventId(String email, Long eventId);
    boolean existsByEmail(String email);
}
