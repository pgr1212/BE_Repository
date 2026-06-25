package com.example.kwu_graduation.domain.requirements.hakbun24.dto;

import java.util.List;

public record RequirementResponse(
        int admissionYear,
        String college,
        String department,

        int totalCredit,
        int majorCredit,
        int liberalArtsCredit,
        int balanceCredit,

        int balanceMinAreas,
        List<String> balanceAreas,
        List<String> balanceMandatoryAreasAnyOf,

        List<String> requiredLiberalCourses,
        boolean multiMajorRequired,
        List<String> graduationProjectOptions,

        EngineeringRequirement engineering,

        boolean topcitRequired,

        List<SubMajorRequirement> subMajors,

        List<String> additionalRequirements
) {}