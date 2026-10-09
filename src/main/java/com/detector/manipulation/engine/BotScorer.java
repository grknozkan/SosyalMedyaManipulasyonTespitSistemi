package com.detector.manipulation.engine;

import com.detector.manipulation.model.AccountRiskProfile;
import com.detector.manipulation.model.SocialPost;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class BotScorer {

    private static final Pattern NUMERIC_SUFFIX_PATTERN = Pattern.compile(".*\\d{5,}$");

    /**
     * Evaluates account metadata and posting behavior to return a bot score between 0.0 and 100.0
     */
    public BotScoreResult evaluateAccount(SocialPost post, AccountRiskProfile profile) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        int followers = post.getAuthorFollowers();
        int following = post.getAuthorFollowing();
        int ageDays = post.getAuthorAccountAgeDays();
        boolean defaultAvatar = post.isHasDefaultAvatar();
        String username = post.getAuthorUsername();

        // 1. Follower to Following Ratio Analysis
        if (following > 200) {
            double ratio = (double) followers / following;
            if (ratio < 0.02) { // e.g., 4 followers to 500 following
                score += 30.0;
                reasons.add("Aşırı düşük Takipçi/Takip oranı (" + followers + " takipçi / " + following + " takip)");
            } else if (ratio < 0.1) {
                score += 15.0;
                reasons.add("Şüpheli Takipçi/Takip dengesizliği (Oran < %10)");
            }
        } else if (followers == 0 && following > 50) {
            score += 25.0;
            reasons.add("Sıfır takipçili agresif takipçi profili");
        }

        // 2. Account Age Analysis
        if (ageDays <= 3) {
            score += 30.0;
            reasons.add("Çok yeni hesap (" + ageDays + " günlük hesap)");
        } else if (ageDays <= 14) {
            score += 20.0;
            reasons.add("Yeni açılmış hesap (" + ageDays + " günlük)");
        } else if (ageDays <= 45) {
            score += 10.0;
            reasons.add("1 aydan genç hesap (" + ageDays + " günlük)");
        }

        // 3. Default Avatar Indicator
        if (defaultAvatar) {
            score += 15.0;
            reasons.add("Varsayılan profil fotoğrafı (Avatar yüklenmemiş)");
        }

        // 4. Algorithmic / Random Username Pattern (e.g. user_984321)
        if (username != null && NUMERIC_SUFFIX_PATTERN.matcher(username).matches()) {
            score += 15.0;
            reasons.add("Otomatik oluşturulmuş şüpheli kullanıcı adı (5+ rastgele rakam)");
        }

        // 5. Historical Activity Factors from Profile (if exists)
        if (profile != null) {
            if (profile.getFlaggedPosts() > 2) {
                score += Math.min(25.0, profile.getFlaggedPosts() * 5.0);
                reasons.add("Geçmişte tekrarlanan manipülasyon bayrakları (" + profile.getFlaggedPosts() + " ihlal)");
            }
        }

        double normalizedScore = Math.min(100.0, Math.max(0.0, score));
        return new BotScoreResult(normalizedScore, reasons);
    }

    public record BotScoreResult(double score, List<String> reasons) {}
}
