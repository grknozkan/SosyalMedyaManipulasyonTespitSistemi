package com.detector.manipulation.model;

public class SystemMetrics {
    private long totalAnalyzedPosts;
    private long flaggedPostsCount;
    private int activeClustersCount;
    private int highThreatCount;
    private int identifiedBotAccounts;
    private double averageThreatScore;
    private boolean burstActivityDetected;
    private double manipulationRatioPercent;

    public SystemMetrics() {}

    public long getTotalAnalyzedPosts() {
        return totalAnalyzedPosts;
    }

    public void setTotalAnalyzedPosts(long totalAnalyzedPosts) {
        this.totalAnalyzedPosts = totalAnalyzedPosts;
    }

    public long getFlaggedPostsCount() {
        return flaggedPostsCount;
    }

    public void setFlaggedPostsCount(long flaggedPostsCount) {
        this.flaggedPostsCount = flaggedPostsCount;
    }

    public int getActiveClustersCount() {
        return activeClustersCount;
    }

    public void setActiveClustersCount(int activeClustersCount) {
        this.activeClustersCount = activeClustersCount;
    }

    public int getHighThreatCount() {
        return highThreatCount;
    }

    public void setHighThreatCount(int highThreatCount) {
        this.highThreatCount = highThreatCount;
    }

    public int getIdentifiedBotAccounts() {
        return identifiedBotAccounts;
    }

    public void setIdentifiedBotAccounts(int identifiedBotAccounts) {
        this.identifiedBotAccounts = identifiedBotAccounts;
    }

    public double getAverageThreatScore() {
        return averageThreatScore;
    }

    public void setAverageThreatScore(double averageThreatScore) {
        this.averageThreatScore = averageThreatScore;
    }

    public boolean isBurstActivityDetected() {
        return burstActivityDetected;
    }

    public void setBurstActivityDetected(boolean burstActivityDetected) {
        this.burstActivityDetected = burstActivityDetected;
    }

    public double getManipulationRatioPercent() {
        return manipulationRatioPercent;
    }

    public void setManipulationRatioPercent(double manipulationRatioPercent) {
        this.manipulationRatioPercent = manipulationRatioPercent;
    }
}
