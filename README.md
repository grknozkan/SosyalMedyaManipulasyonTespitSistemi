# AegisGuard - Sosyal Medya Manipülasyon, Astroturfing ve Botnet Tespit Sistemi

Modern sosyal medya ağlarında **Koordineli Gerçek Dışı Davranış (CIB - Coordinated Inauthentic Behavior)**, organize astroturfing kampanyaları ve otomatik bot ordularını tespit etmek için geliştirilmiş **Java 21** ve **Spring Boot 3.3.4** tabanlı gerçek zamanlı savunma ve analiz motoru.

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

## 🚀 Sistemi Çalıştırma

Sunucu şu anda `http://localhost:8080` adresinde arka planda çalışmaktadır.

Projeyi sıfırdan terminalden başlatmak isterseniz:
```powershell
cd "C:\Users\Gürkan\.gemini\antigravity-ide\scratch\social-manipulation-detector"
.\mvnw.cmd spring-boot:run
```
veya derlenmiş JAR ile:
```powershell
java -jar target\social-manipulation-detector-0.0.1-SNAPSHOT.jar
```

Arayüze erişim:
Tarayıcınızda açın: **[http://localhost:8080](http://localhost:8080)**

---

## 📡 REST API & SSE Akışı

| Metot | Uç Nokta | Açıklama |
|---|---|---|
| `GET` | `/api/metrics` | Canlı tehdit indeksi ve genel istatistikler |
| `GET` | `/api/posts?limit=50` | İncelenen son gönderiler (Tümü / Şüpheliler / Kritik) |
| `GET` | `/api/clusters` | Tespit edilen koordineli CIB kampanya kümeleri |
| `GET` | `/api/accounts/flagged` | Yüksek riskli bot profilleri |
| `POST` | `/api/analyze` | Anlık gönderi analiz testi (Kullanıcı girdisi) |
| `POST` | `/api/simulation/attack?scenario=CRYPTO_PUMP` | Kripto pump botnet saldırısı tetikler |
| `POST` | `/api/simulation/attack?scenario=ASTROTURFING_BOYCOTT` | Sahte boykot astroturfing saldırısı tetikler |
| `POST` | `/api/simulation/attack?scenario=DISINFO_VIRAL` | Dezenformasyon söylentisi simülasyonu |
| `POST` | `/api/simulation/single-organic` | 1 adet doğal organik paylaşım üretir |
| `POST` | `/api/simulation/start?interval=1500` | Sürekli otomatik veri akışını başlatır |
| `POST` | `/api/simulation/stop` | Otomatik akışı durdurur |
| `POST` | `/api/clear` | Tüm geçmişi ve sayaçları sıfırlar |
| `GET` | `/api/stream/live` | Server-Sent Events (SSE) ile anlık canlı push akışı |
