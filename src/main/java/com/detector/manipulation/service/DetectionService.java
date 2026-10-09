package com.detector.manipulation.service;

import com.detector.manipulation.engine.ManipulationDetectionEngine;
import com.detector.manipulation.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DetectionService {

    private final ManipulationDetectionEngine detectionEngine;
    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    private final AtomicLong totalAnalyzed = new AtomicLong(0);
    private final AtomicLong totalFlagged = new AtomicLong(0);
    private final AtomicLong highThreatCount = new AtomicLong(0);

    public DetectionService(ManipulationDetectionEngine detectionEngine) {
        this.detectionEngine = detectionEngine;
    }

    /**
     * Ingests and analyzes a social media post in real-time
     */
    public SocialPost processPost(SocialPost post) {
        SocialPost analyzed = detectionEngine.analyze(post);
        totalAnalyzed.incrementAndGet();

        if (analyzed.getThreatLevel() != ThreatLevel.LOW) {
            totalFlagged.incrementAndGet();
        }
        if (analyzed.getThreatLevel() == ThreatLevel.HIGH || analyzed.getThreatLevel() == ThreatLevel.CRITICAL) {
            highThreatCount.incrementAndGet();
        }

        // Broadcast to SSE clients for instant live UI update
        broadcastEvent("new-post", analyzed);

        return analyzed;
    }

    /**
     * Ad-hoc manual inspection for the user to test custom text
     */
    public AnalyzeResponse inspectText(AnalyzeRequest request) {
        SocialPost mock = new SocialPost();
        mock.setId("INSPECT-" + UUID.randomUUID().toString().substring(0, 6));
        mock.setContent(request.getText());
        mock.setAuthorUsername(request.getUsername() != null && !request.getUsername().isBlank() ? request.getUsername() : "test_user");
        mock.setAuthorFollowers(request.getFollowerCount() != null ? request.getFollowerCount() : 100);
        mock.setAuthorFollowing(request.getFollowingCount() != null ? request.getFollowingCount() : 250);
        mock.setAuthorAccountAgeDays(request.getAccountAgeDays() != null ? request.getAccountAgeDays() : 120);
        mock.setHasDefaultAvatar(request.getDefaultAvatar() != null ? request.getDefaultAvatar() : false);
        mock.setPlatform("Interactive Inspector");

        SocialPost analyzed = detectionEngine.analyze(mock);

        AnalyzeResponse response = new AnalyzeResponse();
        response.setOverallManipulationScore(analyzed.getManipulationScore());
        response.setThreatLevel(analyzed.getThreatLevel());
        response.setHighestSimilarityScore(analyzed.getHighestSimilarityScore());
        response.setFlaggedReasons(analyzed.getFlagReasons());
        response.setExtractedHashtags(analyzed.getHashtags());
        response.setExtractedMentions(analyzed.getMentions());
        response.setBotSuspect(analyzed.isBotSuspect());
        response.setMatchedClusterNarrative(analyzed.getClusterId());

        return response;
    }

    /**
     * Aggregates real-time threat metrics
     */
    public SystemMetrics getMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        long total = totalAnalyzed.get();
        long flagged = totalFlagged.get();
        metrics.setTotalAnalyzedPosts(total);
        metrics.setFlaggedPostsCount(flagged);
        metrics.setActiveClustersCount(detectionEngine.getActiveClusters().size());
        metrics.setHighThreatCount((int) highThreatCount.get());

        long botAccounts = detectionEngine.getAccountProfiles().stream()
                .filter(AccountRiskProfile::isFlagged)
                .count();
        metrics.setIdentifiedBotAccounts((int) botAccounts);

        double ratio = total > 0 ? (double) flagged / total * 100.0 : 0.0;
        metrics.setManipulationRatioPercent(Math.round(ratio * 10.0) / 10.0);

        List<SocialPost> recent = detectionEngine.getRecentPosts(50);
        double avgScore = recent.stream()
                .mapToDouble(SocialPost::getManipulationScore)
                .average()
                .orElse(0.0);
        metrics.setAverageThreatScore(Math.round(avgScore * 10.0) / 10.0);

        return metrics;
    }

    public List<SocialPost> getRecentPosts(int limit) {
        return detectionEngine.getRecentPosts(limit);
    }

    public Collection<CoordinatedCluster> getClusters() {
        return detectionEngine.getActiveClusters();
    }

    public List<AccountRiskProfile> getFlaggedAccounts() {
        return detectionEngine.getAccountProfiles().stream()
                .filter(p -> p.getBotScore() >= 50.0 || p.isFlagged())
                .sorted(Comparator.comparingDouble(AccountRiskProfile::getBotScore).reversed())
                .toList();
    }

    public void clearAll() {
        detectionEngine.clearAll();
        totalAnalyzed.set(0);
        totalFlagged.set(0);
        highThreatCount.set(0);
        broadcastEvent("system-reset", Map.of("message", "Veriler sıfırlandı"));
    }

    // SSE Registration
    public SseEmitter registerSseEmitter() {
        SseEmitter emitter = new SseEmitter(600_000L); // 10 minutes timeout
        sseEmitters.add(emitter);

        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> sseEmitters.remove(emitter));
        emitter.onError(e -> sseEmitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("init").data(getMetrics()));
        } catch (IOException e) {
            sseEmitters.remove(emitter);
        }

        return emitter;
    }

    private void broadcastEvent(String eventName, Object data) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : sseEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        sseEmitters.removeAll(deadEmitters);
    }
}
