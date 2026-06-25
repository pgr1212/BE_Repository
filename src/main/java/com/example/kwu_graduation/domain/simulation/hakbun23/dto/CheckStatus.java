package com.example.kwu_graduation.domain.simulation.hakbun23.dto;

/**
 * 개별 졸업요건 항목의 충족 상태.
 */
public enum CheckStatus {
    SATISFIED,     // 충족
    INSUFFICIENT,  // 미충족(부족)
    NEEDS_INPUT    // 성적 데이터만으로 자동 판정 불가 → 사용자 확인 필요
}
