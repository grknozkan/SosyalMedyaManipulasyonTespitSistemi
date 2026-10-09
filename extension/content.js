// AegisGuard Content Script (Twitter/X & Instagram)
(() => {
    console.log('[AegisGuard] Sosyal Medya Manipülasyon Kalkanı Aktif!');

    const BACKEND_URL = 'http://localhost:8080/api/analyze';
    const PROCESSED_ATTR = 'data-aegis-scanned';

    // Processes a single tweet element on Twitter/X
    async function scanTweetElement(tweetEl) {
        if (tweetEl.getAttribute(PROCESSED_ATTR)) return;
        tweetEl.setAttribute(PROCESSED_ATTR, 'true');

        // Extract Text
        const textEl = tweetEl.querySelector('[data-testid="tweetText"]');
        if (!textEl) return;
        const text = textEl.innerText.trim();
        if (!text || text.length < 5) return;

        // Extract Username & Handle
        const userHeader = tweetEl.querySelector('[data-testid="User-Name"]');
        let username = 'anon_user';
        if (userHeader) {
            const handleMatch = userHeader.innerText.match(/@(\w+)/);
            if (handleMatch) username = handleMatch[1];
        }

        // Check if avatar is default
        const avatarImg = tweetEl.querySelector('img[src*="profile_images"]');
        const defaultAvatar = !avatarImg || avatarImg.src.includes('default_profile');

        try {
            const response = await fetch(BACKEND_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    text: text,
                    username: username,
                    accountAgeDays: 30, // Estimator baseline
                    followerCount: 150,
                    followingCount: 300,
                    defaultAvatar: defaultAvatar
                })
            });

            if (response.ok) {
                const analysis = await response.json();
                injectBadge(tweetEl, analysis);
            }
        } catch (e) {
            // Server might be paused
        }
    }

    // Injects a visual security badge into the tweet
    function injectBadge(tweetEl, analysis) {
        const header = tweetEl.querySelector('[data-testid="User-Name"]') || tweetEl;
        if (!header) return;

        const badge = document.createElement('div');
        const score = Math.round(analysis.overallManipulationScore || 0);
        const level = (analysis.threatLevel || 'LOW').toLowerCase();

        badge.className = `aegis-badge aegis-${level}`;

        let label = `🛡️ AegisGuard: Güvenli (%${score})`;
        if (level === 'medium') label = `⚠️ AegisGuard: Şüpheli (%${score})`;
        else if (level === 'high') label = `🚨 AegisGuard: Yüksek Risk (%${score})`;
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

        header.parentNode.insertBefore(badge, header.nextSibling);
    }

    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Scan all currently visible tweets
    function scanAllVisible() {
        // Twitter/X tweets
        const tweets = document.querySelectorAll('article[data-testid="tweet"]');
        tweets.forEach(scanTweetElement);

        // Instagram comments or posts
        const instaPosts = document.querySelectorAll('article, div[role="dialog"] ul li');
        instaPosts.forEach(postEl => {
            if (postEl.getAttribute(PROCESSED_ATTR)) return;
            postEl.setAttribute(PROCESSED_ATTR, 'true');
            const textEl = postEl.querySelector('h1, span, p');
            if (textEl && textEl.innerText.length > 15) {
                // Can scan Instagram similarly
            }
        });
    }

    // Observe dynamic infinite-scroll feed
    let debounceTimer;
    const observer = new MutationObserver(() => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(scanAllVisible, 400);
    });

    observer.observe(document.body, { childList: true, subtree: true });

    // Initial scan
    setTimeout(scanAllVisible, 1200);
})();
