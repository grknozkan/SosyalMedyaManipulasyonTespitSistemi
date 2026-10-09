package com.detector.manipulation.engine;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TemporalBurstDetector {

    // Tracks timestamps of recent posts for each hashtag (sliding window)
    private final Map<String, List<Instant>> hashtagTimeWindows = new ConcurrentHashMap<>();
    
    // Window duration in seconds (e.g., 60 seconds)
    private static final long WINDOW_SECONDS = 60;
    private static final int BURST_THRESHOLD_POSTS = 5; // >= 5 posts in 60s for a single hashtag/topic is a burst in simulated stream

    /**
     * Records an event and checks if any of the hashtags trigger a temporal burst
     */
    public BurstCheckResult checkBurst(List<String> hashtags, Instant timestamp) {
        if (hashtags == null || hashtags.isEmpty()) {
            return new BurstCheckResult(false, Collections.emptyList());
        }

        Instant cutoff = timestamp.minusSeconds(WINDOW_SECONDS);
        boolean burstDetected = false;
        List<String> burstHashtags = new ArrayList<>();

        for (String tag : hashtags) {
            String lowerTag = tag.toLowerCase(Locale.ROOT);
            hashtagTimeWindows.compute(lowerTag, (key, list) -> {
                if (list == null) {
                    list = new ArrayList<>();
                }
                // Prune older timestamps
                list.removeIf(t -> t.isBefore(cutoff));
                list.add(timestamp);
                return list;
            });

            List<Instant> currentList = hashtagTimeWindows.get(lowerTag);
            if (currentList != null && currentList.size() >= BURST_THRESHOLD_POSTS) {
                burstDetected = true;
                burstHashtags.add(lowerTag + " (" + currentList.size() + " gönderi/" + WINDOW_SECONDS + "s)");
            }
        }

        return new BurstCheckResult(burstDetected, burstHashtags);
    }

    /**
     * Clears sliding history
     */
    public void reset() {
        hashtagTimeWindows.clear();
    }

    public record BurstCheckResult(boolean hasBurst, List<String> burstDetails) {}
}
