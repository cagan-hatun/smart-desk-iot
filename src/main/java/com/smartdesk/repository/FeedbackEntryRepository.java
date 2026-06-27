package com.smartdesk.repository;

import com.smartdesk.model.FeedbackEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackEntryRepository extends JpaRepository<FeedbackEntry, Long> {

    List<FeedbackEntry> findByIsPositiveTrue();

    List<FeedbackEntry> findByIsPositiveFalse();

    List<FeedbackEntry> findAllByOrderByTimestampDesc();
}
