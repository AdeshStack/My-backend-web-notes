package com.big4.dsatracker.controller;

import com.big4.dsatracker.entity.MiscQuestion;
import com.big4.dsatracker.repository.MiscQuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Controller-level test only: the repository is mocked, so no real database is
// touched. Verifies the full CRUD contract the Misc page's JS relies on.
@WebMvcTest(MiscQuestionController.class)
class MiscQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MiscQuestionRepository repository;

    @Test
    void getAll_returnsEveryQuestion() throws Exception {
        MiscQuestion q = new MiscQuestion();
        q.setId(1L);
        q.setTitle("What is a deadlock?");
        q.setAnswer("Two or more threads waiting on each other forever.");
        when(repository.findAll()).thenReturn(List.of(q));

        mockMvc.perform(get("/api/misc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("What is a deadlock?"));
    }

    @Test
    void create_savesTitleAndAnswer_ignoringClientSuppliedProgressFields() throws Exception {
        when(repository.save(any(MiscQuestion.class))).thenAnswer(inv -> {
            MiscQuestion saved = inv.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        String body = """
                { "title": "What is idempotency?", "answer": "Same result no matter how many times you call it." }
                """;

        mockMvc.perform(post("/api/misc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("What is idempotency?"))
                .andExpect(jsonPath("$.done").value(0));

        verify(repository).save(any(MiscQuestion.class));
    }

    @Test
    void update_onlyOverwritesFieldsThatWereSent() throws Exception {
        MiscQuestion existing = new MiscQuestion();
        existing.setId(7L);
        existing.setTitle("Old title");
        existing.setAnswer("Old answer");
        existing.setDone(0);
        existing.setFlagged(0);
        existing.setRating(0);

        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(any(MiscQuestion.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                { "done": 1, "rating": 5 }
                """;

        mockMvc.perform(put("/api/misc/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Old title"))
                .andExpect(jsonPath("$.done").value(1))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void delete_removesTheQuestionById() throws Exception {
        mockMvc.perform(delete("/api/misc/9"))
                .andExpect(status().isOk());

        verify(repository).deleteById(eq(9L));
    }
}
