package com.big4.dsatracker.repository;

import com.big4.dsatracker.entity.TrackerProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackerProgressRepository extends JpaRepository<TrackerProgress, Long> {
    List<TrackerProgress> findByTracker(String tracker);
    Optional<TrackerProgress> findByTrackerAndItemId(String tracker, Integer itemId);
    void deleteByTracker(String tracker);
}
