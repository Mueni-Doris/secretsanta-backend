package com.secretsanta.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "draw_date")
    private String drawDate;

    @Column(nullable = false)
    private String budget;

    private String currency;

    private String rules;

    @Column(name = "organizer_email")
    private String organizerEmail;

    private String status; // "active", "completed"
}
