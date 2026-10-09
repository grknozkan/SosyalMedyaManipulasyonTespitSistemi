package com.detector.manipulation.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SocialPost {
    private String id;
    private String authorId;
    private String authorUsername;
    private int authorFollowers;
    private int authorFollowing;
    private int authorAccountAgeDays;
    private boolean hasDefaultAvatar;
    private String content;
    private Instant timestamp;
    private List<String> hashtags = new ArrayList<>();
    private List<String> mentions = new ArrayList<>();
    private String platform;
    
    // Manipulation analysis fields
    private double manipulationScore; // 0.0 - 100.0
    private ThreatLevel threatLevel = ThreatLevel.LOW;
    private List<String> flagReasons = new ArrayList<>();
    private String clusterId;
    private double highestSimilarityScore;
    private boolean isBotSuspect;

    public SocialPost() {
        this.timestamp = Instant.now();
    }

    public SocialPost(String id, String authorUsername, String content, Instant timestamp) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.content = content;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public int getAuthorFollowers() {
        return authorFollowers;
    }

    public void setAuthorFollowers(int authorFollowers) {
        this.authorFollowers = authorFollowers;
    }

    public int getAuthorFollowing() {
        return authorFollowing;
    }

    public void setAuthorFollowing(int authorFollowing) {
        this.authorFollowing = authorFollowing;
    }

    public int getAuthorAccountAgeDays() {
        return authorAccountAgeDays;
    }

    public void setAuthorAccountAgeDays(int authorAccountAgeDays) {
        this.authorAccountAgeDays = authorAccountAgeDays;
    }

    public boolean isHasDefaultAvatar() {
        return hasDefaultAvatar;
    }

    public void setHasDefaultAvatar(boolean hasDefaultAvatar) {
        this.hasDefaultAvatar = hasDefaultAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public List<String> getMentions() {
        return mentions;
    }

    public void setMentions(List<String> mentions) {
        this.mentions = mentions;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public double getManipulationScore() {
        return manipulationScore;
    }

    public void setManipulationScore(double manipulationScore) {
        this.manipulationScore = manipulationScore;
    }

    public ThreatLevel getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(ThreatLevel threatLevel) {
        this.threatLevel = threatLevel;
    }

    public List<String> getFlagReasons() {
        return flagReasons;
    }

    public void setFlagReasons(List<String> flagReasons) {
        this.flagReasons = flagReasons;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public double getHighestSimilarityScore() {
        return highestSimilarityScore;
    }

    public void setHighestSimilarityScore(double highestSimilarityScore) {
        this.highestSimilarityScore = highestSimilarityScore;
    }

    public boolean isBotSuspect() {
        return isBotSuspect;
    }

    public void setBotSuspect(boolean botSuspect) {
        isBotSuspect = botSuspect;
    }
}
