// AegisGuard Content Script (Twitter/X & Instagram)
(() => {
    console.log('%c[AegisGuard] Sosyal Medya Manipülasyon Kalkanı Aktif!', 'background: #0284c7; color: #fff; font-weight: bold; padding: 4px 8px; border-radius: 4px;');

    const PROCESSED_ATTR = 'data-aegis-scanned';

    // Processes a single tweet element on Twitter/X
    function scanTweetElement(tweetEl) {
        if (tweetEl.getAttribute(PROCESSED_ATTR)) return;
        tweetEl.setAttribute(PROCESSED_ATTR, 'true');

        // Extract Text
        let textEl = tweetEl.querySelector('[data-testid="tweetText"]');
        if (!textEl) {
            textEl = tweetEl.querySelector('div[dir="auto"][lang]');
        }
        if (!textEl) return;

        const text = textEl.innerText.trim();
        if (!text || text.length < 5) return;

        // Extract Username & Handle
        const userHeader = tweetEl.querySelector('[data-testid="User-Name"]');
        let username = 'anon_user';
        if (userHeader) {
            const handleMatch = userHeader.innerText.match(/@([a-zA-Z0-9_]+)/);
            if (handleMatch) username = handleMatch[1];
        }

        // Avatar check
        const avatarImg = tweetEl.querySelector('img[src*="profile_images"]');
        const defaultAvatar = !avatarImg || avatarImg.src.includes('default_profile');

        const payload = {
            text: text,
            username: username,
            accountAgeDays: 30,
            followerCount: 150,
            followingCount: 300,
            defaultAvatar: defaultAvatar
        };

        // Send via background service worker to bypass mixed content
        try {
            chrome.runtime.sendMessage({ action: 'analyzePost', payload: payload }, (response) => {
                if (chrome.runtime.lastError) {
                    console.debug('[AegisGuard] Mesajlaşma uyarısı:', chrome.runtime.lastError.message);
                    return;
                }
                if (response && response.success && response.data) {
                    console.log(`[AegisGuard] Tweet Analiz Edildi: @${username} -> Tehdit: ${response.data.threatLevel} (%${Math.round(response.data.overallManipulationScore || 0)})`);
                    injectBadge(tweetEl, textEl, userHeader, response.data);
                }
            });
        } catch (err) {
            console.debug('[AegisGuard] Extension bağlamı hatası:', err);
        }
    }

    // Injects a visual security badge into the tweet
    function injectBadge(tweetEl, textEl, userHeader, analysis) {
        if (tweetEl.querySelector('.aegis-badge')) return;

        const badge = document.createElement('div');
        const score = Math.round(analysis.overallManipulationScore || 0);
        const level = (analysis.threatLevel || 'LOW').toLowerCase();

        badge.className = `aegis-badge aegis-${level}`;

        let label = `🛡️ AegisGuard: GÜVENLİ (%${score})`;
        if (level === 'medium') label = `⚠️ AegisGuard: ŞÜPHELİ (%${score})`;
        else if (level === 'high') label = `🚨 AegisGuard: YÜKSEK RİSK (%${score})`;
        else if (level === 'critical') label = `⛔ AegisGuard: KRİTİK MANİPÜLASYON (%${score})`;

        badge.innerHTML = `
            <span class="aegis-label">${label}</span>
            ${analysis.flaggedReasons && analysis.flaggedReasons.length > 0 ? `
                <div class="aegis-tooltip">
                    <strong>Manipülasyon Bayrakları:</strong>
                    <ul>${analysis.flaggedReasons.map(r => `<li>${escapeHtml(r)}</li>`).join('')}</ul>
                </div>
            ` : ''}
        `;

        // Insert right above tweet text or below user header
        if (userHeader && userHeader.parentNode) {
            userHeader.parentNode.insertBefore(badge, userHeader.nextSibling);
        } else if (textEl && textEl.parentNode) {
            textEl.parentNode.insertBefore(badge, textEl);
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Scan all currently visible tweets
    function scanAllVisible() {
        const tweets = document.querySelectorAll('article[data-testid="tweet"]');
        tweets.forEach(scanTweetElement);
    }

    // Observe dynamic infinite-scroll feed
    let debounceTimer;
    const observer = new MutationObserver(() => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(scanAllVisible, 300);
    });

    observer.observe(document.body, { childList: true, subtree: true });

    // Initial scan and retries for dynamic page loading
    scanAllVisible();
    setTimeout(scanAllVisible, 1000);
    setTimeout(scanAllVisible, 2500);
})();
