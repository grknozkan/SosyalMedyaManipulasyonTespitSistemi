package com.detector.manipulation.engine;

import com.detector.manipulation.model.AccountRiskProfile;
import com.detector.manipulation.model.CoordinatedCluster;
import com.detector.manipulation.model.SocialPost;
import com.detector.manipulation.model.ThreatLevel;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class ManipulationDetectionEngine {

    private final SimilarityEngine similarityEngine;
    private final BotScorer botScorer;
    private final TemporalBurstDetector temporalBurstDetector;

    // Rolling memory buffer of recent posts for cross-comparison (last 300 posts)
    private final Deque<SocialPost> recentPostBuffer = new ConcurrentLinkedDeque<>();
    private static final int BUFFER_MAX_SIZE = 300;

    // Active coordinated clusters (CIB)
    private final Map<String, CoordinatedCluster> clusters = new ConcurrentHashMap<>();

    // Account risk profiles cache
    private final Map<String, AccountRiskProfile> accountProfiles = new ConcurrentHashMap<>();

    public ManipulationDetectionEngine(SimilarityEngine similarityEngine,
                                       BotScorer botScorer,
                                       TemporalBurstDetector temporalBurstDetector) {
        this.similarityEngine = similarityEngine;
        this.botScorer = botScorer;
        this.temporalBurstDetector = temporalBurstDetector;
    }

    /**
     * Analyzes an incoming post against real-time network history, bot patterns, and coordination clusters
     */
    public synchronized SocialPost analyze(SocialPost post) {
        List<String> flagReasons = new ArrayList<>();
        double totalScore = 0.0;

        // Extract metadata if empty
        if (post.getHashtags() == null || post.getHashtags().isEmpty()) {
            post.setHashtags(similarityEngine.extractHashtags(post.getContent()));
        }
        if (post.getMentions() == null || post.getMentions().isEmpty()) {
            post.setMentions(similarityEngine.extractMentions(post.getContent()));
        }

        // 1. Account Risk Profiling
        String username = post.getAuthorUsername() != null ? post.getAuthorUsername() : "unknown";
        AccountRiskProfile profile = accountProfiles.computeIfAbsent(username, k -> {
            AccountRiskProfile p = new AccountRiskProfile(k);
            p.setAccountAgeDays(post.getAuthorAccountAgeDays());
            p.setDefaultAvatar(post.isHasDefaultAvatar());
            return p;
        });
        profile.setTotalPosts(profile.getTotalPosts() + 1);

        BotScorer.BotScoreResult botResult = botScorer.evaluateAccount(post, profile);
        double botScore = botResult.score();
        if (botScore > 40.0) {
            post.setBotSuspect(true);
            totalScore += (botScore * 0.35); // Max 35 points from bot characteristics
            flagReasons.addAll(botResult.reasons());
        }

        // 2. Similarity & Copypasta Analysis against recent posts
        double maxSimilarity = 0.0;
        SocialPost mostSimilarPost = null;

        for (SocialPost recent : recentPostBuffer) {
            // Compare only with posts from DIFFERENT accounts within last 15 minutes
            if (recent.getAuthorUsername() != null && !recent.getAuthorUsername().equalsIgnoreCase(username)) {
                long minutesDiff = Math.abs(Duration.between(recent.getTimestamp(), post.getTimestamp()).toMinutes());
                if (minutesDiff <= 15) {
                    double sim = similarityEngine.calculateOverallSimilarity(post.getContent(), recent.getContent());
                    if (sim > maxSimilarity) {
                        maxSimilarity = sim;
                        mostSimilarPost = recent;
                    }
                }
            }
        }

        post.setHighestSimilarityScore(Math.round(maxSimilarity * 100.0) / 100.0);

        // Copypasta detection threshold (>= 50% similarity between distinct accounts)
        if (maxSimilarity >= 0.50) {
            int simPercent = (int) Math.round(maxSimilarity * 100.0);
            double similarityContribution = (maxSimilarity * 45.0); // Up to 45 points from cross-account copypasta
            totalScore += similarityContribution;
            flagReasons.add("Farklı hesaplar arasında yüksek metin benzerliği (" + simPercent + "% eşleşme)");

            // 3. Coordinated Inauthentic Behavior (CIB) Cluster Association
            String clusterId = linkOrCreateCluster(post, mostSimilarPost, maxSimilarity);
            post.setClusterId(clusterId);
            flagReasons.add("Koordineli Gerçek Dışı Davranış (CIB) kümesi tespit edildi: [" + clusterId + "]");
        }

        // 4. Temporal Burst (Anomali) Detection
        TemporalBurstDetector.BurstCheckResult burstResult = temporalBurstDetector.checkBurst(post.getHashtags(), post.getTimestamp());
        if (burstResult.hasBurst()) {
            totalScore += 20.0; // Up to 20 points from sudden burst activity
            for (String detail : burstResult.burstDetails()) {
                flagReasons.add("Zamansal Frekans Anomalisi (Burst): " + detail);
            }
        }

        // 5. Final Score Calculation & Classification
        double finalScore = Math.min(100.0, Math.max(0.0, Math.round(totalScore * 10.0) / 10.0));
        post.setManipulationScore(finalScore);
        post.setFlagReasons(flagReasons);

        ThreatLevel threat;
        if (finalScore >= 75.0) {
            threat = ThreatLevel.CRITICAL;
        } else if (finalScore >= 55.0) {
            threat = ThreatLevel.HIGH;
        } else if (finalScore >= 30.0) {
            threat = ThreatLevel.MEDIUM;
        } else {
            threat = ThreatLevel.LOW;
        }
        post.setThreatLevel(threat);

        // Update Account Profile
        if (threat == ThreatLevel.HIGH || threat == ThreatLevel.CRITICAL) {
            profile.setFlaggedPosts(profile.getFlaggedPosts() + 1);
            profile.setFlagged(true);
        }
        profile.setBotScore(botScore);
        profile.setPrimaryRiskFactors(flagReasons);

        // Add to buffer
        recentPostBuffer.addFirst(post);
        if (recentPostBuffer.size() > BUFFER_MAX_SIZE) {
            recentPostBuffer.removeLast();
        }

        return post;
    }

    /**
     * Groups posts and accounts into coordinated campaign clusters
     */
    private String linkOrCreateCluster(SocialPost current, SocialPost matched, double similarity) {
        String existingClusterId = matched != null ? matched.getClusterId() : null;
        CoordinatedCluster cluster;

        if (existingClusterId != null && clusters.containsKey(existingClusterId)) {
            cluster = clusters.get(existingClusterId);
        } else {
            // Create a new cluster
            String newId = "CIB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
            String narrative = deriveNarrative(current.getContent(), current.getHashtags());
            cluster = new CoordinatedCluster(newId, "Koordineli Kampanya #" + newId, narrative);
            cluster.setSamplePostSnippet(current.getContent().length() > 90 ? current.getContent().substring(0, 87) + "..." : current.getContent());
            clusters.put(newId, cluster);
            if (matched != null) {
                matched.setClusterId(newId);
                cluster.getAccounts().add(matched.getAuthorUsername());
            }
        }

        cluster.getAccounts().add(current.getAuthorUsername());
        if (current.getHashtags() != null) {
            cluster.getHashtags().addAll(current.getHashtags());
        }
        cluster.setPostCount(cluster.getPostCount() + 1);
        cluster.setLastActivity(Instant.now());
        cluster.setAverageSimilarity(Math.max(cluster.getAverageSimilarity(), similarity));

        if (cluster.getAccountCount() >= 5) {
            cluster.setSeverity(ThreatLevel.CRITICAL);
        } else if (cluster.getAccountCount() >= 3) {
            cluster.setSeverity(ThreatLevel.HIGH);
        }

        return cluster.getClusterId();
    }

    private String deriveNarrative(String content, List<String> hashtags) {
        if (hashtags != null && !hashtags.isEmpty()) {
            return "Etiket Odaklı Koordinasyon: " + String.join(" ", hashtags);
        }
        String preview = content.replaceAll("\\s+", " ").trim();
        return preview.length() > 60 ? preview.substring(0, 57) + "..." : preview;
    }

    public List<SocialPost> getRecentPosts(int limit) {
        return recentPostBuffer.stream().limit(limit).toList();
    }

    public Collection<CoordinatedCluster> getActiveClusters() {
        return clusters.values();
    }

    public Collection<AccountRiskProfile> getAccountProfiles() {
        return accountProfiles.values();
    }

    public void clearAll() {
        recentPostBuffer.clear();
        clusters.clear();
        accountProfiles.clear();
        temporalBurstDetector.reset();
    }
}
