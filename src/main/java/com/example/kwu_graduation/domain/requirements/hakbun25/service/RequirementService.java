package com.example.kwu_graduation.domain.requirements.hakbun25.service;

import com.example.kwu_graduation.domain.requirements.hakbun25.dto.EngineeringRequirement;
import com.example.kwu_graduation.domain.requirements.hakbun25.dto.MscArea;
import com.example.kwu_graduation.domain.requirements.hakbun25.dto.RequirementResponse;
import com.example.kwu_graduation.domain.requirements.hakbun25.dto.SubMajorRequirement;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 2025학번 졸업요건 조회. 학사 정책 값을 그대로 상수로 담아 학과별로 내려준다.
 * department: jeongyung(정보융합학부) | computer(컴퓨터정보공학부) | software(소프트웨어학부)
 */
@Service("requirementService2025")
public class RequirementService {

    private static final int ADMISSION_YEAR = 2025;
    private static final String COLLEGE = "인공지능융합대학";

    // 학점 요건(세 학과 공통)
    private static final int TOTAL_CREDIT = 133;
    private static final int MAJOR_CREDIT = 45;
    private static final int LIBERAL_ARTS_CREDIT = 31;
    private static final int BALANCE_CREDIT = 30;
    private static final int BALANCE_MIN_AREAS = 4;

    // 2025학번 균형교양 8영역
    private static final List<String> BALANCE_AREAS = List.of(
            "언어와 표현", "과학과 기술", "인간과 철학", "사회와 경제",
            "글로벌문화와 제2외국어", "예술과 체육", "수리와 자연", "대학실용영어");

    private static final List<String> GRADUATION_PROJECT_OPTIONS = List.of("졸업논문", "졸업작품");

    public RequirementResponse get(String department) {
        return switch (department) {
            case "jeongyung" -> jeongyung();
            case "computer" -> computer();
            case "software" -> software();
            default -> throw new IllegalArgumentException("지원하지 않는 학과입니다: " + department);
        };
    }

    private RequirementResponse jeongyung() {
        return new RequirementResponse(
                ADMISSION_YEAR, COLLEGE, "정보융합학부",
                TOTAL_CREDIT, MAJOR_CREDIT, LIBERAL_ARTS_CREDIT, BALANCE_CREDIT,
                BALANCE_MIN_AREAS, BALANCE_AREAS, List.of(),
                List.of(), true, GRADUATION_PROJECT_OPTIONS,
                null, false, List.of(), List.of());
    }

    private RequirementResponse computer() {
        EngineeringRequirement engineering = new EngineeringRequirement(
                27,
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1",
                        "대학물리학1", "대학화학및실험1", "C프로그래밍", "스마트사회와인공지능"),
                List.of(),
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                        "선형대수학", "벡터해석학및연습", "확률및통계", "확률및불규칙신호론",
                        "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2", "대학화학",
                        "C프로그래밍", "스마트사회와인공지능"),
                List.of("공학설계입문", "수치해석"),
                List.of("산학협력캡스톤설계", "산학협력캡스톤설계1", "산학협력캡스톤설계2"));

        return new RequirementResponse(
                ADMISSION_YEAR, COLLEGE, "컴퓨터정보공학부",
                TOTAL_CREDIT, MAJOR_CREDIT, LIBERAL_ARTS_CREDIT, BALANCE_CREDIT,
                BALANCE_MIN_AREAS, BALANCE_AREAS, List.of(),
                List.of(), true, GRADUATION_PROJECT_OPTIONS,
                engineering, false, List.of(),
                List.of(
                        "설계 12학점 이수 여부는 공학인증 사이트에서 확인 (https://ce.kw.ac.kr/engineering_certify/subject_info.php)",
                        "세부전공: 공통 포함 총 30학점 이수 (본인 세부전공 2과목 이상 + 타 세부전공 1과목 이상, 컴퓨터정보공학부 개설 교과목만 인정). 세부전공: 지능컴퓨팅시스템전공 / 지능정보전공"));
    }

    private RequirementResponse software() {
        EngineeringRequirement engineering = new EngineeringRequirement(
                18,
                List.of("C프로그래밍", "스마트사회와인공지능"),
                List.of(
                        new MscArea("수학", 6, List.of(
                                "대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                                "선형대수학", "벡터해석학및연습", "확률및통계", "확률및불규칙신호론")),
                        new MscArea("기초과학", 3, List.of(
                                "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2",
                                "대학물리및실험1", "대학물리및실험2", "대학화학")),
                        new MscArea("공학기초", 6, List.of("C프로그래밍", "스마트사회와인공지능"))),
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                        "선형대수학", "벡터해석학및연습", "확률및통계", "확률및불규칙신호론",
                        "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2",
                        "대학물리및실험1", "대학물리및실험2", "대학화학",
                        "C프로그래밍", "스마트사회와인공지능"),
                List.of("공학설계입문", "이산수학"),
                List.of("산학협력캡스톤설계1", "산학협력캡스톤설계2", "산학협력캡스톤설계"));

        List<SubMajorRequirement> subMajors = List.of(
                new SubMajorRequirement("소프트웨어전공", List.of(), 0),
                new SubMajorRequirement("인공지능전공",
                        List.of("인공지능", "빅데이터처리및응용", "컴퓨터비전", "기계학습", "딥러닝실습"), 3));

        return new RequirementResponse(
                ADMISSION_YEAR, COLLEGE, "소프트웨어학부",
                TOTAL_CREDIT, MAJOR_CREDIT, LIBERAL_ARTS_CREDIT, BALANCE_CREDIT,
                BALANCE_MIN_AREAS, BALANCE_AREAS, List.of(),
                List.of(), true, GRADUATION_PROJECT_OPTIONS,
                engineering, true, subMajors,
                List.of("설계 12학점 이수 여부는 공학인증 사이트에서 확인 (https://cs.kw.ac.kr/engineering_certify/subject.php)"));
    }
}
