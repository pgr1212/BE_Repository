package com.example.kwu_graduation.domain.requirements.common.spec;

import java.util.List;

/**
 * 학번·학과별 졸업요건 정의.
 * resources/requirements/hakbun{yy}/{학과코드}.json 파일을 그대로 매핑한다.
 */
public record GraduationRequirement(
        int admissionYear,
        String college,
        String department,

        // 학점 요건
        int totalCredit,          // 졸업 총 이수학점
        int majorCredit,          // 주전공(필수 포함) 최소학점
        int liberalArtsCredit,    // 교양 총 이수학점(필수교양 + 균형교양)
        int balanceCredit,        // 균형교양 최소학점

        // 균형교양 영역 요건
        int balanceMinAreas,                      // 8영역 중 최소 이수 영역 수
        List<String> balanceAreas,                // 전체 균형교양 영역(참고용)
        List<String> balanceMandatoryAreasAnyOf,  // 이 중 최소 1개 영역은 반드시 포함

        // 과목·기타 요건
        List<String> requiredLiberalCourses,  // 반드시 이수해야 하는 필수교양 과목명
        boolean multiMajorRequired,           // 다전공(복수/부/심화/연계/마이크로/학생설계융합) 중 택1 필수 여부
        List<String> graduationProjectOptions // 졸업논문/졸업작품(캡스톤) 중 택1
) {
}
