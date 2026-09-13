package com.big4.dsatracker.controller;

import com.big4.dsatracker.entity.MiscQuestion;
import com.big4.dsatracker.repository.MiscQuestionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Full CRUD for user-created Misc questions: add your own title + answer,
// edit them, delete them, and toggle done/flagged/rating like every other tracker.
@RestController
@RequestMapping("/api/misc")
public class MiscQuestionController {

    private final MiscQuestionRepository repository;

    public MiscQuestionController(MiscQuestionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MiscQuestion> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public MiscQuestion create(@RequestBody MiscQuestion body) {
        MiscQuestion q = new MiscQuestion();
        q.setTitle(body.getTitle());
        q.setAnswer(body.getAnswer());
        return repository.save(q);
    }

    @PutMapping("/{id}")
    public MiscQuestion update(@PathVariable Long id, @RequestBody MiscQuestion body) {
        MiscQuestion existing = repository.findById(id).orElseThrow();
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getAnswer() != null) existing.setAnswer(body.getAnswer());
        if (body.getDone() != null) existing.setDone(body.getDone());
        if (body.getFlagged() != null) existing.setFlagged(body.getFlagged());
        if (body.getRating() != null) existing.setRating(body.getRating());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
