package com.example.tickettriage.web;

import java.util.List;

public record TicketTriageResponse(
        Severity severity,
        Priority priority,
        List<String> nextSteps) {
}
