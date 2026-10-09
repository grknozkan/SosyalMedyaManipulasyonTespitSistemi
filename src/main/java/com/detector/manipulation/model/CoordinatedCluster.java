package com.detector.manipulation.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class CoordinatedCluster {
    private String clusterId;
    private String campaignName;
    private String coreNarrative;
    private Instant firstDetected;
    private Instant lastActivity;
    private int postCount;
    private double averageSimilarity;
    private ThreatLevel severity = ThreatLevel.MEDIUM;
    private Set<String> accounts = new HashSet<>();
    private Set<String> hashtags = new HashSet<>();
    private String samplePostSnippet;
    private boolean active = true;

    public CoordinatedCluster() {
        this.firstDetected = Instant.now();
        this.lastActivity = Instant.now();
    }

    public CoordinatedCluster(String clusterId, String campaignName, String coreNarrative) {
        this();
        this.clusterId = clusterId;
        this.campaignName = campaignName;
        this.coreNarrative = coreNarrative;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getCoreNarrative() {
        return coreNarrative;
    }

    public void setCoreNarrative(String coreNarrative) {
        this.coreNarrative = coreNarrative;
    }

    public Instant getFirstDetected() {
        return firstDetected;
    }

    public void setFirstDetected(Instant firstDetected) {
        this.firstDetected = firstDetected;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public int getAccountCount() {
        return accounts != null ? accounts.size() : 0;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public double getAverageSimilarity() {
        return averageSimilarity;
    }

    public void setAverageSimilarity(double averageSimilarity) {
        this.averageSimilarity = averageSimilarity;
    }

    public ThreatLevel getSeverity() {
        return severity;
    }

    public void setSeverity(ThreatLevel severity) {
        this.severity = severity;
    }

    public Set<String> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<String> accounts) {
        this.accounts = accounts;
    }

    public Set<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(Set<String> hashtags) {
        this.hashtags = hashtags;
    }

    public String getSamplePostSnippet() {
        return samplePostSnippet;
    }

    public void setSamplePostSnippet(String samplePostSnippet) {
        this.samplePostSnippet = samplePostSnippet;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
