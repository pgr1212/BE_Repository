package com.example.kwu_graduation.domain.grade.dto;

/**
 * KLAS 성적에서 산출한 평점 통계.
 *
 * @param gpa               현재 누적 평점 (4.5 만점, 학점 가중 평균)
 * @param stdDeviation      과목별 평점의 표준편차 (성적의 일관성/편차)
 * @param gradedCredits     평점 산출에 포함된 학점 (P/NP·삭제 과목 제외)
 * @param gradedCourseCount 평점 산출에 포함된 과목 수
 */
public record GpaStats(
        double gpa,
        double stdDeviation,
        int gradedCredits,
        int gradedCourseCount
) {
}
