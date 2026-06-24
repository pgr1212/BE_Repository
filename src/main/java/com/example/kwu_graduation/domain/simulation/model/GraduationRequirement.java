package com.example.kwu_graduation.domain.simulation.model;

import java.util.List;

public record GraduationRequirement(
        String requirementYear,
        String departmentCode,
        int totalCredit,
        int majorCredit,
        int cultureCredit,
        int etcCredit,
        List<RequiredCourse> requiredCourses,
        List<RequiredArea> requiredAreas,
        List<ManualCheck> manualChecks
) {
    public GraduationRequirement {
        requiredCourses = requiredCourses == null ? List.of() : List.copyOf(requiredCourses);
        requiredAreas = requiredAreas == null ? List.of() : List.copyOf(requiredAreas);
        manualChecks = manualChecks == null ? List.of() : List.copyOf(manualChecks);
    }
}
