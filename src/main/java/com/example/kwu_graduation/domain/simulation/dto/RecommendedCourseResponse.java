package com.example.kwu_graduation.domain.simulation.dto;

import com.example.kwu_graduation.domain.simulation.model.CourseCategory;

public record RecommendedCourseResponse(
        String courseCode,
        String courseName,
        CourseCategory category,
        int credit
) {
}
