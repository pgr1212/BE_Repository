package com.example.kwu_graduation.domain.simulation.dto;

public record GraduationSimulationRequest(
        int admissionYear,
        String department,
        int currentGradeYear,
        int currentSemester,
        Boolean includeInProgressCourses,
        Boolean multiMajorCompleted,
        Boolean graduationProjectCompleted,
        Boolean engineeringProgram,
        Boolean topcitCompleted,
        String subMajor
) {
    public boolean includeInProgressCoursesOrDefault() {
        return Boolean.TRUE.equals(includeInProgressCourses);
    }
}
