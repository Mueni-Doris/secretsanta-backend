package com.secretsanta.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(
        name = "matches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_matches_giver_event_round",
                        columnNames = {"giver_id", "event_id", "round"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "giver_id", nullable = false)
    private Long giverId;

    @Column(name = "giver_name", nullable = false)
    private String giverName;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "avatar_color")
    private String avatarColor;

    @Column(name = "round", nullable = false)
    private int round;

    @Column(name = "event_id")
    private Long eventId;
}
