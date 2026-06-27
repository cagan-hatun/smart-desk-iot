package com.smartdesk.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    private Long id = 1L;

    private Double idealTempMin;
    private Double idealTempMax;

    private Double idealHumidityMin;
    private Double idealHumidityMax;

    private Double idealAirQualityMax;

    private Double idealNoiseMax;

    private Double idealLightMin;
    private Double idealLightMax;

    private Integer sampleCount = 0;

    // ----- Getters & Setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getIdealTempMin() {
        return idealTempMin;
    }

    public void setIdealTempMin(Double idealTempMin) {
        this.idealTempMin = idealTempMin;
    }

    public Double getIdealTempMax() {
        return idealTempMax;
    }

    public void setIdealTempMax(Double idealTempMax) {
        this.idealTempMax = idealTempMax;
    }

    public Double getIdealHumidityMin() {
        return idealHumidityMin;
    }

    public void setIdealHumidityMin(Double idealHumidityMin) {
        this.idealHumidityMin = idealHumidityMin;
    }

    public Double getIdealHumidityMax() {
        return idealHumidityMax;
    }

    public void setIdealHumidityMax(Double idealHumidityMax) {
        this.idealHumidityMax = idealHumidityMax;
    }

    public Double getIdealAirQualityMax() {
        return idealAirQualityMax;
    }

    public void setIdealAirQualityMax(Double idealAirQualityMax) {
        this.idealAirQualityMax = idealAirQualityMax;
    }

    public Double getIdealNoiseMax() {
        return idealNoiseMax;
    }

    public void setIdealNoiseMax(Double idealNoiseMax) {
        this.idealNoiseMax = idealNoiseMax;
    }

    public Double getIdealLightMin() {
        return idealLightMin;
    }

    public void setIdealLightMin(Double idealLightMin) {
        this.idealLightMin = idealLightMin;
    }

    public Double getIdealLightMax() {
        return idealLightMax;
    }

    public void setIdealLightMax(Double idealLightMax) {
        this.idealLightMax = idealLightMax;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}
