package com.secretsanta.repository;

import com.secretsanta.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByOrganizerEmail(String organizerEmail);

    boolean existsByOrganizerEmail(String organizerEmail);
}