package com.example.tickettriage.web;

import com.example.tickettriage.service.TicketTriageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {

    private final TicketTriageService triageService;

    public TicketController(TicketTriageService triageService) {
        this.triageService = triageService;
    }

    @PostMapping("/ticket")
    public TicketTriageResponse triageTicket(@Valid @RequestBody TicketRequest request) {
        return triageService.triage(request);
    }
}
