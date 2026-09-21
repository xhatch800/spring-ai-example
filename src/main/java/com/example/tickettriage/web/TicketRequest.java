package com.example.tickettriage.web;

import jakarta.validation.constraints.NotBlank;

public record TicketRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String impact) {
}
