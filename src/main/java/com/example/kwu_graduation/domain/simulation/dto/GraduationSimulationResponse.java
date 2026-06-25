package com.example.kwu_graduation.domain.simulation.dto;

import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckItem;

import java.util.List;

public record GraduationSimulationResponse(
        int admissionYear,
        String department,
        boolean graduatableNow,
        int totalRequired,
        int totalEarned,
        GraduationSummaryResponse summary,
        List<RequirementGapResponse> gaps,
        List<String> warnings,
        List<CheckItem> requirementItems
) {
    public GraduationSimulationResponse {
        gaps = gaps == null ? List.of() : List.copyOf(gaps);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        requirementItems = requirementItems == null ? List.of() : List.copyOf(requirementItems);
    }
}
