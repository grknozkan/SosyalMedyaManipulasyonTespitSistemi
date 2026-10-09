package com.detector.manipulation.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AccountRiskProfile {
    private String username;
    private double botScore; // 0.0 - 100.0
    private boolean flagged;
    private int totalPosts;
    private int flaggedPosts;
    private double followerToFollowingRatio;
    private int accountAgeDays;
    private boolean defaultAvatar;
    private Instant lastSeen;
    private List<String> primaryRiskFactors = new ArrayList<>();

    public AccountRiskProfile() {
        this.lastSeen = Instant.now();
    }

    public AccountRiskProfile(String username) {
        this();
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getBotScore() {
        return botScore;
    }

    public void setBotScore(double botScore) {
        this.botScore = botScore;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public int getFlaggedPosts() {
        return flaggedPosts;
    }

    public void setFlaggedPosts(int flaggedPosts) {
        this.flaggedPosts = flaggedPosts;
    }

    public double getFollowerToFollowingRatio() {
        return followerToFollowingRatio;
    }

    public void setFollowerToFollowingRatio(double followerToFollowingRatio) {
        this.followerToFollowingRatio = followerToFollowingRatio;
    }

    public int getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(int accountAgeDays) {
        this.accountAgeDays = accountAgeDays;
    }

    public boolean isDefaultAvatar() {
        return defaultAvatar;
    }

    public void setDefaultAvatar(boolean defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public List<String> getPrimaryRiskFactors() {
        return primaryRiskFactors;
    }

    public void setPrimaryRiskFactors(List<String> primaryRiskFactors) {
        this.primaryRiskFactors = primaryRiskFactors;
    }
}
