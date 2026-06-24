package com.example.kwu_graduation.domain.simulation.dto;

public record GraduationSummaryResponse(
        int totalCredit,
        int majorCredit,
        int cultureCredit,
        int etcCredit
) {
}
