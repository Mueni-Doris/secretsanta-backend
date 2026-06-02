package com.secretsanta.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InviteService {

    private static final Logger log = LoggerFactory.getLogger(InviteService.class);

    private final JavaMailSender mailSender;

    public InviteService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvite(String toEmail, String eventName, String joinLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("muenidoris22@gmail.com");
        message.setTo(toEmail);
        message.setSubject("🎁 You're invited to " + eventName + "!");
        message.setText(
                "Hi there!\n\n" +
                        "You've been invited to join " + eventName + " — a Secret Santa gift exchange!\n\n" +
                        "Click the link below to join and submit your wishlist:\n" +
                        joinLink + "\n\n" +
                        "Can't wait to see you there! 🎄\n\n" +
                        "— The Secret Santa Team"
        );
        mailSender.send(message);
    }

    public int sendBulkInvites(List<String> emails, String eventName, String joinLink) {
        int successCount = 0;
        for (String email : emails) {
            try {
                sendInvite(email, eventName, joinLink);
                successCount++;
            } catch (Exception e) {
                log.warn("Invite email failed email={}", email, e);
            }
        }
        return successCount;
    }
}
