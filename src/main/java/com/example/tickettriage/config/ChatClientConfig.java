package com.example.tickettriage.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            You are a support ticket triage assistant for an engineering
            organization. Given a trouble ticket's title, description, and
            impact statement, determine:
            - severity: one of CRITICAL, HIGH, MEDIUM, LOW
            - priority: one of P1, P2, P3, P4
            - nextSteps: a short list of concrete, actionable next steps for
              the on-call engineer

            Respond with only the requested structured data. Do not include
            any explanation outside the structured response.
            """;

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(SYSTEM_PROMPT).build();
    }
}
