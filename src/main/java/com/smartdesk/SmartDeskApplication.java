package com.smartdesk;

import com.smartdesk.model.SensorReading;
import com.smartdesk.repository.SensorReadingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class SmartDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDeskApplication.class, args);
    }

 
    @Bean
    CommandLineRunner seedData(SensorReadingRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();
                for (int i = 30; i >= 0; i--) {
                    SensorReading reading = new SensorReading();
                    reading.setTemperature(round1(21 + Math.random() * 4));
                    reading.setHumidity(round1(40 + Math.random() * 15));
                    reading.setAirQuality((int) (1000 + Math.random() * 1500));
                    reading.setNoise((int) (800 + Math.random() * 1200));
                    reading.setDistance(round1(30 + Math.random() * 60));
                    reading.setTimestamp(now.minusMinutes(i * 2L));
                    repository.save(reading);
                }
            }
        };
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
