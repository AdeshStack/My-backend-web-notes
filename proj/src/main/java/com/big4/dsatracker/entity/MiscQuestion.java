package com.big4.dsatracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// User-authored Q&A entries for the Misc page. Unlike the other trackers,
// the question/answer TEXT itself is stored here (the user writes it),
// alongside the same done/flagged/rating numeric fields as every other tracker.
@Entity
@Table(name = "misc_question")
public class MiscQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Lob
    private String answer;

    private Integer done = 0;
    private Integer flagged = 0;
    private Integer rating = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    public MiscQuestion() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Integer getDone() { return done; }
    public void setDone(Integer done) { this.done = done; }

    public Integer getFlagged() { return flagged; }
    public void setFlagged(Integer flagged) { this.flagged = flagged; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
