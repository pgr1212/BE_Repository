package com.example.kwu_graduation.domain.simulation.model;

public record RequiredCourse(
        String courseCode,
        String courseName,
        CourseCategory category,
        int credit
) {
}
