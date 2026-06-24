package com.example.kwu_graduation.domain.simulation.dto;

public record RequirementGapResponse(
        String type,
        String name,
        int required,
        int completed,
        int remaining,
        boolean satisfied
) {
    public static RequirementGapResponse of(String type, String name, int required, int completed) {
        int remaining = Math.max(required - completed, 0);
        return new RequirementGapResponse(type, name, required, completed, remaining, remaining == 0);
    }
}
