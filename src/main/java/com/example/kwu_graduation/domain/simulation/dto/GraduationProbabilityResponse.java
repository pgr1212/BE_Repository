package com.example.kwu_graduation.domain.simulation.dto;

/**
 * 졸업 가능 확률 계산 결과.
 *
 * @param probability        졸업 시점 목표 누적 평점 달성 확률 (0~100)
 * @param currentGpa         현재 누적 평점 (4.5 만점)
 * @param targetGpa          목표 누적 평점 (졸업 시점)
 * @param requiredAverageGpa 목표 달성을 위해 남은 과목에서 받아야 하는 평균 평점
 * @param plannedCourseCount 다음 학기에 들을 과목 수
 * @param plannedCredits     다음 학기에 들을 학점
 * @param message            결과 설명 문구
 */
public record GraduationProbabilityResponse(
        int probability,
        double currentGpa,
        double targetGpa,
        double requiredAverageGpa,
        int plannedCourseCount,
        int plannedCredits,
        String message
) {
}
