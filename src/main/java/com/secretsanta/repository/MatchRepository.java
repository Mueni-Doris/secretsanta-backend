package com.secretsanta.repository;

import com.secretsanta.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByGiverIdAndRoundAndEventId(Long giverId, int round, Long eventId);
    List<Match> findByRoundAndEventId(int round, Long eventId);
    boolean existsByGiverIdAndRoundAndEventId(Long giverId, int round, Long eventId);
    List<Match> findByGiverId(Long giverId);
    Optional<Match> findByGiverIdAndRound(Long giverId, int round);
    List<Match> findByRound(int round);
    boolean existsByGiverIdAndRound(Long giverId, int round);
}