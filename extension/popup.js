document.addEventListener('DOMContentLoaded', async () => {
    const serverStatus = document.getElementById('serverStatus');
    const statusText = document.getElementById('statusText');
    const inspectedCount = document.getElementById('inspectedCount');
    const botCount = document.getElementById('botCount');

    try {
        const res = await fetch('http://localhost:8080/api/metrics');
        if (res.ok) {
            const data = await res.json();
            statusText.textContent = 'Java Sunucusu Aktif (Port 8080)';
            serverStatus.classList.remove('offline');
            inspectedCount.textContent = data.totalAnalyzedPosts || 0;
            botCount.textContent = data.flaggedPostsCount || 0;
        } else {
            throw new Error('Server returned non-200');
        }
    } catch (e) {
        serverStatus.classList.add('offline');
        statusText.textContent = 'Java Sunucusu Kapalı (localhost:8080)';
    }
});
