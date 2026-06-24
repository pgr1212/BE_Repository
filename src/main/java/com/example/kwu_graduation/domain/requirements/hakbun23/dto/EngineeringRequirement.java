package com.example.kwu_graduation.domain.requirements.hakbun23.dto;

import java.util.List;

public record EngineeringRequirement(
        int mscCredit,
        List<String> mscRequiredCourses,
        List<MscArea> mscAreas,
        List<String> mscPoolCourses,
        List<String> requiredCourses,
        List<String> requiredCoursesOneOf
) {}