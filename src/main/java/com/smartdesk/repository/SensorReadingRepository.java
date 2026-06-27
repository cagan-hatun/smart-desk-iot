package com.smartdesk.repository;

import com.smartdesk.model.SensorReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /** En son kaydedilen olcumu getirir (dashboard'un anlik karti icin) */
    SensorReading findFirstByOrderByTimestampDesc();

    /** Grafikler icin en son N kaydi getirir */
    List<SensorReading> findAllByOrderByTimestampDesc(Pageable pageable);
}
