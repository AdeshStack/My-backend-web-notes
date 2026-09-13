package com.big4.dsatracker.controller;

import com.big4.dsatracker.entity.TrackerProgress;
import com.big4.dsatracker.repository.TrackerProgressRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// One generic API serves every tracker page. The {tracker} path segment
// (e.g. "dsa", "dbms", "azure", "java", "docker") scopes all reads/writes.
@RestController
@RequestMapping("/api/progress/{tracker}")
public class TrackerProgressController {

    private final TrackerProgressRepository repository;

    public TrackerProgressController(TrackerProgressRepository repository) {
        this.repository = repository;
    }

    // Returns only the rows that exist for this tracker; the frontend treats
    // any question with no row as not-done/not-flagged/unrated by default.
    @GetMapping
    public List<TrackerProgress> getAll(@PathVariable String tracker) {
        return repository.findByTracker(tracker);
    }

    // Upserts one question's numeric progress within this tracker.
    @PutMapping("/{itemId}")
    public TrackerProgress update(@PathVariable String tracker, @PathVariable Integer itemId,
                                   @RequestBody TrackerProgress body) {
        TrackerProgress existing = repository.findByTrackerAndItemId(tracker, itemId)
                .orElse(new TrackerProgress(tracker, itemId, 0, 0, 0));
        existing.setDone(body.getDone() == null ? 0 : body.getDone());
        existing.setFlagged(body.getFlagged() == null ? 0 : body.getFlagged());
        existing.setRating(body.getRating() == null ? 0 : body.getRating());
        return repository.save(existing);
    }

    // Wipes all progress rows for this tracker only.
    @PostMapping("/reset")
    public void reset(@PathVariable String tracker) {
        repository.deleteByTracker(tracker);
    }
}
