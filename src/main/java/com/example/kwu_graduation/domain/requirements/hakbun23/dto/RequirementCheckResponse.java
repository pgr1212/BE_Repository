package com.example.kwu_graduation.domain.requirements.hakbun23.dto;

import java.util.List;

public record RequirementCheckResponse(
        List<RequirementItem> items,
        boolean graduationPossible,
        int unmetCount
) {
    public static RequirementCheckResponse of(List<RequirementItem> items) {
        long unmet = items.stream().filter(i -> !i.satisfied()).count();
        return new RequirementCheckResponse(items, unmet == 0, (int) unmet);
    }
}