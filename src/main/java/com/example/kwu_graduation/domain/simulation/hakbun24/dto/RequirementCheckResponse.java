package com.example.kwu_graduation.domain.requirements.hakbun24.dto;

import java.util.List;

/**
 * 졸업요건 점검 응답.
 *
 * @param admissionYear 입학 학번
 * @param department    학과명
 * @param graduatable   모든 항목 충족 여부(하나라도 미충족/확인필요면 false)
 * @param totalRequired 졸업 총 필요학점
 * @param totalEarned   취득 총 학점
 * @param items         항목별 점검 결과
 */
public record RequirementCheckResponse(
        int admissionYear,
        String department,
        boolean graduatable,
        int totalRequired,
        int totalEarned,
        List<CheckItem> items
) {
}
