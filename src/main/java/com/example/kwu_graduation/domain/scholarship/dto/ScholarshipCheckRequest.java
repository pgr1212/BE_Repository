package com.example.kwu_graduation.domain.scholarship.dto;

public record ScholarshipCheckRequest(
        int recentSemesterCredit,      // 직전학기 취득학점
        double recentSemesterGpa,      // 직전학기 평점
        int grade,                      // 학년 (1~4)
        Boolean isFinancialHardship,    // 가정형편 곤란 여부 (사용자 입력)
        Boolean hasDeptServiceRecognition, // 학과 봉사활동 인정 여부 (사용자 입력)
        Integer familyMembersEnrolled,  // 본인 포함 직계가족 재학 인원 (사용자 입력)
        Boolean hasCouncilOrOrgContribution // 학생회·교내단체 공로 여부 (사용자 입력)
) {}