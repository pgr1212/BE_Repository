package com.example.kwu_graduation.domain.requirements.hakbun25.dto;

import java.util.List;

/**
 * 세부전공(트랙)별 세부전공필수 요건. 세부전공이 있는 학과(소프트웨어학부)만 사용한다.
 */
public record SubMajorRequirement(
        String name,                 // 세부전공명 (소프트웨어전공 / 인공지능전공)
        List<String> requiredCourses,
        int requiredCount            // 위 과목 중 최소 이수 과목 수
) {}
