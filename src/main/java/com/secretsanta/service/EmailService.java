package com.secretsanta.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── 1. Admin: Event Created ───────────────────────────────────────────
    // Triggered: when admin submits CreateEvent form
    // Sent to:   organizer email
    public void sendEventCreatedConfirmation(
            String adminEmail,
            String eventName,
            String drawDate,
            String budget,
            String currency,
            String joinLink
    ) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("muenidoris22@gmail.com");
        msg.setTo(adminEmail);
        msg.setSubject("🎄 Your Secret Santa event is live — " + eventName);
        msg.setText(
                "Hi there!\n\n" +
                        "Your Secret Santa event has been created successfully!\n\n" +
                        "── Event Details ──────────────────────\n" +
                        "Name:       " + eventName + "\n" +
                        "Draw Date:  " + drawDate + "\n" +
                        "Budget:     " + currency + " " + budget + "\n\n" +
                        "── Invite Link ────────────────────────\n" +
                        joinLink + "\n\n" +
                        "Share this link with your participants so they can join.\n\n" +
                        "🎁 The Secret Santa Team"
        );
        mailSender.send(msg);
    }

    // ── 2. Admin: All Participants Have Joined ────────────────────────────
    // Triggered: when the last participant joins via /join
    // Sent to:   organizer email
    public void sendAllJoinedNotification(
            String adminEmail,
            String eventName,
            int participantCount
    ) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("muenidoris22@gmail.com");
        msg.setTo(adminEmail);
        msg.setSubject("✅ All " + participantCount + " participants have joined — " + eventName);
        msg.setText(
                "Great news!\n\n" +
                        "All " + participantCount + " participants have joined " + eventName + ".\n\n" +
                        "Everyone is now ready to spin the wheel on draw day!\n\n" +
                        "Log in to your dashboard to check the status:\n" +
                        "http://localhost:5173/dashboard\n\n" +
                        "🎄 The Secret Santa Team"
        );
        mailSender.send(msg);
    }

    // ── 3. Admin: All Participants Have Spun ─────────────────────────────
    // Triggered: when the last person spins the wheel
    // Sent to:   organizer email
    public void sendAllSpunNotification(
            String adminEmail,
            String eventName,
            int round
    ) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("muenidoris22@gmail.com");
        msg.setTo(adminEmail);
        msg.setSubject("🎉 Round " + round + " complete! Everyone has spun — " + eventName);
        msg.setText(
                "The draw is complete!\n\n" +
                        "All participants have spun the wheel for Round " + round + " of " + eventName + ".\n\n" +
                        "Every participant now knows their secret match.\n" +
                        "The gifting can begin! 🎁\n\n" +
                        "View the full results on your dashboard:\n" +
                        "http://localhost:5173/dashboard\n\n" +
                        "🎄 The Secret Santa Team"
        );
        mailSender.send(msg);
    }

    // ── 4. Participant: Wishlist Reminder ─────────────────────────────────
    // Triggered: admin clicks "Send Reminder" on dashboard
    // Sent to:   participant email
    public void sendReminder(String toEmail, String name) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("muenidoris22@gmail.com");
        msg.setTo(toEmail);
        msg.setSubject("🎁 Secret Santa Reminder!");
        msg.setText(
                "Hi " + name + "!\n\n" +
                        "Just a reminder — the Secret Santa draw is coming up soon.\n" +
                        "Make sure you've submitted your wishlist!\n\n" +
                        "🎄 The Secret Santa Team"
        );
        mailSender.send(msg);
    }

    // ── 5. Participant: Invite ────────────────────────────────────────────
    // Triggered: admin sends invites from InviteParticipants page
    // Sent to:   each invited email
    public void sendInvite(String toEmail, String eventName, String joinLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("muenidoris22@gmail.com");
        msg.setTo(toEmail);
        msg.setSubject("🎁 You're invited to " + eventName + "!");
        msg.setText(
                "Hi there!\n\n" +
                        "You've been invited to join " + eventName + " — a Secret Santa gift exchange!\n\n" +
                        "Click the link below to join and submit your wishlist:\n" +
                        joinLink + "\n\n" +
                        "Can't wait to see you there! 🎄\n\n" +
                        "— The Secret Santa Team"
        );
        mailSender.send(msg);
    }

    // ── 6. Participant: Password Reset ───────────────────────────────────
    // Triggered: user requests a password reset from the login page
    public void sendPasswordReset(String toEmail, String name, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("muenidoris22@gmail.com");
        msg.setTo(toEmail);
        msg.setSubject("Reset your Secret Santa password");
        msg.setText(
                "Hi " + name + ",\n\n" +
                        "We received a request to reset your Secret Santa password.\n\n" +
                        "Use this link to set a new password:\n" +
                        resetLink + "\n\n" +
                        "This link expires in 1 hour. If you did not request this, you can ignore this email.\n\n" +
                        "The Secret Santa Team"
        );
        mailSender.send(msg);
    }
}
