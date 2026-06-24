package com.example.kwu_graduation.domain.requirements.hakbun24.dto;

/**
 * 졸업요건 점검 시 KLAS 성적 데이터만으로는 자동 판정이 불가능한 항목에 대한
 * 사용자 자기보고 값. 성적(creditSummary)과 과목 목록(subjects)은 더 이상
 * 클라이언트가 직접 보내지 않고, 서버가 Klas-Cookie 헤더로 KLAS에서 직접 조회한다.
 */
public record RequirementCheckRequest(
        boolean isDoubleMajor,
        boolean isEngineeringProgram,   // 컴퓨터정보공학부, 소프트웨어학부 둘 다 사용
        Boolean topcitPassed,            // 컴퓨터정보공학부 + 소프트웨어학부 둘 다 사용 (외부 입력)
        Boolean thesisPassed,            // 소프트웨어학부만 사용 (외부 입력)
        String subMajor                  // 소프트웨어학부만 사용: "소프트웨어전공" 또는 "인공지능전공"
) {}