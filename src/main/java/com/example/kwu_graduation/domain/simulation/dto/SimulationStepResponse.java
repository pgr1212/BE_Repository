package com.example.kwu_graduation.domain.simulation.dto;

import java.util.List;

public record SimulationStepResponse(
        int gradeYear,
        int semester,
        List<RecommendedCourseResponse> recommendedCourses,
        int addedCredit,
        List<RequirementGapResponse> remainingGaps,
        boolean graduationAvailable,
        String message
) {
}
