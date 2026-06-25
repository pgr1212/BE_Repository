package com.example.kwu_graduation.domain.requirements.hakbun23.dto;

import java.util.List;

/**
 * 학번·학과별 졸업요건 정보(조회용). 25/26학번과 동일한 형태를 공유한다.
 */
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