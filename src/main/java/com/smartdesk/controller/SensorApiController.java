package com.smartdesk.controller;

import com.smartdesk.model.SensorReading;
import com.smartdesk.model.UserProfile;
import com.smartdesk.repository.SensorReadingRepository;
import com.smartdesk.service.ComfortService;
import com.smartdesk.service.PresenceService;
import com.smartdesk.service.ProfileService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/sensor")
public class SensorApiController {

    private final SensorReadingRepository repository;
    private final ComfortService comfortService;
    private final ProfileService profileService;
    private final PresenceService presenceService;

    private final AtomicBoolean demoActive = new AtomicBoolean(false);
    private volatile SensorReading demoReading;

    public SensorApiController(SensorReadingRepository repository,
                               ComfortService comfortService,
                               ProfileService profileService,
                               PresenceService presenceService) {
        this.repository = repository;
        this.comfortService = comfortService;
        this.profileService = profileService;
        this.presenceService = presenceService;
    }

    /**
     * ESP32'den gelen yeni olcumu kaydeder.
     * Beklenen JSON govdesi:
     * {
     *   "temperature": 23.4,
     *   "humidity": 45.0,
     *   "airQuality": 1450,
     *   "noise": 1200,
     *   "distance": 55.0
     * }
     */
    @PostMapping
    public SensorReading addReading(@RequestBody SensorReading reading) {
        reading.setId(null);
        reading.setTimestamp(null); // sunucu zamani kullanilsin
        SensorReading saved = repository.save(reading);
        presenceService.update(saved);
        return saved;
    }

    /** Dashboard'un birkaç saniyede bir cagirdigi anlik durum endpoint'i */
    @GetMapping("/latest")
    public Map<String, Object> latest() {
        SensorReading reading = demoActive.get() && demoReading != null
                ? demoReading
                : repository.findFirstByOrderByTimestampDesc();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reading", reading);
        response.put("comfort", comfortService.evaluate(reading));
        response.put("presenceState", presenceService.getState().name());
        response.put("currentSessionMinutes", presenceService.getSessionMinutes());
        response.put("totalSessionMinutes", presenceService.getTotalMinutes());
        response.put("shouldSuggestBreak", presenceService.shouldSuggestBreak());
        return response;
    }

    /**
     * Demo modu acma/kapama ve slider degerlerini uygulama.
     * Beklenen JSON govdesi: {"active": true, "temperature": 24, "light": 1500, "noise": 600, "distance": 50}
     * Hicbir yere kaydetmez, sadece in-memory tutar. active=false oldugunda
     * /latest tekrar gercek sensor verisine doner.
     */
    @PostMapping("/demo")
    public Map<String, Object> demo(@RequestBody Map<String, Object> body) {
        boolean active = !Boolean.FALSE.equals(body.get("active"));
        demoActive.set(active);

        if (active) {
            SensorReading r = new SensorReading();
            if (body.get("temperature") != null) r.setTemperature(((Number) body.get("temperature")).doubleValue());
            if (body.get("light") != null) r.setLight(((Number) body.get("light")).intValue());
            if (body.get("noise") != null) r.setNoise(((Number) body.get("noise")).intValue());
            if (body.get("distance") != null) r.setDistance(((Number) body.get("distance")).doubleValue());
            r.setTimestamp(LocalDateTime.now());
            demoReading = r;
            presenceService.update(r);
        } else {
            demoReading = null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("active", demoActive.get());
        return result;
    }

    /** Grafikler icin son N kayit (varsayilan 30) */
    @GetMapping("/history")
    public List<SensorReading> history(@RequestParam(defaultValue = "30") int limit) {
        List<SensorReading> readings = repository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
        Collections.reverse(readings); // eskiden yeniye sirala
        return readings;
    }

    /**
     * Kullanici geri bildirimi kaydeder ve profili gunceller.
     * Beklenen JSON govdesi: {"isPositive": true} veya {"isPositive": false}
     * Yanit: guncel UserProfile JSON
     */
    @PostMapping("/feedback")
    public ResponseEntity<UserProfile> addFeedback(@RequestBody Map<String, Boolean> body) {
        SensorReading reading = repository.findFirstByOrderByTimestampDesc();
        if (reading == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean isPositive = Boolean.TRUE.equals(body.get("isPositive"));
        profileService.addFeedback(reading, isPositive);
        return ResponseEntity.ok(profileService.getOrCreateProfile());
    }
}
