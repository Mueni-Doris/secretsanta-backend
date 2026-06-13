package com.secretsanta.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(nullable = false)
    private String email;

    // Hashed password — set when participant accepts invite
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private String status;

    @Column(name = "wishlist_status")
    private String wishlistStatus;

    @Column(name = "avatar_color")
    private String avatarColor;

    @Column(name = "invite_token", unique = true)
    private String inviteToken;

    @Column(name = "reset_password_token", unique = true)
    private String resetPasswordToken;

    @Column(name = "reset_password_expires_at")
    private LocalDateTime resetPasswordExpiresAt;

    @Column(name = "has_spun")
    private boolean hasSpun = false;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void normalizeFields() {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}