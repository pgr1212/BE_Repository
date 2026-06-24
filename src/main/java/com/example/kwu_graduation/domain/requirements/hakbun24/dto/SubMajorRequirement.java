package com.example.kwu_graduation.domain.requirements.hakbun24.dto;

import java.util.List;

public record SubMajorRequirement(
        String name,
        List<String> requiredCourses,
        int requiredCount
) {}