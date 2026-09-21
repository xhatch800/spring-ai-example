package com.example.tickettriage.service;

import com.example.tickettriage.error.AiTriageException;
import com.example.tickettriage.web.TicketRequest;
import com.example.tickettriage.web.TicketTriageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TicketTriageService {

    private final ChatClient chatClient;

    public TicketTriageService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public TicketTriageResponse triage(TicketRequest request) {
        String userMessage = """
                Title: %s
                Description: %s
                Impact: %s
                """.formatted(request.title(), request.description(), request.impact());

        TicketTriageResponse response;
        try {
            response = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .entity(TicketTriageResponse.class);
        } catch (RuntimeException ex) {
            throw new AiTriageException("Failed to triage ticket via Anthropic", ex);
        }

        if (response == null) {
            throw new AiTriageException("Failed to triage ticket via Anthropic: empty response", null);
        }

        return response;
    }
}
