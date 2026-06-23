package com.example.kwu_graduation.domain.requirements.hakbun23.dto;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;

import java.util.List;

public record RequirementCheckRequest(
        CreditSummaryResponse creditSummary,
        List<KlasSubjectGradeResponse> subjects,
        boolean isDoubleMajor,
        boolean isEngineeringProgram,   // 컴퓨터정보공학부, 소프트웨어학부 둘 다 사용
        Boolean topcitPassed,            // 소프트웨어학부만 사용 (외부 입력)
        Boolean thesisPassed,            // 소프트웨어학부만 사용 (외부 입력)
        String subMajor                  // 소프트웨어학부만 사용: "소프트웨어전공" 또는 "인공지능전공"
) {}