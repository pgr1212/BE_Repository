package com.example.kwu_graduation.domain.simulation.dto;

import java.util.List;

/**
 * 졸업 가능 확률 계산 요청.
 *
 * <p>프론트 입력값(다음 학기 전공 과목 수, 추가 교양 과목 수, 재수강 과목, 목표 학기 평점)을 담는다.
 * 현재 누적 평점은 KLAS 성적으로 직접 조회하므로 요청에 포함하지 않는다.
 *
 * @param nextSemesterMajorCount 다음 학기에 들을 전공 과목 수
 * @param additionalCultureCount 추가로 들을 교양 과목 수
 * @param retakeCourses          재수강 과목 목록
 * @param targetGpa              목표 학기 평점 (0 초과 4.5 이하)
 * @param creditPerCourse        과목당 학점 (미지정 시 3학점으로 가정)
 */
public record GraduationProbabilityRequest(
        int nextSemesterMajorCount,
        int additionalCultureCount,
        List<String> retakeCourses,
        double targetGpa,
        Integer creditPerCourse
) {
    public List<String> safeRetakeCourses() {
        return retakeCourses == null ? List.of() : retakeCourses;
    }

    public int creditPerCourseOrDefault() {
        return creditPerCourse == null || creditPerCourse <= 0 ? 3 : creditPerCourse;
    }

    /** 다음 학기에 들을 총 과목 수 (전공 + 교양 + 재수강). */
    public int plannedCourseCount() {
        return nextSemesterMajorCount + additionalCultureCount + safeRetakeCourses().size();
    }
}
