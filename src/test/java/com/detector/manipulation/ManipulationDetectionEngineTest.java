package com.detector.manipulation;

import com.detector.manipulation.engine.*;
import com.detector.manipulation.model.SocialPost;
import com.detector.manipulation.model.ThreatLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ManipulationDetectionEngineTest {

    private SimilarityEngine similarityEngine;
    private BotScorer botScorer;
    private TemporalBurstDetector temporalBurstDetector;
    private ManipulationDetectionEngine detectionEngine;

    @BeforeEach
    void setUp() {
        similarityEngine = new SimilarityEngine();
        botScorer = new BotScorer();
        temporalBurstDetector = new TemporalBurstDetector();
        detectionEngine = new ManipulationDetectionEngine(similarityEngine, botScorer, temporalBurstDetector);
    }

    @Test
    void testSimilarityEngineCalculatesHighForCopypasta() {
        String original = "ACİL DUYURU! $SOLAR coin büyük borsalarda listeleniyor, en az 100x yapacak sakın kaçırmayın! #SolarMoon #CryptoPump";
        String variation = "Son dakika! $SOLAR coin listeleniyor, 100x yapacak sakın kaçırmayın! #SolarMoon #CryptoPump";

        double similarity = similarityEngine.calculateOverallSimilarity(original, variation);
        assertTrue(similarity >= 0.50, "Copypasta variations should score high similarity, got: " + similarity);
    }

    @Test
    void testBotScorerFlagsSuspiciousAccount() {
        SocialPost botPost = new SocialPost();
        botPost.setAuthorUsername("bot_user849204");
        botPost.setAuthorFollowers(1);
        botPost.setAuthorFollowing(750);
        botPost.setAuthorAccountAgeDays(2);
        botPost.setHasDefaultAvatar(true);

        BotScorer.BotScoreResult result = botScorer.evaluateAccount(botPost, null);
        assertTrue(result.score() >= 60.0, "Suspicious bot account score should be high, got: " + result.score());
        assertTrue(result.reasons().size() >= 3, "Should have multiple flagged reasons");
    }

    @Test
    void testOrganicPostReceivesLowThreatLevel() {
        SocialPost organic = new SocialPost();
        organic.setId("ORG-1");
        organic.setAuthorUsername("ayse_kaya");
        organic.setAuthorFollowers(1200);
        organic.setAuthorFollowing(450);
        organic.setAuthorAccountAgeDays(800);
        organic.setHasDefaultAvatar(false);
        organic.setContent("Bugün hava çok güzel, parkta kitap okumak harika bir fikir.");

        SocialPost analyzed = detectionEngine.analyze(organic);
        assertEquals(ThreatLevel.LOW, analyzed.getThreatLevel());
        assertTrue(analyzed.getManipulationScore() < 30.0);
    }

    @Test
    void testCoordinatedBotsFormCIBCluster() {
        // First bot
        SocialPost bot1 = new SocialPost();
        bot1.setId("B1");
        bot1.setAuthorUsername("spammer_bot1192");
        bot1.setAuthorFollowers(0);
        bot1.setAuthorFollowing(400);
        bot1.setAuthorAccountAgeDays(1);
        bot1.setHasDefaultAvatar(true);
        bot1.setContent("Büyük kampanya! #XfirmasıBoykot ediyoruz sesimizi duyuruyoruz!");
        bot1.setTimestamp(Instant.now());

        detectionEngine.analyze(bot1);

        // Second bot with similar content and different account
        SocialPost bot2 = new SocialPost();
        bot2.setId("B2");
        bot2.setAuthorUsername("spammer_bot2839");
        bot2.setAuthorFollowers(2);
        bot2.setAuthorFollowing(500);
        bot2.setAuthorAccountAgeDays(1);
        bot2.setHasDefaultAvatar(true);
        bot2.setContent("Büyük kampanya! #XfirmasıBoykot ediyoruz sesimizi duyuralım!");
        bot2.setTimestamp(Instant.now());

        SocialPost analyzed2 = detectionEngine.analyze(bot2);

        assertNotNull(analyzed2.getClusterId(), "Second bot should be linked to a CIB cluster");
        assertTrue(analyzed2.getManipulationScore() >= 50.0, "Coordinated bot should score high manipulation score");
        assertEquals(1, detectionEngine.getActiveClusters().size(), "One CIB cluster should be registered");
    }
}
