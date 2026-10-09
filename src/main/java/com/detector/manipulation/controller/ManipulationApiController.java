package com.detector.manipulation.controller;

import com.detector.manipulation.model.*;
import com.detector.manipulation.service.DetectionService;
import com.detector.manipulation.service.SimulationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ManipulationApiController {

    private final DetectionService detectionService;
    private final SimulationService simulationService;

    public ManipulationApiController(DetectionService detectionService, SimulationService simulationService) {
        this.detectionService = detectionService;
        this.simulationService = simulationService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<SystemMetrics> getMetrics() {
        return ResponseEntity.ok(detectionService.getMetrics());
    }

    @GetMapping("/posts")
    public ResponseEntity<List<SocialPost>> getPosts(@RequestParam(defaultValue = "50") int limit,
                                                    @RequestParam(required = false) String filter) {
        List<SocialPost> posts = detectionService.getRecentPosts(limit);
        if ("flagged".equalsIgnoreCase(filter)) {
            posts = posts.stream().filter(p -> p.getThreatLevel() != ThreatLevel.LOW).toList();
        } else if ("high".equalsIgnoreCase(filter)) {
            posts = posts.stream().filter(p -> p.getThreatLevel() == ThreatLevel.HIGH || p.getThreatLevel() == ThreatLevel.CRITICAL).toList();
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/clusters")
    public ResponseEntity<Collection<CoordinatedCluster>> getClusters() {
        return ResponseEntity.ok(detectionService.getClusters());
    }

    @GetMapping("/accounts/flagged")
    public ResponseEntity<List<AccountRiskProfile>> getFlaggedAccounts() {
        return ResponseEntity.ok(detectionService.getFlaggedAccounts());
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyzeCustomPost(@RequestBody AnalyzeRequest request) {
        return ResponseEntity.ok(detectionService.inspectText(request));
    }

    @PostMapping("/post")
    public ResponseEntity<SocialPost> ingestSinglePost(@RequestBody SocialPost post) {
        return ResponseEntity.ok(detectionService.processPost(post));
    }

    @PostMapping("/simulation/start")
    public ResponseEntity<Map<String, Object>> startSimulation(@RequestParam(defaultValue = "1500") int interval) {
        simulationService.startSimulation(interval);
        return ResponseEntity.ok(Map.of("status", "running", "intervalMillis", interval));
    }

    @PostMapping("/simulation/stop")
    public ResponseEntity<Map<String, Object>> stopSimulation() {
        simulationService.stopSimulation();
        return ResponseEntity.ok(Map.of("status", "stopped"));
    }

    @PostMapping("/simulation/single-organic")
    public ResponseEntity<SocialPost> generateSingleOrganic() {
        return ResponseEntity.ok(simulationService.generateAndEmitOrganicPost());
    }

    @PostMapping("/simulation/attack")
    public ResponseEntity<List<SocialPost>> triggerAttack(@RequestParam(defaultValue = "CRYPTO_PUMP") String scenario) {
        List<SocialPost> attackPosts = simulationService.triggerAttackScenario(scenario);
        return ResponseEntity.ok(attackPosts);
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearSystem() {
        detectionService.clearAll();
        return ResponseEntity.ok(Map.of("message", "Tüm tespit geçmişi ve sayaçlar sıfırlandı."));
    }

    @GetMapping(value = "/stream/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveEvents() {
        return detectionService.registerSseEmitter();
    }
}
