package com.detector.manipulation.model;

import java.util.List;

public class AnalyzeResponse {
    private double overallManipulationScore;
    private ThreatLevel threatLevel;
    private double botScore;
    private double highestSimilarityScore;
    private List<String> flaggedReasons;
    private List<String> extractedHashtags;
    private List<String> extractedMentions;
    private boolean isBotSuspect;
    private String matchedClusterNarrative;

    public AnalyzeResponse() {}

    public double getOverallManipulationScore() {
        return overallManipulationScore;
    }

    public void setOverallManipulationScore(double overallManipulationScore) {
        this.overallManipulationScore = overallManipulationScore;
    }

    public ThreatLevel getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(ThreatLevel threatLevel) {
        this.threatLevel = threatLevel;
    }

    public double getBotScore() {
        return botScore;
    }

    public void setBotScore(double botScore) {
        this.botScore = botScore;
    }

    public double getHighestSimilarityScore() {
        return highestSimilarityScore;
    }

    public void setHighestSimilarityScore(double highestSimilarityScore) {
        this.highestSimilarityScore = highestSimilarityScore;
    }

    public List<String> getFlaggedReasons() {
        return flaggedReasons;
    }

    public void setFlaggedReasons(List<String> flaggedReasons) {
        this.flaggedReasons = flaggedReasons;
    }

    public List<String> getExtractedHashtags() {
        return extractedHashtags;
    }

    public void setExtractedHashtags(List<String> extractedHashtags) {
        this.extractedHashtags = extractedHashtags;
    }

    public List<String> getExtractedMentions() {
        return extractedMentions;
    }

    public void setExtractedMentions(List<String> extractedMentions) {
        this.extractedMentions = extractedMentions;
    }

    public boolean isBotSuspect() {
        return isBotSuspect;
    }

    public void setBotSuspect(boolean botSuspect) {
        isBotSuspect = botSuspect;
    }

    public String getMatchedClusterNarrative() {
        return matchedClusterNarrative;
    }

    public void setMatchedClusterNarrative(String matchedClusterNarrative) {
        this.matchedClusterNarrative = matchedClusterNarrative;
    }
}
