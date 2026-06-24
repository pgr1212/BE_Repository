package com.example.kwu_graduation.domain.requirements.hakbun25.dto;

import java.util.List;

/**
 * 공학인증 MSC(수학·기초과학·공학기초) 영역별 최소학점 요건.
 * 영역 구분이 없는 학과(컴퓨터정보공학부)는 빈 목록으로 둔다.
 */
public record MscArea(
        String name,        // 영역명 (수학 / 기초과학 / 공학기초)
        int minCredit,      // 해당 영역 최소 이수학점
        List<String> courses
) {}
