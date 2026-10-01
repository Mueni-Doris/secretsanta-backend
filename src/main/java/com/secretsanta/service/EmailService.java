package com.secretsanta.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Actual frontend URL used for clickable app links
    @Value("${app.frontend-base-url}")
    private String frontendUrl;

    // Public URL where email images are hosted
    @Value("${app.email-image-base-url}")
    private String emailImageBaseUrl;

    private static final String FROM_EMAIL = "muenidoris04@gmail.com";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    // ─────────────────────────────────────────────────────────────
    // Generic HTML Email Sender
    // ─────────────────────────────────────────────────────────────

    private void sendHtmlEmail(
            String to,
            String subject,
            String htmlContent
    ) {
        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            // true = render email as HTML
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send email to " + to,
                    e
            );
        }
    }


    // ─────────────────────────────────────────────────────────────
    // 1. Organizer: Event Created
    // ─────────────────────────────────────────────────────────────

    public void sendEventCreatedConfirmation(
            String adminEmail,
            String eventName,
            String drawDate,
            String budget,
            String currency,
            String joinLink
    ) {

        String eventImageUrl =
                emailImageBaseUrl + "/zsantasss.jpeg";

        System.out.println("EVENT IMAGE URL = " + eventImageUrl);

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin: 0;
                    padding: 30px 15px;
                    background-color: #f3f4f9d302b1;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #25312b;
                ">

                    <div style="
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #fffdf8;
                        border-radius: 14px;
                        overflow: hidden;
                        border: 1px solid #e4e4dc;
                    ">

                        <!-- FESTIVE HEADER -->
                        <div style="
                            background-color: #174c3c;
                            padding: 28px 30px 24px 30px;
                            text-align: center;
                        ">

                        //     <img
                        //         src="%s"
                        //         alt="Secret Santa"
                        //         width="130"
                        //         style="
                        //             width: 10px;
                        //             max-width: 100%%;
                        //             height: auto;
                        //             display: inline-block;
                        //             margin-bottom: 14px;
                        //         "
                        //     />

                            <h1 style="
                                margin: 0;
                                color: #ffffff;
                                font-size: 25px;
                                line-height: 1.3;
                            ">
                                Your Secret Santa event is live!
                            </h1>

                            <p style="
                                margin: 9px 0 0 0;
                                color: #e8eee9;
                                font-size: 14px;
                                line-height: 1.5;
                            ">
                                The gifts, surprises and questionable wrapping
                                can officially begin.
                            </p>

                        </div>


                        <!-- BODY -->
                        <div style="
                            padding: 32px 38px;
                            background-color: #fffdf8;
                        ">

                            <p style="
                                margin-top: 0;
                                font-size: 16px;
                                line-height: 1.6;
                            ">
                                Hi there!
                            </p>

                            <p style="
                                font-size: 16px;
                                line-height: 1.7;
                                color: #4b554f;
                            ">
                                Your Secret Santa event is officially set up.
                                Now it's time to bring everyone in and get ready
                                for the draw.
                            </p>


                            <!-- EVENT DETAILS -->
                            <div style="
                                background-color: #f3f7f2;
                                border-left: 4px solid #174c3c;
                                padding: 20px 22px;
                                border-radius: 8px;
                                margin: 26px 0;
                            ">

                                <h3 style="
                                    margin: 0 0 14px 0;
                                    color: #174c3c;
                                    font-size: 17px;
                                ">
                                    Event Details
                                </h3>

                                <p style="
                                    margin: 0;
                                    line-height: 2;
                                    font-size: 15px;
                                    color: #39453f;
                                ">
                                    <strong>Event:</strong> %s
                                    <br>

                                    <strong>Draw Date:</strong> %s
                                    <br>

                                    <strong>Budget:</strong> %s %s
                                </p>

                            </div>


                            <!-- INVITATION -->
                            <div style="
                                background-color: #f3f7f2;
                                border-radius: 10px;
                                padding: 24px;
                                text-align: center;
                                margin-top: 26px;
                            ">

                                <h3 style="
                                    margin: 0 0 10px 0;
                                    color: #4b554f;
                                    font-size: 18px;
                                ">
                                    Invite your participants
                                </h3>

                                <p style="
                                    margin: 0 auto 18px auto;
                                    color: #655653;
                                    font-size: 14px;
                                    line-height: 1.6;
                                ">
                                    Share your invitation link with everyone
                                    you'd like to join the gift exchange.
                                </p>

                            </div>


                            <p style="
                                margin: 28px 0 0 0;
                                font-size: 15px;
                                line-height: 1.7;
                                color: #4b554f;
                            ">
                                Once everyone joins, you'll be one step closer
                                to finding out who's gifting who.
                            </p>

                            <p style="
                                margin: 24px 0 0 0;
                                font-size: 15px;
                                line-height: 1.6;
                            ">
                                Happy gifting!
                                <br>
                                <strong style="color: #174c3c;">
                                    The Secret Santa Team
                                </strong>
                            </p>

                        </div>


                        <!-- FOOTER -->
                        <div style="
                            background-color: #174c3c;
                            padding: 15px 25px;
                            text-align: center;
                        ">

                            <p style="
                                margin: 0;
                                color: #dce8df;
                                font-size: 12px;
                                line-height: 1.5;
                            ">
                                Made for good gifts, bad guesses and great memories.
                            </p>

                        </div>

                    </div>

                </body>
                </html>
                """.formatted(
                eventImageUrl,
                eventName,
                drawDate,
                currency,
                budget,
                joinLink
        );

        sendHtmlEmail(
                adminEmail,
                "🎄 Your Secret Santa event is live — " + eventName,
                html
        );
    }


    // ─────────────────────────────────────────────────────────────
    // 2. Organizer: All Participants Have Joined
    // ─────────────────────────────────────────────────────────────

    public void sendAllJoinedNotification(
            String adminEmail,
            String eventName,
            int participantCount
    ) {

        String dashboardLink = frontendUrl + "/dashboard";

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin: 0;
                    padding: 30px 15px;
                    background-color: #f3f4f1;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #25312b;
                ">

                    <div style="
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #fffdf8;
                        border-radius: 14px;
                        overflow: hidden;
                        border: 1px solid #e4e4dc;
                    ">

                        <div style="
                            background-color: #174c3c;
                            padding: 30px;
                            text-align: center;
                        ">

                            <h1 style="
                                color: #ffffff;
                                margin: 0;
                                font-size: 25px;
                            ">
                                Everyone's here!
                            </h1>

                            <p style="
                                color: #e8eee9;
                                margin: 8px 0 0 0;
                                font-size: 14px;
                            ">
                                Your Secret Santa crew is officially complete.
                            </p>

                        </div>


                        <div style="padding: 32px 38px;">

                            <p style="
                                font-size: 16px;
                                line-height: 1.7;
                            ">
                                Great news!
                            </p>

                            <p style="
                                font-size: 15px;
                                line-height: 1.7;
                                color: #4b554f;
                            ">
                                All <strong>%d participants</strong>
                                have joined <strong>%s</strong>.
                            </p>

                            <div style="
                                background-color: #f3f7f2;
                                border-left: 4px solid #174c3c;
                                border-radius: 8px;
                                padding: 20px;
                                margin: 25px 0;
                            ">

                                <p style="
                                    margin: 0;
                                    color: #39453f;
                                    line-height: 1.7;
                                ">
                                    Everyone is now ready for the draw.
                                    Things are about to get interesting.
                                </p>

                            </div>


                            <div style="
                                text-align: center;
                                margin-top: 28px;
                            ">

                                <a
                                    href="%s"
                                    style="
                                        display: inline-block;
                                        background-color: #b43b35;
                                        color: #ffffff;
                                        text-decoration: none;
                                        padding: 13px 24px;
                                        border-radius: 7px;
                                        font-size: 14px;
                                        font-weight: bold;
                                    "
                                >
                                    View Dashboard
                                </a>

                            </div>


                            <p style="
                                margin-top: 30px;
                                font-size: 15px;
                                line-height: 1.6;
                            ">
                                Happy gifting!
                                <br>
                                <strong style="color: #174c3c;">
                                    The Secret Santa Team
                                </strong>
                            </p>

                        </div>


                        <div style="
                            background-color: #174c3c;
                            padding: 15px 25px;
                            text-align: center;
                        ">

                            <p style="
                                margin: 0;
                                color: #dce8df;
                                font-size: 12px;
                            ">
                                Made for good gifts, bad guesses and great memories.
                            </p>

                        </div>

                    </div>

                </body>
                </html>
                """.formatted(
                participantCount,
                eventName,
                dashboardLink
        );

        sendHtmlEmail(
                adminEmail,
                "✅ All " + participantCount +
                        " participants have joined — " + eventName,
                html
        );
    }


    // ─────────────────────────────────────────────────────────────
    // 3. Organizer: All Participants Have Spun
    // ─────────────────────────────────────────────────────────────

    public void sendAllSpunNotification(
            String adminEmail,
            String eventName,
            int round
    ) {

        String dashboardLink = frontendUrl + "/dashboard";

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin: 0;
                    padding: 30px 15px;
                    background-color: #f3f4f1;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #25312b;
                ">

                    <div style="
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #fffdf8;
                        border-radius: 14px;
                        overflow: hidden;
                        border: 1px solid #e4e4dc;
                    ">

                        <div style="
                            background-color: #174c3c;
                            padding: 30px;
                            text-align: center;
                        ">

                            <h1 style="
                                color: #ffffff;
                                margin: 0;
                                font-size: 25px;
                            ">
                                The draw is complete!
                            </h1>

                            <p style="
                                color: #e8eee9;
                                margin: 8px 0 0 0;
                                font-size: 14px;
                            ">
                                The secrets are officially out.
                            </p>

                        </div>


                        <div style="padding: 32px 38px;">

                            <p style="
                                font-size: 15px;
                                line-height: 1.7;
                                color: #4b554f;
                            ">
                                All participants have spun the wheel for
                                <strong>Round %d</strong> of
                                <strong>%s</strong>.
                            </p>

                            <div style="
                                background-color: #fff3f1;
                                border-radius: 8px;
                                padding: 20px;
                                margin: 25px 0;
                            ">

                                <p style="
                                    margin: 0;
                                    color: #655653;
                                    line-height: 1.7;
                                ">
                                    Everyone now knows their secret match.
                                    The gifting can officially begin!
                                </p>

                            </div>


                            <div style="
                                text-align: center;
                                margin-top: 28px;
                            ">

                                <a
                                    href="%s"
                                    style="
                                        display: inline-block;
                                        background-color: #b43b35;
                                        color: #ffffff;
                                        text-decoration: none;
                                        padding: 13px 24px;
                                        border-radius: 7px;
                                        font-size: 14px;
                                        font-weight: bold;
                                    "
                                >
                                    View Results
                                </a>

                            </div>


                            <p style="
                                margin-top: 30px;
                                font-size: 15px;
                                line-height: 1.6;
                            ">
                                Happy gifting!
                                <br>
                                <strong style="color: #174c3c;">
                                    The Secret Santa Team
                                </strong>
                            </p>

                        </div>


                        <div style="
                            background-color: #174c3c;
                            padding: 15px 25px;
                            text-align: center;
                        ">

                            <p style="
                                margin: 0;
                                color: #dce8df;
                                font-size: 12px;
                            ">
                                Made for good gifts, bad guesses and great memories.
                            </p>

                        </div>

                    </div>

                </body>
                </html>
                """.formatted(
                round,
                eventName,
                dashboardLink
        );

        sendHtmlEmail(
                adminEmail,
                "🎉 Round " + round +
                        " complete! Everyone has spun — " + eventName,
                html
        );
    }


    // ─────────────────────────────────────────────────────────────
    // 4. Participant: Wishlist Reminder
    // ─────────────────────────────────────────────────────────────

    public void sendReminder(
            String toEmail,
            String name
    ) {

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin: 0;
                    padding: 30px 15px;
                    background-color: #f3f4f1;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #25312b;
                ">

                    <div style="
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #fffdf8;
                        border-radius: 14px;
                        overflow: hidden;
                        border: 1px solid #e4e4dc;
                    ">

                        <div style="
                            background-color: #174c3c;
                            padding: 30px;
                            text-align: center;
                        ">

                            <h1 style="
                                margin: 0;
                                color: #ffffff;
                                font-size: 24px;
                            ">
                                A little Secret Santa reminder
                            </h1>

                        </div>


                        <div style="padding: 32px 38px;">

                            <p style="
                                font-size: 16px;
                                line-height: 1.7;
                            ">
                                Hi %s!
                            </p>

                            <div style="
                                background-color: #fff3f1;
                                padding: 20px;
                                border-radius: 8px;
                                border-left: 4px solid #b43b35;
                                margin: 22px 0;
                            ">

                                <p style="
                                    margin: 0;
                                    line-height: 1.7;
                                    color: #655653;
                                ">
                                    The Secret Santa draw is coming up soon.
                                    Make sure you've submitted your wishlist
                                    so your Secret Santa isn't left guessing.
                                </p>

                            </div>


                            <p style="
                                margin-top: 28px;
                                font-size: 15px;
                            ">
                                Happy gifting!
                                <br>
                                <strong style="color: #174c3c;">
                                    The Secret Santa Team
                                </strong>
                            </p>

                        </div>


                        <div style="
                            background-color: #174c3c;
                            padding: 15px 25px;
                            text-align: center;
                        ">

                            <p style="
                                margin: 0;
                                color: #dce8df;
                                font-size: 12px;
                            ">
                                Made for good gifts, bad guesses and great memories.
                            </p>

                        </div>

                    </div>

                </body>
                </html>
                """.formatted(name);

        sendHtmlEmail(
                toEmail,
                "Secret Santa Reminder!",
                html
        );
    }


    // ─────────────────────────────────────────────────────────────
    // 5. Participant: Invite
    // ─────────────────────────────────────────────────────────────

    public void sendInvite(
            String toEmail,
            String eventName,
            String joinLink
    ) {

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin: 0;
                    padding: 30px 15px;
                    background-color: #f3f4f1;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #25312b;
                ">

                    <div style="
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #fffdf8;
                        border-radius: 14px;
                        overflow: hidden;
                        border: 1px solid #e4e4dc;
                    ">

                        <div style="
                            background-color: #174c3c;
                            padding: 30px;
                            text-align: center;
                        ">

                            <h1 style="
                                color: #ffffff;
                                margin: 0;
                                font-size: 25px;
                            ">
                                You've been invited!
                            </h1>

                            <p style="
                                color: #e8eee9;
                                margin: 8px 0 0 0;
                                font-size: 14px;
                            ">
                                Someone has a little holiday mystery
                                waiting for you.
                            </p>

                        </div>


                        <div style="padding: 32px 38px;">

                            <p style="
                                font-size: 16px;
                                line-height: 1.6;
                            ">
                                Hi there!
                            </p>

                            <p style="
                                font-size: 15px;
                                line-height: 1.7;
                                color: #4b554f;
                            ">
                                You've been invited to join
                                <strong>%s</strong>,
                                a Secret Santa gift exchange.
                            </p>


                            <div style="
                                background-color: #fff3f1;
                                padding: 22px;
                                border-radius: 9px;
                                text-align: center;
                                margin: 26px 0;
                            ">

                                <p style="
                                    margin: 0 0 18px 0;
                                    color: #655653;
                                    line-height: 1.6;
                                ">
                                    Join the event, submit your wishlist
                                    and get ready for the draw.
                                </p>

                                <a
                                    href="%s"
                                    style="
                                        display: inline-block;
                                        background-color: #b43b35;
                                        color: #ffffff;
                                        text-decoration: none;
                                        padding: 13px 24px;
                                        border-radius: 7px;
                                        font-size: 14px;
                                        font-weight: bold;
                                    "
                                >
                                    Join Secret Santa
                                </a>

                            </div>


                            <p style="
                                margin-top: 28px;
                                font-size: 15px;
                            ">
                                See you there!
                                <br>
                                <strong style="color: #174c3c;">
                                    The Secret Santa Team
                                </strong>
                            </p>

                        </div>


                        <div style="
                            background-color: #174c3c;
                            padding: 15px 25px;
                            text-align: center;
                        ">

                            <p style="
                                margin: 0;
                                color: #dce8df;
                                font-size: 12px;
                            ">
                                Made for good gifts, bad guesses and great memories.
                            </p>

                        </div>

                    </div>

                </body>
                </html>
                """.formatted(
                eventName,
                joinLink
        );

        sendHtmlEmail(
                toEmail,
                "🎄 You're invited to " + eventName + "!",
                html
        );
    }


    // ─────────────────────────────────────────────────────────────
    // 6. Participant: Password Reset
    // ─────────────────────────────────────────────────────────────

    public void sendPasswordReset(
            String toEmail,
            String name,
            String resetLink
    ) {

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin: 0;
                    padding: 30px 15px;
                    background-color: #f3f4f1;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #25312b;
                ">

                    <div style="
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #fffdf8;
                        border-radius: 14px;
                        overflow: hidden;
                        border: 1px solid #e4e4dc;
                    ">

                        <div style="
                            background-color: #174c3c;
                            padding: 30px;
                            text-align: center;
                        ">

                            <h1 style="
                                color: #ffffff;
                                margin: 0;
                                font-size: 24px;
                            ">
                                Reset your password
                            </h1>

                        </div>


                        <div style="padding: 32px 38px;">

                            <p style="
                                font-size: 16px;
                                line-height: 1.6;
                            ">
                                Hi %s,
                            </p>

                            <p style="
                                font-size: 15px;
                                line-height: 1.7;
                                color: #4b554f;
                            ">
                                We received a request to reset your
                                Secret Santa password.
                            </p>


                            <div style="
                                background-color: #f3f7f2;
                                padding: 22px;
                                border-radius: 9px;
                                text-align: center;
                                margin: 26px 0;
                            ">

                                <a
                                    href="%s"
                                    style="
                                        display: inline-block;
                                        background-color: #174c3c;
                                        color: #ffffff;
                                        text-decoration: none;
                                        padding: 13px 24px;
                                        border-radius: 7px;
                                        font-size: 14px;
                                        font-weight: bold;
                                    "
                                >
                                    Reset Password
                                </a>

                            </div>


                            <p style="
                                font-size: 13px;
                                line-height: 1.7;
                                color: #737b76;
                            ">
                                This link expires in 1 hour.
                                If you did not request a password reset,
                                you can safely ignore this email.
                            </p>


                            <p style="
                                margin-top: 28px;
                                font-size: 15px;
                            ">
                                <strong style="color: #174c3c;">
                                    The Secret Santa Team
                                </strong>
                            </p>

                        </div>


                        <div style="
                            background-color: #174c3c;
                            padding: 15px 25px;
                            text-align: center;
                        ">

                            <p style="
                                margin: 0;
                                color: #dce8df;
                                font-size: 12px;
                            ">
                                Made for good gifts, bad guesses and great memories.
                            </p>

                        </div>

                    </div>

                </body>
                </html>
                """.formatted(
                name,
                resetLink
        );

        sendHtmlEmail(
                toEmail,
                "Reset your Secret Santa password",
                html
        );
    }
}