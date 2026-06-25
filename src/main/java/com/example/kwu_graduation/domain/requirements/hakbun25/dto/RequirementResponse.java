package com.example.kwu_graduation.domain.requirements.hakbun25.dto;

import java.util.List;

/**
 * 학번·학과별 졸업요건 정보(조회용).
 * 성적/로그인 없이 "해당 학번의 졸업요건이 무엇인지"를 그대로 내려준다.
 * 2025·2026학번이 동일한 형태를 공유한다.
 */
public record RequirementResponse(
        int admissionYear,
        String college,
        String department,

        // 학점 요건
        int totalCredit,          // 졸업 총 이수학점
        int majorCredit,          // 주전공(필수 포함) 최소학점
        int liberalArtsCredit,    // 교양 총 이수학점(필수교양 + 균형교양)
        int balanceCredit,        // 균형교양 최소학점

        // 균형교양 영역 요건
        int balanceMinAreas,                      // 영역 중 최소 이수 영역 수
        List<String> balanceAreas,                // 전체 균형교양 영역
        List<String> balanceMandatoryAreasAnyOf,  // 이 중 최소 1개 영역은 반드시 포함

        // 과목·기타 요건
        List<String> requiredLiberalCourses,   // 반드시 이수해야 하는 필수교양 과목명
        boolean multiMajorRequired,            // 다전공(복수/부/심화/연계/마이크로/학생설계융합) 중 택1 필수 여부
        List<String> graduationProjectOptions, // 졸업논문/졸업작품(캡스톤) 중 택1

        // 공학인증(공학프로그램) 요건. 공학인증 학과만 값이 있고, 그 외 학과는 null.
        EngineeringRequirement engineering,

        // TOPCIT 응시 필수 여부(소프트웨어학부 true)
        boolean topcitRequired,

        // 세부전공(트랙)별 세부전공필수 요건. 세부전공이 있는 학과(소프트)만 값이 있고, 없으면 빈 목록.
        List<SubMajorRequirement> subMajors,

        // 성적만으로 자동 판정이 어려운 학과별 추가 요건(설계학점 등) 안내 문자열 목록.
        List<String> additionalRequirements
) {}
