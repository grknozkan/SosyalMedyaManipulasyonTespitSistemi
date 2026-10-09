// AegisGuard Client Application
document.addEventListener('DOMContentLoaded', () => {
    let currentFilter = 'all';
    let isAutoStreaming = false;
    let eventSource = null;
    let localPosts = [];

    // DOM Elements
    const threatIndexVal = document.getElementById('threatIndexVal');
    const threatGaugeCircle = document.getElementById('threatGaugeCircle');
    const threatStatusTag = document.getElementById('threatStatusTag');
    const totalPostsVal = document.getElementById('totalPostsVal');
    const flaggedCountVal = document.getElementById('flaggedCountVal');
    const activeClustersVal = document.getElementById('activeClustersVal');
    const botAccountsVal = document.getElementById('botAccountsVal');
    const manipulationRatioVal = document.getElementById('manipulationRatioVal');

    const feedListContainer = document.getElementById('feedListContainer');
    const emptyStatePlaceholder = document.getElementById('emptyStatePlaceholder');
    const feedCounterBadge = document.getElementById('feedCounterBadge');
    const filterTabs = document.querySelectorAll('.filter-tab');

    const clustersListContainer = document.getElementById('clustersListContainer');
    const botsListContainer = document.getElementById('botsListContainer');

    const btnAttackCrypto = document.getElementById('btnAttackCrypto');
    const btnAttackBoycott = document.getElementById('btnAttackBoycott');
    const btnAttackDisinfo = document.getElementById('btnAttackDisinfo');
    const btnSingleOrganic = document.getElementById('btnSingleOrganic');
    const btnToggleAutoStream = document.getElementById('btnToggleAutoStream');
    const autoStreamBtnLabel = document.getElementById('autoStreamBtnLabel');
    const btnClearData = document.getElementById('btnClearData');

    const inspectorForm = document.getElementById('inspectorForm');
    const inspectorResultBox = document.getElementById('inspectorResultBox');
    const inspectThreatBadge = document.getElementById('inspectThreatBadge');
    const inspectScoreBarFill = document.getElementById('inspectScoreBarFill');
    const inspectScoreVal = document.getElementById('inspectScoreVal');
    const inspectSimVal = document.getElementById('inspectSimVal');
    const inspectReasonsList = document.getElementById('inspectReasonsList');

    // 1. Initialize SSE Connection
    function initSse() {
        if (eventSource) {
            eventSource.close();
        }

        eventSource = new EventSource('/api/stream/live');

        eventSource.addEventListener('init', (e) => {
            const metrics = JSON.parse(e.data);
            updateMetrics(metrics);
        });

        eventSource.addEventListener('new-post', (e) => {
            const post = JSON.parse(e.data);
            handleNewPost(post);
        });

        eventSource.addEventListener('system-reset', () => {
            localPosts = [];
            renderFeed();
            fetchMetrics();
            fetchClusters();
            fetchFlaggedBots();
        });

        eventSource.onerror = () => {
            console.warn('SSE bağlantısı kesildi, yeniden bağlanılıyor...');
            // Fallback polling will handle data in the meantime
        };
    }

    // 2. Fetch Initial State
    async function fetchInitialData() {
        await Promise.all([
            fetchMetrics(),
            fetchPosts(),
            fetchClusters(),
            fetchFlaggedBots()
        ]);
    }

    async function fetchMetrics() {
        try {
            const res = await fetch('/api/metrics');
            if (res.ok) {
                const metrics = await res.json();
                updateMetrics(metrics);
            }
        } catch (err) {
            console.error('Metrics fetch error:', err);
        }
    }

    async function fetchPosts() {
        try {
            const res = await fetch('/api/posts?limit=50');
            if (res.ok) {
                localPosts = await res.json();
                renderFeed();
            }
        } catch (err) {
            console.error('Posts fetch error:', err);
        }
    }

    async function fetchClusters() {
        try {
            const res = await fetch('/api/clusters');
            if (res.ok) {
                const clusters = await res.json();
                renderClusters(clusters);
            }
        } catch (err) {
            console.error('Clusters fetch error:', err);
        }
    }

    async function fetchFlaggedBots() {
        try {
            const res = await fetch('/api/accounts/flagged');
            if (res.ok) {
                const bots = await res.json();
                renderBots(bots);
            }
        } catch (err) {
            console.error('Bots fetch error:', err);
        }
    }

    // 3. UI Update Helpers
    function updateMetrics(metrics) {
        if (!metrics) return;

        totalPostsVal.textContent = metrics.totalAnalyzedPosts || 0;
        flaggedCountVal.textContent = metrics.flaggedPostsCount || 0;
        activeClustersVal.textContent = metrics.activeClustersCount || 0;
        botAccountsVal.textContent = metrics.identifiedBotAccounts || 0;
        manipulationRatioVal.textContent = `%${metrics.manipulationRatioPercent || '0.0'}`;

        const score = Math.round(metrics.averageThreatScore || 0);
        threatIndexVal.textContent = score;

        // Gauge update: circumference = 2 * PI * 68 ≈ 427
        const circumference = 427;
        const offset = circumference - (circumference * score / 100);
        threatGaugeCircle.style.strokeDashoffset = Math.max(0, offset);

        if (score < 30) {
            threatGaugeCircle.style.stroke = 'var(--color-emerald)';
            threatStatusTag.textContent = 'GÜVENLİ';
            threatStatusTag.style.background = 'rgba(16, 185, 129, 0.15)';
            threatStatusTag.style.color = 'var(--color-emerald)';
        } else if (score < 55) {
            threatGaugeCircle.style.stroke = 'var(--color-amber)';
            threatStatusTag.textContent = 'ŞÜPHELİ';
            threatStatusTag.style.background = 'rgba(245, 158, 11, 0.15)';
            threatStatusTag.style.color = 'var(--color-amber)';
        } else if (score < 75) {
            threatGaugeCircle.style.stroke = 'var(--color-orange)';
            threatStatusTag.textContent = 'YÜKSEK RİSK';
            threatStatusTag.style.background = 'rgba(249, 115, 22, 0.15)';
            threatStatusTag.style.color = 'var(--color-orange)';
        } else {
            threatGaugeCircle.style.stroke = 'var(--color-red)';
            threatStatusTag.textContent = 'KRİTİK SALDIRI';
            threatStatusTag.style.background = 'rgba(239, 68, 68, 0.15)';
            threatStatusTag.style.color = 'var(--color-red)';
        }
    }

    function handleNewPost(post) {
        // Add to front of array
        localPosts.unshift(post);
        if (localPosts.length > 80) localPosts.pop();

        renderFeed();
        fetchMetrics();
        fetchClusters();
        fetchFlaggedBots();
    }

    function renderFeed() {
        feedCounterBadge.textContent = `${localPosts.length} Gönderi`;

        let filtered = localPosts;
        if (currentFilter === 'flagged') {
            filtered = localPosts.filter(p => p.threatLevel !== 'LOW');
        } else if (currentFilter === 'high') {
            filtered = localPosts.filter(p => p.threatLevel === 'HIGH' || p.threatLevel === 'CRITICAL');
        }

        if (filtered.length === 0) {
            feedListContainer.innerHTML = `
                <div class="feed-empty-state">
                    <div class="empty-icon">📡</div>
                    <p>Filtreye uygun gönderi bulunamadı.</p>
                </div>
            `;
            return;
        }

        feedListContainer.innerHTML = filtered.map(post => {
            const threatClass = `threat-${post.threatLevel ? post.threatLevel.toLowerCase() : 'low'}`;
            const badgeClass = `badge-${post.threatLevel ? post.threatLevel.toLowerCase() : 'low'}`;
            const threatLabel = getThreatLabel(post.threatLevel);

            const initial = (post.authorUsername || 'U').charAt(0).toUpperCase();
            const defaultAvatarClass = post.hasDefaultAvatar ? 'default-avatar' : '';

            const reasonsHtml = (post.flagReasons || []).map(r => `
                <span class="reason-tag ${r.includes('CIB') ? 'cluster-tag' : ''}">
                    ${r.includes('CIB') ? '🕸️' : '⚠️'} ${escapeHtml(r)}
                </span>
            `).join('');

            const timeStr = post.timestamp ? new Date(post.timestamp).toLocaleTimeString('tr-TR') : 'Şimdi';

            return `
                <article class="feed-item ${threatClass}">
                    <div class="feed-item-top">
                        <div class="feed-author-box">
                            <div class="author-avatar ${defaultAvatarClass}">${initial}</div>
                            <div class="author-info">
                                <strong>@${escapeHtml(post.authorUsername || 'anon')}</strong>
                                <span class="author-meta">
                                    ${post.authorFollowers} takipçi • ${post.authorFollowing} takip • ${post.authorAccountAgeDays} günlük
                                </span>
                            </div>
                        </div>
                        <div style="display: flex; align-items: center; gap: 0.6rem;">
                            <span style="font-size: 0.72rem; color: var(--text-muted); font-family: var(--font-mono);">${timeStr}</span>
                            <span class="threat-badge ${badgeClass}">${threatLabel} (%${Math.round(post.manipulationScore || 0)})</span>
                        </div>
                    </div>
                    <div class="feed-content-text">${escapeHtml(post.content || '')}</div>
                    ${reasonsHtml ? `<div class="feed-reasons-box">${reasonsHtml}</div>` : ''}
                </article>
            `;
        }).join('');
    }

    function renderClusters(clusters) {
        if (!clusters || clusters.length === 0) {
            clustersListContainer.innerHTML = '<div class="empty-clusters-text">Aktif koordineli botnet kümesi yok.</div>';
            return;
        }

        clustersListContainer.innerHTML = clusters.map(c => {
            const accountsSnippet = Array.from(c.accounts || []).slice(0, 3).map(a => `@${escapeHtml(a)}`).join(', ');
            const remainingCount = (c.accountCount || 0) - 3;
            const extra = remainingCount > 0 ? ` +${remainingCount} bot` : '';

            return `
                <div class="cluster-item">
                    <div class="cluster-top">
                        <span class="cluster-title">${escapeHtml(c.campaignName)}</span>
                        <span class="threat-badge badge-${c.severity ? c.severity.toLowerCase() : 'medium'}">
                            ${c.accountCount} Bot Hesap
                        </span>
                    </div>
                    <div class="cluster-narrative">"${escapeHtml(c.coreNarrative || '')}"</div>
                    <div class="cluster-meta">
                        <span>Botlar: ${accountsSnippet}${extra}</span>
                        <span>Ort. Benzerlik: %${Math.round((c.averageSimilarity || 0) * 100)}</span>
                    </div>
                </div>
            `;
        }).join('');
    }

    function renderBots(bots) {
        if (!bots || bots.length === 0) {
            botsListContainer.innerHTML = '<div class="empty-clusters-text">Bayraklanan bot hesabı bulunmuyor.</div>';
            return;
        }

        botsListContainer.innerHTML = bots.slice(0, 6).map(b => {
            return `
                <div class="bot-item">
                    <div>
                        <div class="bot-username">@${escapeHtml(b.username)}</div>
                        <div class="bot-meta">${b.accountAgeDays} günlük • ${b.flaggedPosts} şüpheli paylaşım</div>
                    </div>
                    <span class="bot-score-pill">Bot Skoru: ${Math.round(b.botScore)}</span>
                </div>
            `;
        }).join('');
    }

    function getThreatLabel(level) {
        switch (level) {
            case 'CRITICAL': return 'KRİTİK TEHDİT';
            case 'HIGH': return 'YÜKSEK RİSK';
            case 'MEDIUM': return 'ŞÜPHELİ';
            default: return 'GÜVENLİ';
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // 4. Filter Tab Click Handlers
    filterTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            filterTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            currentFilter = tab.dataset.filter;
            renderFeed();
        });
    });

    // 5. Action Buttons (Simulations)
    async function triggerAttack(scenario) {
        try {
            const res = await fetch(`/api/simulation/attack?scenario=${scenario}`, { method: 'POST' });
            if (res.ok) {
                // Instantly re-fetch
                setTimeout(fetchInitialData, 300);
            }
        } catch (err) {
            console.error('Attack trigger failed:', err);
        }
    }

    btnAttackCrypto.addEventListener('click', () => triggerAttack('CRYPTO_PUMP'));
    btnAttackBoycott.addEventListener('click', () => triggerAttack('ASTROTURFING_BOYCOTT'));
    btnAttackDisinfo.addEventListener('click', () => triggerAttack('DISINFO_VIRAL'));

    btnSingleOrganic.addEventListener('click', async () => {
        try {
            await fetch('/api/simulation/single-organic', { method: 'POST' });
            setTimeout(fetchInitialData, 200);
        } catch (err) {
            console.error('Single organic failed:', err);
        }
    });

    btnToggleAutoStream.addEventListener('click', async () => {
        if (!isAutoStreaming) {
            await fetch('/api/simulation/start?interval=1500', { method: 'POST' });
            isAutoStreaming = true;
            autoStreamBtnLabel.textContent = 'Akışı Duraklat';
            btnToggleAutoStream.classList.remove('btn-secondary');
            btnToggleAutoStream.classList.add('btn-primary');
        } else {
            await fetch('/api/simulation/stop', { method: 'POST' });
            isAutoStreaming = false;
            autoStreamBtnLabel.textContent = 'Otomatik Akışı Başlat';
            btnToggleAutoStream.classList.remove('btn-primary');
            btnToggleAutoStream.classList.add('btn-secondary');
        }
    });

    btnClearData.addEventListener('click', async () => {
        if (confirm('Tüm analiz edilmiş gönderiler ve botnet verileri sıfırlansın mı?')) {
            await fetch('/api/clear', { method: 'POST' });
            localPosts = [];
            renderFeed();
            fetchInitialData();
        }
    });

    // 6. Manual Post Inspector Form
    inspectorForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const text = document.getElementById('inspectText').value.trim();
        const username = document.getElementById('inspectUsername').value.trim();
        const age = parseInt(document.getElementById('inspectAge').value, 10);
        const followers = parseInt(document.getElementById('inspectFollowers').value, 10);
        const following = parseInt(document.getElementById('inspectFollowing').value, 10);
        const defaultAvatar = document.getElementById('inspectDefaultAvatar').checked;

        if (!text) return;

        try {
            const res = await fetch('/api/analyze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    text: text,
                    username: username,
                    accountAgeDays: age,
                    followerCount: followers,
                    followingCount: following,
                    defaultAvatar: defaultAvatar
                })
            });

            if (res.ok) {
                const data = await res.json();
                displayInspectorResult(data);
                fetchInitialData(); // update clusters/counters if it matched
            }
        } catch (err) {
            console.error('Inspector error:', err);
        }
    });

    function displayInspectorResult(data) {
        inspectorResultBox.style.display = 'block';

        const score = Math.round(data.overallManipulationScore || 0);
        inspectScoreVal.textContent = `${score}/100`;
        inspectSimVal.textContent = `%${Math.round((data.highestSimilarityScore || 0) * 100)}`;
        inspectScoreBarFill.style.width = `${Math.min(100, Math.max(5, score))}%`;

        inspectThreatBadge.className = `threat-badge badge-${data.threatLevel ? data.threatLevel.toLowerCase() : 'low'}`;
        inspectThreatBadge.textContent = getThreatLabel(data.threatLevel);

        if (data.flaggedReasons && data.flaggedReasons.length > 0) {
            inspectReasonsList.innerHTML = data.flaggedReasons.map(r => `<li>${escapeHtml(r)}</li>`).join('');
        } else {
            inspectReasonsList.innerHTML = '<li style="color: var(--color-emerald)">Herhangi bir manipülasyon veya botnet şüphesi tespit edilmedi. Gönderi organik görünüyor.</li>';
        }
    }

    // 7. Periodic Sync Loop (Every 4s)
    setInterval(() => {
        fetchMetrics();
        fetchClusters();
        fetchFlaggedBots();
    }, 4000);

    // Initial setup
    initSse();
    fetchInitialData();
});
