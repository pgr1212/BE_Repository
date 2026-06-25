package com.example.kwu_graduation.domain.simulation.hakbun24.spec;

import java.util.List;

/**
 * 세부전공(트랙)별 세부전공필수 교과목 요건.
 * 예) 소프트웨어학부 인공지능전공: 지정 5과목 중 3과목 이상 이수.
 * 세부전공필수가 없는 트랙(예: 소프트웨어전공)은 {@code requiredCount}=0 으로 두면 항상 충족 처리된다.
 *
 * @param name           세부전공명(예: 인공지능전공, 소프트웨어전공)
 * @param requiredCourses 세부전공필수 후보 과목
 * @param requiredCount  위 과목 중 최소 이수 과목 수(0이면 요건 없음)
 */
public record SubMajorRequirement(
        String name,
        List<String> requiredCourses,
        int requiredCount
) {
    public SubMajorRequirement {
        requiredCourses = requiredCourses == null ? List.of() : requiredCourses;
    }
}
