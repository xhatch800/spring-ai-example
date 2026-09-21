package com.example.tickettriage.web;

import com.example.tickettriage.error.AiTriageException;
import com.example.tickettriage.service.TicketTriageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketTriageService triageService;

    @Test
    void returnsTriageResultForValidRequest() throws Exception {
        TicketTriageResponse response = new TicketTriageResponse(
                Severity.CRITICAL, Priority.P1, List.of("Page on-call", "Open incident channel"));
        when(triageService.triage(any(TicketRequest.class))).thenReturn(response);

        TicketRequest request = new TicketRequest(
                "Payment service down", "All payment requests are failing with 500 errors",
                "Revenue-impacting, all customers");

        mockMvc.perform(post("/ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.priority").value("P1"))
                .andExpect(jsonPath("$.nextSteps[0]").value("Page on-call"));
    }

    @Test
    void returnsBadRequestForBlankTitle() throws Exception {
        TicketRequest request = new TicketRequest("", "Description", "Impact");

        mockMvc.perform(post("/ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadGatewayWhenAiTriageFails() throws Exception {
        when(triageService.triage(any(TicketRequest.class)))
                .thenThrow(new AiTriageException("Failed to triage ticket via Anthropic", new RuntimeException("boom")));

        TicketRequest request = new TicketRequest("Title", "Description", "Impact");

        mockMvc.perform(post("/ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }
}
