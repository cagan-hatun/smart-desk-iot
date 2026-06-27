package com.smartdesk.service;

import com.smartdesk.model.SensorReading;
import com.smartdesk.model.UserProfile;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ComfortService {

    private final ProfileService profileService;

    public ComfortService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public Map<String, Object> evaluate(SensorReading r) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (r == null) {
            result.put("score", 0);
            result.put("status", "VERI_YOK");
            result.put("metrics", Map.of());
            return result;
        }

        UserProfile profile = profileService.getOrCreateProfile();
        Map<String, String> metricStatus = new LinkedHashMap<>();
        int totalScore = 0;
        int metricCount = 0;

        if (r.getTemperature() != null) {
            int s = scoreValue(r.getTemperature(),
                    profile.getIdealTempMin(), profile.getIdealTempMax());
            metricStatus.put("temperature", labelFor(s));
            totalScore += s;
            metricCount++;
        }

        if (r.getHumidity() != null) {
            int s = scoreValue(r.getHumidity(),
                    profile.getIdealHumidityMin(), profile.getIdealHumidityMax());
            metricStatus.put("humidity", labelFor(s));
            totalScore += s;
            metricCount++;
        }

        if (r.getAirQuality() != null) {
            int s = scoreValueInverted(r.getAirQuality(), profile.getIdealAirQualityMax());
            metricStatus.put("airQuality", labelFor(s));
            totalScore += s;
            metricCount++;
        }

        if (r.getNoise() != null) {
            int s = scoreValueInverted(r.getNoise(), profile.getIdealNoiseMax());
            metricStatus.put("noise", labelFor(s));
            totalScore += s;
            metricCount++;
        }

        if (r.getLight() != null && profile.getIdealLightMin() != null && profile.getIdealLightMax() != null) {
            int s = scoreValue(r.getLight(), profile.getIdealLightMin(), profile.getIdealLightMax());
            metricStatus.put("light", labelFor(s));
            totalScore += s;
            metricCount++;
        }

        int finalScore = metricCount > 0 ? totalScore / metricCount : 0;
        result.put("score", finalScore);
        result.put("status", labelFor(finalScore));
        result.put("metrics", metricStatus);
        return result;
    }

    private String labelFor(int score) {
        if (score >= 75) return "IDEAL";
        if (score >= 50) return "ORTA";
        return "DIKKAT";
    }

    
    private int scoreValue(double value, double idealMin, double idealMax) {
        if (value >= idealMin && value <= idealMax) return 100;
        double percentOutside = value < idealMin
                ? (idealMin - value) / idealMin * 100
                : (value - idealMax) / idealMax * 100;
        return scoreFromPercentOutside(percentOutside);
    }

    private int scoreValueInverted(double value, double idealMax) {
        if (value <= idealMax) return 100;
        double percentOutside = (value - idealMax) / idealMax * 100;
        return scoreFromPercentOutside(percentOutside);
    }

    private int scoreFromPercentOutside(double percentOutside) {
        if (percentOutside <= 20) {
            return (int) Math.round(100 - (percentOutside / 20.0) * 40);
        }
        if (percentOutside <= 50) {
            return (int) Math.round(60 - ((percentOutside - 20) / 30.0) * 40);
        }
        return 20;
    }
}
