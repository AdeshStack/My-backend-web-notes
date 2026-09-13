package com.big4.dsatracker.controller;

import com.big4.dsatracker.entity.TrackerProgress;
import com.big4.dsatracker.repository.TrackerProgressRepository;
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
// touched. Verifies the /api/progress/{tracker} contract that every tracker
// page's JS (dsa, dbms, azure, java, docker) relies on.
@WebMvcTest(TrackerProgressController.class)
class TrackerProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrackerProgressRepository repository;

    @Test
    void getAll_returnsRowsForThatTrackerOnly() throws Exception {
        TrackerProgress row = new TrackerProgress("dsa", 1, 1, 0, 4);
        when(repository.findByTracker("dsa")).thenReturn(List.of(row));

        mockMvc.perform(get("/api/progress/dsa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tracker").value("dsa"))
                .andExpect(jsonPath("$[0].itemId").value(1))
                .andExpect(jsonPath("$[0].done").value(1))
                .andExpect(jsonPath("$[0].rating").value(4));

        verify(repository).findByTracker("dsa");
    }

    @Test
    void update_createsNewRow_whenNoneExistsYet() throws Exception {
        when(repository.findByTrackerAndItemId("docker", 5)).thenReturn(Optional.empty());
        when(repository.save(any(TrackerProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = objectMapper.writeValueAsString(new TrackerProgress(null, null, 1, 1, 5));

        mockMvc.perform(put("/api/progress/docker/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracker").value("docker"))
                .andExpect(jsonPath("$.itemId").value(5))
                .andExpect(jsonPath("$.done").value(1))
                .andExpect(jsonPath("$.flagged").value(1))
                .andExpect(jsonPath("$.rating").value(5));

        verify(repository).save(any(TrackerProgress.class));
    }

    @Test
    void update_updatesExistingRow_insteadOfDuplicating() throws Exception {
        TrackerProgress existing = new TrackerProgress("java", 10, 0, 0, 0);
        when(repository.findByTrackerAndItemId("java", 10)).thenReturn(Optional.of(existing));
        when(repository.save(any(TrackerProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = objectMapper.writeValueAsString(new TrackerProgress(null, null, 1, 0, 3));

        mockMvc.perform(put("/api/progress/java/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(1))
                .andExpect(jsonPath("$.rating").value(3));

        verify(repository, never()).save(argThat(p -> !"java".equals(p.getTracker())));
    }

    @Test
    void reset_deletesAllRowsForThatTrackerOnly() throws Exception {
        mockMvc.perform(post("/api/progress/azure/reset"))
                .andExpect(status().isOk());

        verify(repository).deleteByTracker(eq("azure"));
        verify(repository, never()).deleteByTracker(eq("dbms"));
    }
}
