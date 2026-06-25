package com.example.kwu_graduation.domain.requirements.hakbun2526.spec;

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
        List<String> graduationProjectOptions,// 졸업논문/졸업작품(캡스톤) 중 택1

        // 공학인증(공학프로그램) 요건. 공학인증 학과(컴정공·소프트)만 값이 있고, 그 외 학과는 null.
        // 값이 있으면 공학/일반 프로그램 선택 및 MSC·공필 요건을 자동 판정한다.
        EngineeringProgram engineering,

        // TOPCIT 응시 필수 여부(소프트웨어학부 true). KLAS 성적으로 알 수 없어 자기보고 값으로 판정한다.
        boolean topcitRequired,

        // 세부전공(트랙)별 세부전공필수 요건. 세부전공이 있는 학과(소프트)만 값이 있고, 없으면 빈 목록.
        List<SubMajorRequirement> subMajors,

        // 성적(KLAS) 데이터만으로는 자동 판정이 어려운 학과별 추가 요건(설계학점 등).
        // 화면에 "확인 필요" 안내 항목으로 노출하기 위한 설명 문자열 목록이며, 없는 학과는 빈 목록.
        List<String> additionalRequirements
) {

    /** JSON에 일부 목록 필드가 빠져 있어도(null) NPE 없이 동작하도록 빈 목록으로 보정한다. */
    public GraduationRequirement {
        balanceAreas = balanceAreas == null ? List.of() : balanceAreas;
        balanceMandatoryAreasAnyOf = balanceMandatoryAreasAnyOf == null ? List.of() : balanceMandatoryAreasAnyOf;
        requiredLiberalCourses = requiredLiberalCourses == null ? List.of() : requiredLiberalCourses;
        graduationProjectOptions = graduationProjectOptions == null ? List.of() : graduationProjectOptions;
        subMajors = subMajors == null ? List.of() : subMajors;
        additionalRequirements = additionalRequirements == null ? List.of() : additionalRequirements;
    }
}
