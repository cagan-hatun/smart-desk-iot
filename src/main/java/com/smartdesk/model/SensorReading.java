package com.smartdesk.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tek bir sensor olcum kaydini temsil eder.
 * ESP32, her olcumde bu yapiya uygun bir JSON gonderir:
 * {
 *   "temperature": 23.4,
 *   "humidity": 45.0,
 *   "airQuality": 1450,
 *   "noise": 1200,
 *   "distance": 55.0
 * }
 */
@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DHT22 - santigrat derece */
    private Double temperature;

    /** DHT22 - bagil nem yuzdesi */
    private Double humidity;

    /** MQ-135 - ham analog deger (0-4095). Dusuk = daha temiz hava (kalibrasyon gerekir) */
    private Integer airQuality;

    /** HW-484 - ham analog deger (0-4095). Dusuk = daha sessiz */
    private Integer noise;

    /** KY-018 - ham analog deger (0-4095). Dusuk = daha karanlik */
    private Integer light;

    /** HC-SR04 - kullanicinin masaya olan mesafesi (cm) */
    private Double distance;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // ----- Getters & Setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Integer getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(Integer airQuality) {
        this.airQuality = airQuality;
    }

    public Integer getNoise() {
        return noise;
    }

    public void setNoise(Integer noise) {
        this.noise = noise;
    }

    public Integer getLight() {
        return light;
    }

    public void setLight(Integer light) {
        this.light = light;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
