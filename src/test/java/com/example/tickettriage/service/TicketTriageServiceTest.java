package com.example.tickettriage.service;

import com.example.tickettriage.error.AiTriageException;
import com.example.tickettriage.web.Priority;
import com.example.tickettriage.web.Severity;
import com.example.tickettriage.web.TicketRequest;
import com.example.tickettriage.web.TicketTriageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketTriageServiceTest {

    @Test
    void triageReturnsStructuredResponseFromChatClient() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        TicketTriageResponse expected = new TicketTriageResponse(
                Severity.HIGH, Priority.P2,
                List.of("Restart the affected service", "Notify the on-call engineer"));

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(TicketTriageResponse.class)).thenReturn(expected);

        TicketTriageService service = new TicketTriageService(chatClient);
        TicketRequest request = new TicketRequest(
                "Database unreachable", "App cannot connect to primary DB", "All customers affected");

        TicketTriageResponse actual = service.triage(request);

        assertThat(actual).isEqualTo(expected);

        ArgumentCaptor<String> userMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(userMessageCaptor.capture());
        String userMessage = userMessageCaptor.getValue();
        assertThat(userMessage).contains(request.title());
        assertThat(userMessage).contains(request.description());
        assertThat(userMessage).contains(request.impact());
    }

    @Test
    void triageWrapsChatClientFailuresInAiTriageException() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);

        RuntimeException upstreamFailure = new RuntimeException("upstream timeout");

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(upstreamFailure);

        TicketTriageService service = new TicketTriageService(chatClient);
        TicketRequest request = new TicketRequest("Title", "Description", "Impact");

        assertThatThrownBy(() -> service.triage(request))
                .isInstanceOf(AiTriageException.class)
                .hasMessageContaining("Failed to triage ticket")
                .hasCause(upstreamFailure);
    }

    @Test
    void triageThrowsAiTriageExceptionWhenChatClientReturnsNullEntity() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(TicketTriageResponse.class)).thenReturn(null);

        TicketTriageService service = new TicketTriageService(chatClient);
        TicketRequest request = new TicketRequest("Title", "Description", "Impact");

        assertThatThrownBy(() -> service.triage(request))
                .isInstanceOf(AiTriageException.class)
                .hasMessageContaining("Failed to triage ticket");
    }
}
