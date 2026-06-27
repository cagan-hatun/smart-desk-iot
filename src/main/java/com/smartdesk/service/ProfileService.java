package com.smartdesk.service;

import com.smartdesk.model.FeedbackEntry;
import com.smartdesk.model.SensorReading;
import com.smartdesk.model.UserProfile;
import com.smartdesk.repository.FeedbackEntryRepository;
import com.smartdesk.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final UserProfileRepository profileRepo;
    private final FeedbackEntryRepository feedbackRepo;

    public ProfileService(UserProfileRepository profileRepo, FeedbackEntryRepository feedbackRepo) {
        this.profileRepo = profileRepo;
        this.feedbackRepo = feedbackRepo;
    }

    public UserProfile getOrCreateProfile() {
        UserProfile p = profileRepo.findById(1L).orElseGet(() -> {
            UserProfile newP = new UserProfile();
            newP.setIdealTempMin(20.0);
            newP.setIdealTempMax(24.0);
            newP.setIdealHumidityMin(40.0);
            newP.setIdealHumidityMax(60.0);
            newP.setIdealAirQualityMax(2000.0);
            newP.setIdealNoiseMax(1800.0);
            newP.setIdealLightMin(800.0);
            newP.setIdealLightMax(3000.0);
            return profileRepo.save(newP);
        });
        if (p.getIdealLightMin() == null || p.getIdealLightMax() == null) {
            p.setIdealLightMin(800.0);
            p.setIdealLightMax(3000.0);
            profileRepo.save(p);
        }
        return p;
    }

    public void addFeedback(SensorReading reading, boolean isPositive) {
        FeedbackEntry entry = new FeedbackEntry();
        entry.setTemperature(reading.getTemperature());
        entry.setHumidity(reading.getHumidity());
        entry.setAirQuality(reading.getAirQuality());
        entry.setNoise(reading.getNoise());
        entry.setLight(reading.getLight());
        entry.setIsPositive(isPositive);
        feedbackRepo.save(entry);

        if (isPositive) {
            updateProfileFromFeedback();
        }
    }

    public void updateProfileFromFeedback() {
        List<FeedbackEntry> positives = feedbackRepo.findByIsPositiveTrue();
        if (positives.size() < 5) return;

        double tempMean = mean(positives, "temperature");
        double tempStd  = stdDev(positives, "temperature", tempMean);

        double humMean = mean(positives, "humidity");
        double humStd  = stdDev(positives, "humidity", humMean);

        double aqMean = mean(positives, "airQuality");
        double aqStd  = stdDev(positives, "airQuality", aqMean);

        double noiseMean = mean(positives, "noise");
        double noiseStd  = stdDev(positives, "noise", noiseMean);

        double lightMean = mean(positives, "light");
        double lightStd  = stdDev(positives, "light", lightMean);

        double idealTempMin = tempMean - tempStd;
        double idealTempMax = tempMean + tempStd;
        double idealHumidityMin = humMean - humStd;
        double idealHumidityMax = humMean + humStd;
        double idealAirQualityMax = aqMean + aqStd;
        double idealNoiseMax = noiseMean + noiseStd;
        double idealLightMin = lightMean - lightStd;
        double idealLightMax = lightMean + lightStd;

        // Negatifler "ne istemiyorum"u gosterir: ideal araligin icine dusen
        // bir negatif ortalama varsa, o yondeki siniri sikilastiriyoruz.
        List<FeedbackEntry> negatives = feedbackRepo.findByIsPositiveFalse();
        if (negatives.size() >= 3) {
            double[] tempRange = tightenRange(idealTempMin, idealTempMax, mean(negatives, "temperature"), tempStd);
            idealTempMin = tempRange[0];
            idealTempMax = tempRange[1];

            double[] humRange = tightenRange(idealHumidityMin, idealHumidityMax, mean(negatives, "humidity"), humStd);
            idealHumidityMin = humRange[0];
            idealHumidityMax = humRange[1];

            double[] lightRange = tightenRange(idealLightMin, idealLightMax, mean(negatives, "light"), lightStd);
            idealLightMin = lightRange[0];
            idealLightMax = lightRange[1];

            idealAirQualityMax = tightenUpperBound(idealAirQualityMax, mean(negatives, "airQuality"), aqStd);
            idealNoiseMax = tightenUpperBound(idealNoiseMax, mean(negatives, "noise"), noiseStd);
        }

        UserProfile p = getOrCreateProfile();
        p.setIdealTempMin(idealTempMin);
        p.setIdealTempMax(idealTempMax);
        p.setIdealHumidityMin(idealHumidityMin);
        p.setIdealHumidityMax(idealHumidityMax);
        p.setIdealAirQualityMax(idealAirQualityMax);
        p.setIdealNoiseMax(idealNoiseMax);
        p.setIdealLightMin(idealLightMin);
        p.setIdealLightMax(idealLightMax);
        p.setSampleCount(positives.size());
        profileRepo.save(p);
    }

   
    private double[] tightenRange(double min, double max, double negMean, double std) {
        if (negMean <= min || negMean >= max) return new double[]{min, max};
        double margin = std / 4;
        boolean closerToMax = (max - negMean) <= (negMean - min);
        if (closerToMax) {
            return new double[]{min, Math.max(min, negMean - margin)};
        }
        return new double[]{Math.min(max, negMean + margin), max};
    }

    /** Tek tarafli (sadece ust sinirli) metrikler icin tightenRange'in karsiligi. */
    private double tightenUpperBound(double max, double negMean, double std) {
        if (negMean >= max) return max;
        double margin = std / 4;
        return Math.max(0, negMean - margin);
    }

    private double mean(List<FeedbackEntry> entries, String metric) {
        return entries.stream()
                .mapToDouble(e -> metricValue(e, metric))
                .average()
                .orElse(0.0);
    }

    private double stdDev(List<FeedbackEntry> entries, String metric, double mean) {
        double variance = entries.stream()
                .mapToDouble(e -> metricValue(e, metric))
                .map(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double metricValue(FeedbackEntry e, String metric) {
        return switch (metric) {
            case "temperature" -> e.getTemperature() != null ? e.getTemperature() : 0.0;
            case "humidity"    -> e.getHumidity()    != null ? e.getHumidity()    : 0.0;
            case "airQuality"  -> e.getAirQuality()  != null ? e.getAirQuality()  : 0.0;
            case "noise"       -> e.getNoise()        != null ? e.getNoise()       : 0.0;
            case "light"       -> e.getLight()        != null ? e.getLight()       : 0.0;
            default -> 0.0;
        };
    }
}
