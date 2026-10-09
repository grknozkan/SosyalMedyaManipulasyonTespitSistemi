// AegisGuard Background Service Worker (Bypasses Mixed Content / HTTPS restrictions)
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === 'analyzePost') {
        fetch('http://localhost:8080/api/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request.payload)
        })
        .then(response => {
            if (!response.ok) throw new Error('HTTP ' + response.status);
            return response.json();
        })
        .then(data => {
            sendResponse({ success: true, data: data });
        })
        .catch(err => {
            console.warn('[AegisGuard Service Worker] Analiz hatası:', err);
            sendResponse({ success: false, error: err.message });
        });

        return true; // Asenkron yanıt için kanal açık tutulur
    }
});
