package com.big4.dsatracker.entity;

import jakarta.persistence.*;

// One row per (tracker, itemId) pair. Stores ONLY numeric progress data:
// done (solved/reviewed), flagged (revision/important), rating (0-5 stars).
// Question text/answers/links stay static in each page's HTML - never stored here.
@Entity
@Table(name = "tracker_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"tracker", "item_id"}))
public class TrackerProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tracker; // "dsa", "dbms", "azure", "java", "docker"

    @Column(name = "item_id", nullable = false)
    private Integer itemId; // the question's number on that page

    private Integer done = 0;    // 0/1 - solved or reviewed
    private Integer flagged = 0; // 0/1 - revision or important
    private Integer rating = 0;  // 0-5

    public TrackerProgress() {
    }

    public TrackerProgress(String tracker, Integer itemId, Integer done, Integer flagged, Integer rating) {
        this.tracker = tracker;
        this.itemId = itemId;
        this.done = done;
        this.flagged = flagged;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTracker() { return tracker; }
    public void setTracker(String tracker) { this.tracker = tracker; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getDone() { return done; }
    public void setDone(Integer done) { this.done = done; }

    public Integer getFlagged() { return flagged; }
    public void setFlagged(Integer flagged) { this.flagged = flagged; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
