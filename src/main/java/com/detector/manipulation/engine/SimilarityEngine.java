package com.detector.manipulation.engine;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SimilarityEngine {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+|www\\.\\S+");
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^a-zA-Z0-9ğüşıöçĞÜŞİÖÇ\\s]");

    /**
     * Extracts hashtags from text
     */
    public List<String> extractHashtags(String text) {
        if (text == null) return Collections.emptyList();
        List<String> hashtags = new ArrayList<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(text);
        while (matcher.find()) {
            hashtags.add("#" + matcher.group(1).toLowerCase(Locale.ROOT));
        }
        return hashtags;
    }

    /**
     * Extracts mentions from text
     */
    public List<String> extractMentions(String text) {
        if (text == null) return Collections.emptyList();
        List<String> mentions = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            mentions.add("@" + matcher.group(1).toLowerCase(Locale.ROOT));
        }
        return mentions;
    }

    /**
     * Cleans text: lowercases, removes URLs, removes special characters, normalizes whitespace
     */
    public String normalizeText(String text) {
        if (text == null) return "";
        String withoutUrls = URL_PATTERN.matcher(text).replaceAll(" ");
        String cleaned = CLEAN_PATTERN.matcher(withoutUrls).replaceAll(" ");
        return cleaned.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    /**
     * Generates word set (unigrams)
     */
    public Set<String> generateWords(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) return Collections.emptySet();
        String[] words = normalizedText.split("\\s+");
        return new HashSet<>(Arrays.asList(words));
    }

    /**
     * Builds word-level n-grams (shingles)
     */
    public Set<String> generateWordNGrams(String normalizedText, int n) {
        if (normalizedText == null || normalizedText.isBlank()) return Collections.emptySet();
        String[] words = normalizedText.split("\\s+");
        if (words.length < n) {
            return Collections.singleton(normalizedText);
        }
        Set<String> shingles = new HashSet<>();
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(" ");
                sb.append(words[i + j]);
            }
            shingles.add(sb.toString());
        }
        return shingles;
    }

    /**
     * Builds character 3-grams (tri-grams) for robust subword/stem matching
     */
    public Set<String> generateCharTrigrams(String normalizedText) {
        String compact = normalizedText.replaceAll("\\s+", "");
        if (compact.length() < 3) return Collections.singleton(compact);
        Set<String> trigrams = new HashSet<>();
        for (int i = 0; i <= compact.length() - 3; i++) {
            trigrams.add(compact.substring(i, i + 3));
        }
        return trigrams;
    }

    private <T> double jaccard(Set<T> set1, Set<T> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        Set<T> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }

    /**
     * Calculates Jaccard similarity of word unigrams
     */
    public double calculateWordJaccard(String text1, String text2) {
        String norm1 = normalizeText(text1);
        String norm2 = normalizeText(text2);
        if (norm1.equals(norm2)) return 1.0;
        return jaccard(generateWords(norm1), generateWords(norm2));
    }

    /**
     * Calculates Jaccard similarity of word bigrams
     */
    public double calculateBigramJaccard(String text1, String text2) {
        String norm1 = normalizeText(text1);
        String norm2 = normalizeText(text2);
        if (norm1.equals(norm2)) return 1.0;
        return jaccard(generateWordNGrams(norm1, 2), generateWordNGrams(norm2, 2));
    }

    /**
     * Calculates Jaccard similarity of character trigrams
     */
    public double calculateCharTrigramJaccard(String text1, String text2) {
        String norm1 = normalizeText(text1);
        String norm2 = normalizeText(text2);
        if (norm1.equals(norm2)) return 1.0;
        return jaccard(generateCharTrigrams(norm1), generateCharTrigrams(norm2));
    }

    /**
     * Calculates Levenshtein Distance
     */
    public int calculateLevenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= s2.length(); j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            System.arraycopy(curr, 0, prev, 0, curr.length);
        }

        return prev[s2.length()];
    }

    /**
     * Normalized Levenshtein similarity ratio between 0.0 and 1.0
     */
    public double calculateLevenshteinSimilarity(String text1, String text2) {
        String norm1 = normalizeText(text1);
        String norm2 = normalizeText(text2);

        if (norm1.isEmpty() && norm2.isEmpty()) return 1.0;
        if (norm1.isEmpty() || norm2.isEmpty()) return 0.0;
        if (norm1.equals(norm2)) return 1.0;

        int maxLen = Math.max(norm1.length(), norm2.length());
        if (maxLen == 0) return 1.0;

        int distance = calculateLevenshteinDistance(norm1, norm2);
        return Math.max(0.0, 1.0 - ((double) distance / maxLen));
    }

    /**
     * Multi-layered composite similarity score:
     * Combines word overlap (unigrams), phrase overlap (bigrams), subword patterns (trigrams), and string edit distance.
     */
    public double calculateOverallSimilarity(String text1, String text2) {
        String norm1 = normalizeText(text1);
        String norm2 = normalizeText(text2);
        if (norm1.equals(norm2)) return 1.0;

        double unigram = calculateWordJaccard(text1, text2);
        double bigram = calculateBigramJaccard(text1, text2);
        double trigram = calculateCharTrigramJaccard(text1, text2);
        double levenshtein = calculateLevenshteinSimilarity(text1, text2);

        // Weighted combination: emphasizes word content and phrasing while resilient to slight variations
        return (unigram * 0.35) + (bigram * 0.25) + (trigram * 0.20) + (levenshtein * 0.20);
    }
}
