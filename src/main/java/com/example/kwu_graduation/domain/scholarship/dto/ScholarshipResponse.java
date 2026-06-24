package com.example.kwu_graduation.domain.scholarship.info.dto;

import java.util.List;

/**
 * 장학금 조회용 응답(조회 전용). 실제 판정(satisfied 계산)은 하지 않고,
 * "이 장학금의 조건이 뭔지, 그중 KLAS 데이터로 자동 판별 가능한 부분이 뭔지"를 구조화해 내려준다.
 *
 * <p>grade 의미 (장학금요약_버전.pdf 기준):
 * - A: KLAS 성적(직전학기 이수학점+평점)만으로 충족 여부를 계산할 수 있는 장학금
 * - B: 성적 조건은 자동 체크 가능하지만, 나머지(소득구간/추천/봉사인정 등)는 사용자 입력 필요
 * - C: 추천·심사·재단 자체 선정이 기준이라 앱이 판별 불가. 링크+안내만 제공
 */
public record ScholarshipResponse(
        String name,                          // 장학금명
        String grade,                         // "A" | "B" | "C"
        Integer minCreditsLastSemester,       // 직전학기 최소 이수학점(없으면 null)
        Integer minCreditsLastSemesterSenior, // 4학년 완화 기준(없으면 null, 보통 minCreditsLastSemester-3)
        Double minGpa,                        // 4.5만점 기준 최소 평점(없으면 null)
        List<String> manualCheckItems,        // 성적 외 사용자가 직접 확인해야 하는 조건들(B/C에서 사용)
        String amount,                         // 지원 금액
        String purpose,                        // 지원 목적(성적우수/가계곤란/대가성/기타 등)
        String overlapPolicy,                  // 중복지원 가능 여부 안내
        String note                            // 학과 자체 기준 우선, 매년 변경 가능 등 주의사항
) {}