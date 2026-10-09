package com.detector.manipulation.service;

import com.detector.manipulation.model.SocialPost;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SimulationService {

    private final DetectionService detectionService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> simulationTask;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int intervalMillis = 1500; // default 1.5s per post

    private final Random random = new Random();

    // Organic content pool
    private final List<String> organicAuthors = List.of(
            "deniz_kaya", "ayse_yilmaz", "mehmet_dev", "elif_tech", "can_software",
            "zeynep_art", "burak_explorer", "selin_reads", "ali_travels", "gamze_code"
    );

    private final List<String> organicPosts = List.of(
            "Bugün Java 21 Virtual Threads ile harika bir benchmark testi yaptım, performans gerçekten muazzam!",
            "Hafta sonu için Kadıköy'de güzel bir kahveci arıyorum, önerisi olan var mı? #İstanbul #Kahve",
            "Yapay zeka modellerinin son versiyonları gerçekten kod yazma hızını ikiye katlıyor.",
            "Sonbahar havası nihayet geldi, serin akşam yürüyüşleri gibisi yok. #Ekim #Sonbahar",
            "Yeni bir açık kaynak projeye başladım, ilk pull request'leri kabul etmek harika bir duygu!",
            "Trafik bugün inanılmaz yoğun, toplu taşıma her zamanki gibi kurtarıcı oldu.",
            "Microservices mimarisinde event-driven tasarım gerçekten scalability için vazgeçilmez.",
            "Okuduğum son bilim kurgu kitabı distopik gelecekleri o kadar iyi özetlemiş ki hayran kaldım.",
            "Websocket ile canlı veri senkronizasyonu kurmak REST polling'e göre hem daha zarif hem çok daha hafif.",
            "Yeni bir dil öğrenirken syntax'tan çok o dilin düşünce yapısını kavramak gerekiyor."
    );

    // Attack Scenarios
    private final List<String> cryptoBots = List.of(
            "crypto_king84920", "moon_hunter11029", "token_bull99301", "sol_gem88392",
            "whale_alert00293", "defi_insider77492", "x100_seeker55482", "crypto_shark12948"
    );

    private final List<String> cryptoTemplates = List.of(
            "ACİL DUYURU! $SOLAR coin büyük borsalarda listeleniyor, en az 100x yapacak sakın kaçırmayın! #SolarMoon #CryptoPump #Binance",
            "Son dakika! $SOLAR coin listeleniyor, 100x yapacak fırsatı hemen değerlendirin kaçırmayın! #SolarMoon #CryptoPump",
            "Büyük fırsat ayağınıza geldi! $SOLAR token bu gece patlıyor, 100x yapacak kaçırmayın! #SolarMoon #AltcoinGems",
            "Kaçırırsanız çok üzülürsünüz! $SOLAR coin listelemesi onaylandı, 100x potansiyel! #SolarMoon #CryptoPump #Kripto",
            "Herkes baksın! $SOLAR coin için alım fırsatı başladı, 100x yapacak sakın geç kalmayın! #SolarMoon #Altcoins"
    );

    private final List<String> boycottBots = List.of(
            "sesimiz_var38291", "tuketici_hakki44921", "halk_hareketi88291", "adalet_sesi90192",
            "direnis_vakti77482", "dur_de29482", "boykot_zamani66291", "sessiz_kalma11092"
    );

    private final List<String> boycottTemplates = List.of(
            "Fahiş fiyatlara ve haksız kazanca artık dur diyoruz! #MarketlerBoykot kampanyasını başlatıyoruz, herkes destek olsun! #TüketiciHareketi",
            "Halkın sesine kulak verin! Fahiş fiyatlara karşı #MarketlerBoykot diyoruz, sessiz kalmayın ses verin! #TüketiciHareketi",
            "Artık yeter! Fahiş zamlara ve fırsatçılığa karşı #MarketlerBoykot hareketine herkes katılsın! #TüketiciHareketi #BirlikteGüçlüyüz",
            "Cebimize göz dikenlere karşı #MarketlerBoykot çağrısı yapıyoruz! Paylaşalım sesimiz duyulsun! #TüketiciHareketi",
            "Birlik olma zamanı! Haksız fiyat artışlarına karşı #MarketlerBoykot kampanyasına tam destek! #TüketiciHareketi"
    );

    private final List<String> disinfoBots = List.of(
            "gercekler_ortada99120", "uyan_turkiye44819", "saklanan_haberler33910",
            "bilgi_akisi22819", "sessiz_tanik77102", "haber_merkezi66102"
    );

    private final List<String> disinfoTemplates = List.of(
            "LÜTFEN DİKKAT! Şehir ana su şebekesine kimyasal madde sızdığı söyleniyor, musluk suyu içmeyin ve acil yayın! #AcilDurum #SağlıkUyarısı",
            "Acil uyarı! Şebeke suyuna zehirli kimyasal karıştığı iddia ediliyor, çocuklara su içirmeyin paylaşın! #AcilDurum #SağlıkUyarısı",
            "Resmi kanallar susuyor ama şebeke suyuna kimyasal madde karışmış, hemen önlem alın ve uyarın! #AcilDurum #SağlıkUyarısı #HalkSağlığı",
            "Bunu saklıyorlar! Şehir şebeke suyuna kimyasal madde sızıntısı var, musluk suyunu kullanmayın acil yayın! #AcilDurum #SağlıkUyarısı"
    );

    public SimulationService(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    public synchronized void startSimulation(int intervalMs) {
        this.intervalMillis = Math.max(300, intervalMs);
        if (isRunning.get()) {
            stopSimulation();
        }
        isRunning.set(true);
        simulationTask = scheduler.scheduleAtFixedRate(this::generateAndEmitOrganicPost, 200, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopSimulation() {
        isRunning.set(false);
        if (simulationTask != null && !simulationTask.isCancelled()) {
            simulationTask.cancel(true);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Generates a single organic post
     */
    public SocialPost generateAndEmitOrganicPost() {
        String author = organicAuthors.get(random.nextInt(organicAuthors.size()));
        String content = organicPosts.get(random.nextInt(organicPosts.size()));

        SocialPost post = new SocialPost();
        post.setId("POST-" + UUID.randomUUID().toString().substring(0, 8));
        post.setAuthorId("AUTH-" + Math.abs(author.hashCode() % 10000));
        post.setAuthorUsername(author);
        post.setAuthorFollowers(300 + random.nextInt(4500));
        post.setAuthorFollowing(150 + random.nextInt(500));
        post.setAuthorAccountAgeDays(200 + random.nextInt(1500));
        post.setHasDefaultAvatar(false);
        post.setContent(content);
        post.setPlatform("X / Twitter");
        post.setTimestamp(Instant.now());

        return detectionService.processPost(post);
    }

    /**
     * Triggers a coordinated attack scenario (Burst of bot accounts sharing copypasta in seconds)
     */
    public List<SocialPost> triggerAttackScenario(String scenarioType) {
        List<SocialPost> generatedPosts = new ArrayList<>();

        List<String> bots;
        List<String> templates;
        String campaignPlatform = "X / Twitter";

        switch (scenarioType.toUpperCase(Locale.ROOT)) {
            case "CRYPTO_PUMP" -> {
                bots = cryptoBots;
                templates = cryptoTemplates;
            }
            case "ASTROTURFING_BOYCOTT" -> {
                bots = boycottBots;
                templates = boycottTemplates;
            }
            case "DISINFO_VIRAL" -> {
                bots = disinfoBots;
                templates = disinfoTemplates;
            }
            default -> {
                bots = cryptoBots;
                templates = cryptoTemplates;
            }
        }

        for (int i = 0; i < bots.size(); i++) {
            String botName = bots.get(i);
            String template = templates.get(random.nextInt(templates.size()));

            SocialPost post = new SocialPost();
            post.setId("BOT-" + UUID.randomUUID().toString().substring(0, 8));
            post.setAuthorId("BOTID-" + (1000 + i));
            post.setAuthorUsername(botName);
            // Strong bot characteristics: very few followers, many following, new account, default avatar
            post.setAuthorFollowers(random.nextInt(8));
            post.setAuthorFollowing(300 + random.nextInt(800));
            post.setAuthorAccountAgeDays(1 + random.nextInt(4));
            post.setHasDefaultAvatar(true);
            post.setContent(template);
            post.setPlatform(campaignPlatform);
            post.setTimestamp(Instant.now().minusSeconds(random.nextInt(15))); // within 15 seconds!

            SocialPost analyzed = detectionService.processPost(post);
            generatedPosts.add(analyzed);
        }

        return generatedPosts;
    }
}
