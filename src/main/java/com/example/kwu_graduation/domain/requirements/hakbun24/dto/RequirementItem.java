package com.example.kwu_graduation.domain.requirements.hakbun24.dto;

public record RequirementItem(
        String label,
        int earned,
        int required,
        boolean satisfied
) {
    public static RequirementItem of(String label, int earned, int required) {
        return new RequirementItem(label, earned, required, earned >= required);
    }
}