# AegisGuard - Sosyal Medya Manipülasyon, Astroturfing ve Botnet Tespit Sistemi

Modern sosyal medya ağlarında **Koordineli Gerçek Dışı Davranış (CIB - Coordinated Inauthentic Behavior)**, organize astroturfing kampanyaları ve otomatik bot ordularını tespit etmek için geliştirilmiş **Java 21**, **Spring Boot 3.3.4** ve **Chrome Extension (Manifest V3)** tabanlı gerçek zamanlı savunma ve analiz motoru.

---

## 🏛️ Mimari Katmanlar ve Algoritmik Altyapı

### 1. Metin Benzerliği & Copypasta Algoritmaları (`SimilarityEngine`)
- **Çok Katmanlı Jaccard Benzerliği:**
  - **Kelime Düzeyi (Unigram Jaccard):** Farklı hesaplar arasındaki ortak kelime kümesi örtüşmesi.
  - **Kelime Çiftleri (Bigram Shingles):** Cümle kalıbı ve ardışık ifade benzerliği.
  - **Karakter Trigramları (Char 3-grams):** Türkçe gibi eklemeli dillerde çekim ekleri ve harf sapmalarına dayanıklı kök/gövde eşleştirme.
- **Dinamik Programlama ile Levenshtein Oranı:** Karakter düzeyinde filtre atlatma amaçlı yazım hatalarını yakalar.
- **Ağırlıklı Kompozit Skor:** `%35 Kelime + %25 Şingle + %20 Trigram + %20 Levenshtein`

### 2. Zamansal Anomali & Frekans Patlaması (`TemporalBurstDetector`)
- 60 saniyelik kayan zaman penceresi (Sliding Time Window).
- Belirli etiket (#hashtag) veya temalarda ani frekans yükselişlerini (Spike / Burst) gerçek zamanlı tespit eder.

### 3. Davranışsal Hesap Profilleme & Bot Skoru (`BotScorer`)
- Takipçi/Takip oranı anomalisi (ör. 0 takipçi, 700 takip).
- Hesap yaşı analizi (özellikle 1-7 günlük yeni açılmış hesaplar).
- Varsayılan profil fotoğrafı (varsayılan avatar tespiti).
- Algoritmik/Rastgele oluşturulmuş kullanıcı adı örüntüsü (sonunda 5+ rastgele rakam).

### 4. Koordineli Kampanya Kümeleme (`ManipulationDetectionEngine`)
- Farklı hesaplar kısa zaman aralığında aynı söylemi (%50+ benzerlik) paylaştığında otomatik olarak **CIB (Coordinated Cluster)** oluşturulur ve hesaplar aynı kümede ilişkilendirilir.
- Tehdit Seviyeleri: **DÜŞÜK**, **ORTA**, **YÜKSEK**, **KRİTİK**.

---

## 🧩 Chrome Eklentisi Kurulumu (Twitter/X & Instagram Canlı Koruma)

Proje içerisinde yer alan `extension/` klasörü, sıfır maliyetle ve hiçbir API anahtarı gerektirmeden gerçek Twitter/X ve Instagram akışındaki gönderileri anında denetlemenizi sağlar.

1. **Google Chrome, Brave veya Microsoft Edge** tarayıcınızı açın.
2. Adres çubuğuna gidin:
   ```
   chrome://extensions
   ```
3. Sağ üst köşedeki **Geliştirici modu (Developer Mode)** anahtarını aktif edin.
4. Sol üstteki **Paketlenmemiş öğe yükle (Load unpacked)** butonuna tıklayın.
5. Proje dizinindeki `extension` klasörünü seçin:
   ```
   .../social-manipulation-detector/extension
   ```
6. **x.com (Twitter)** veya **instagram.com** sayfasını açıp yenileyin (F5).
7. Tweet'lerin ve gönderilerin hemen altında canlı **AegisGuard Güvenlik Rozetleri** belirecektir:
   - `[🛡️ AegisGuard: GÜVENLİ]`
   - `[🚨 AegisGuard: KRİTİK MANİPÜLASYON (%88) - Bot Şüphesi / Copypasta]`

---

## 🚀 Java Backend Sunucusunu Çalıştırma

Sunucuyu terminalden başlatmak için:
```powershell
.\mvnw.cmd spring-boot:run
```
veya paketlenmiş JAR dosyasıyla:
```powershell
java -jar target/social-manipulation-detector-0.0.1-SNAPSHOT.jar
```

AegisGuard Canlı Kontrol Paneline erişim:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🐍 Terminal Toplayıcısı (`collector.py`)

Harici bir web tarayıcısı açmadan, konsol üzerinden kopyalanan herhangi bir gönderiyi test etmek için:
```bash
python collector.py
```

---

## 📡 REST API & SSE Akış Dokümantasyonu

| Metot | Uç Nokta | Açıklama |
|---|---|---|
| `GET` | `/api/metrics` | Canlı tehdit indeksi ve genel istatistikler |
| `GET` | `/api/posts?limit=50` | İncelenen son gönderiler (Tümü / Şüpheliler / Kritik) |
| `GET` | `/api/clusters` | Tespit edilen koordineli CIB kampanya kümeleri |
| `GET` | `/api/accounts/flagged` | Yüksek riskli bot profilleri |
| `POST` | `/api/analyze` | Anlık gönderi analiz testi (Kullanıcı girdisi) |
| `POST` | `/api/post` | Dış sistemlerden (Eklenti / Scraper) gönderi kabulü |
| `POST` | `/api/simulation/attack?scenario=CRYPTO_PUMP` | Kripto pump botnet saldırısı simülasyonu |
| `POST` | `/api/simulation/attack?scenario=ASTROTURFING_BOYCOTT` | Sahte boykot astroturfing saldırısı simülasyonu |
| `POST` | `/api/simulation/attack?scenario=DISINFO_VIRAL` | Dezenformasyon söylentisi simülasyonu |
| `POST` | `/api/simulation/single-organic` | 1 adet doğal organik kullanıcı gönderisi üretir |
| `POST` | `/api/simulation/start?interval=1500` | Sürekli otomatik veri akışını başlatır |
| `POST` | `/api/simulation/stop` | Otomatik akışı durdurur |
| `POST` | `/api/clear` | Tüm geçmişi ve sayaçları sıfırlar |
| `GET` | `/api/stream/live` | Server-Sent Events (SSE) ile anlık canlı push akışı |
