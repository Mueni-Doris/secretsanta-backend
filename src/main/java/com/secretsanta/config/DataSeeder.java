package com.secretsanta.config;

import com.secretsanta.model.Event;
import com.secretsanta.model.Match;
import com.secretsanta.model.Participant;
import com.secretsanta.repository.EventRepository;
import com.secretsanta.repository.MatchRepository;
import com.secretsanta.repository.ParticipantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            ParticipantRepository participantRepo,
            MatchRepository matchRepo,
            EventRepository eventRepo
    ) {
        return args -> {

            // Only seed once
            if (participantRepo.count() > 0) {
                System.out.println("✅ Database already seeded — skipping.");
                return;
            }

            System.out.println("🌱 Seeding family data...");

            // ── SEED EVENT ──────────────────────────────────────────────────
            Event event = new Event();
            event.setName("Family Holiday Swap 2024");
            event.setDrawDate("2024-12-20");
            event.setBudget("1500");
            event.setCurrency("KES");
            event.setRules("No gift cards. Keep it thoughtful!");
            event.setOrganizerEmail("doris@family.com");
            event.setStatus("active");
            eventRepo.save(event);

            // ── SEED PARTICIPANTS ────────────────────────────────────────────
            // Using setters so adding/removing fields never breaks this file

            Participant dad = new Participant();
            dad.setName("Dad");
            dad.setEmail("dad@family.com");
            dad.setStatus("Joined");
            dad.setWishlistStatus("Submitted");
            dad.setAvatarColor("#c8453a");
            dad.setHasSpun(true);

            Participant mum = new Participant();
            mum.setName("Mum");
            mum.setEmail("mum@family.com");
            mum.setStatus("Joined");
            mum.setWishlistStatus("Submitted");
            mum.setAvatarColor("#2a7a3a");
            mum.setHasSpun(true);

            Participant diana = new Participant();
            diana.setName("Diana");
            diana.setNickname("Dee");
            diana.setEmail("diana@family.com");
            diana.setStatus("Joined");
            diana.setWishlistStatus("Submitted");
            diana.setAvatarColor("#7a5c1e");
            diana.setHasSpun(true);

            Participant dorah = new Participant();
            dorah.setName("Dorah");
            dorah.setEmail("dorah@family.com");
            dorah.setStatus("Joined");
            dorah.setWishlistStatus("Submitted");
            dorah.setAvatarColor("#1a4a7a");
            dorah.setHasSpun(true);

            Participant doris = new Participant();
            doris.setName("Doris");
            doris.setEmail("muenidoria04@gmail.com");
            doris.setStatus("Joined");
            doris.setWishlistStatus("Submitted");
            doris.setAvatarColor("#6a2a7a");
            doris.setHasSpun(true);

            Participant delvis = new Participant();
            delvis.setName("Delvis");
            delvis.setEmail("delvis@family.com");
            delvis.setStatus("Joined");
            delvis.setWishlistStatus("Pending");
            delvis.setAvatarColor("#8a3a2a");
            delvis.setHasSpun(true);

            List<Participant> saved = participantRepo.saveAll(
                    List.of(dad, mum, diana, dorah, doris, delvis)
            );

            // Map by name for clean reference
            Participant pDad    = getByName(saved, "Dad");
            Participant pMum    = getByName(saved, "Mum");
            Participant pDiana  = getByName(saved, "Diana");
            Participant pDorah  = getByName(saved, "Dorah");
            Participant pDoris  = getByName(saved, "Doris");
            Participant pDelvis = getByName(saved, "Delvis");

            // ── SEED ROUND 1 MATCHES ─────────────────────────────────────────
            // Dad    → Doris
            // Mum    → Diana (Dee)
            // Diana  → Delvis
            // Dorah  → Dad
            // Doris  → Dorah
            // Delvis → Mum

            Match m1 = new Match();
            m1.setGiverId(pDad.getId());
            m1.setGiverName("Dad");
            m1.setReceiverId(pDoris.getId());
            m1.setReceiverName("Doris");
            m1.setAvatarColor(pDoris.getAvatarColor());
            m1.setRound(1);

            Match m2 = new Match();
            m2.setGiverId(pMum.getId());
            m2.setGiverName("Mum");
            m2.setReceiverId(pDiana.getId());
            m2.setReceiverName("Diana");
            m2.setAvatarColor(pDiana.getAvatarColor());
            m2.setRound(1);

            Match m3 = new Match();
            m3.setGiverId(pDiana.getId());
            m3.setGiverName("Diana");
            m3.setReceiverId(pDelvis.getId());
            m3.setReceiverName("Delvis");
            m3.setAvatarColor(pDelvis.getAvatarColor());
            m3.setRound(1);

            Match m4 = new Match();
            m4.setGiverId(pDorah.getId());
            m4.setGiverName("Dorah");
            m4.setReceiverId(pDad.getId());
            m4.setReceiverName("Dad");
            m4.setAvatarColor(pDad.getAvatarColor());
            m4.setRound(1);

            Match m5 = new Match();
            m5.setGiverId(pDoris.getId());
            m5.setGiverName("Doris");
            m5.setReceiverId(pDorah.getId());
            m5.setReceiverName("Dorah");
            m5.setAvatarColor(pDorah.getAvatarColor());
            m5.setRound(1);

            Match m6 = new Match();
            m6.setGiverId(pDelvis.getId());
            m6.setGiverName("Delvis");
            m6.setReceiverId(pMum.getId());
            m6.setReceiverName("Mum");
            m6.setAvatarColor(pMum.getAvatarColor());
            m6.setRound(1);

            matchRepo.saveAll(List.of(m1, m2, m3, m4, m5, m6));

            System.out.println("✅ Seeded: 1 event, 6 participants, 6 Round 1 matches.");
            System.out.println("   Dad→Doris | Mum→Diana(Dee) | Diana→Delvis");
            System.out.println("   Dorah→Dad | Doris→Dorah    | Delvis→Mum");
        };
    }

    private Participant getByName(List<Participant> list, String name) {
        return list.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant not found: " + name));
    }
}