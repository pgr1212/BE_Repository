package com.example.kwu_graduation.domain.requirements.hakbun23.service;

import com.example.kwu_graduation.domain.requirements.hakbun23.dto.EngineeringRequirement;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.SubMajorRequirement;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 2020~2023학번(20~23학번 공통) 졸업요건 조회. 학사 정책 값을 그대로 상수로 담아 학과별로 내려준다.
 * department: jeongyung(정보융합학부) | computer(컴퓨터정보공학부) | software(소프트웨어학부)
 *
 * <p>※ 「Ⅱ-1 수강신청자료집 전체.pdf」, 「2023학번 졸업요건.pdf」 기준으로 작성.
 * 아래 두 항목은 학과 확인이 필요함(머지 전 재검토 권장):
 * 1) 컴퓨터정보공학부 TOPCIT 응시 필수 여부 - 자료상 소프트웨어학부와 함께 적용 대상으로 보이나
 *    기존 코드엔 빠져있었음.
 * 2) 소프트웨어학부 졸업논문은 "졸업논문/졸업작품 중 택1"이 아니라 "졸업논문(작품) '가' 판정"이
 *    별도 필수 요건임 - graduationProjectOptions 필드로는 이 뉘앙스가 완전히 표현되지 않아
 *    additionalRequirements 텍스트로만 안내함.
 */
@Service("requirementService2023")
public class RequirementService {

    private static final int ADMISSION_YEAR = 2023; // 2020~2023학번 공통 수치
    private static final String COLLEGE = "인공지능융합대학";

    private static final int TOTAL_CREDIT = 133;
    private static final int MAJOR_CREDIT = 60; // 다전공 이수 시 54로 하향(아래 additionalRequirements 안내)
    private static final int LIBERAL_ARTS_CREDIT = 22;
    private static final int BALANCE_CREDIT = 22; // 필수교양(광운인되기 1학점, 폐지로 균형교양에 흡수)+균형교양21학점
    private static final int BALANCE_MIN_AREAS = 0; // 20~23학번은 영역별 의무이수요건 없음(7과목 총 21학점만 충족하면 됨)

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
                List.of("인공지능과컴퓨팅사고"), false, GRADUATION_PROJECT_OPTIONS,
                null, false, List.of(),
                List.of(
                        "캡스톤(졸업작품/캡스톤디자인/산학협력캡스톤설계1·2) 중 1개 이수 필요",
                        "다전공 이수 시 전공학점 60→54학점으로 하향"));
    }

    private RequirementResponse computer() {
        EngineeringRequirement engineering = new EngineeringRequirement(
                30,
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1",
                        "대학물리학1", "대학물리학2", "C프로그래밍", "인공지능과컴퓨팅사고"),
                List.of(),
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                        "선형대수학", "이산수학", "벡터해석학및연습", "확률및통계", "수치해석", "확률및불규칙신호론",
                        "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2", "대학화학",
                        "C프로그래밍", "인공지능과컴퓨팅사고"),
                List.of("공학설계입문", "이산수학"),
                List.of("산학협력캡스톤설계1", "산학협력캡스톤설계2"));

        return new RequirementResponse(
                ADMISSION_YEAR, COLLEGE, "컴퓨터정보공학부",
                TOTAL_CREDIT, MAJOR_CREDIT, LIBERAL_ARTS_CREDIT, BALANCE_CREDIT,
                BALANCE_MIN_AREAS, BALANCE_AREAS, List.of(),
                List.of(), false, GRADUATION_PROJECT_OPTIONS,
                engineering, true, List.of(),
                List.of(
                        "설계 12학점 포함 여부는 KLAS 성적표 비고란/공학인증 사이트로 확인 필요",
                        "다전공 이수 시 전공학점 60→54학점으로 하향",
                        "TOPCIT 필수 응시 대상인지 학과 확인 필요(기존 코드에는 소프트웨어학부 전용으로만 적용돼 있었음)"));
    }

    private RequirementResponse software() {
        EngineeringRequirement engineering = new EngineeringRequirement(
                12,
                List.of("C프로그래밍", "인공지능과컴퓨팅사고"),
                List.of(),
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                        "선형대수학", "이산수학", "벡터해석학및연습", "확률및통계", "수치해석", "확률및불규칙신호론",
                        "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2", "대학화학",
                        "C프로그래밍", "인공지능과컴퓨팅사고"),
                List.of("공학설계입문", "이산수학"),
                List.of("산학협력캡스톤설계1", "산학협력캡스톤설계2"));

        List<SubMajorRequirement> subMajors = List.of(
                new SubMajorRequirement("소프트웨어전공", List.of(), 0),
                new SubMajorRequirement("인공지능전공",
                        List.of("인공지능", "빅데이터처리및응용", "컴퓨터비전", "기계학습", "딥러닝실습"), 3));

        return new RequirementResponse(
                ADMISSION_YEAR, COLLEGE, "소프트웨어학부",
                TOTAL_CREDIT, MAJOR_CREDIT, LIBERAL_ARTS_CREDIT, BALANCE_CREDIT,
                BALANCE_MIN_AREAS, BALANCE_AREAS, List.of(),
                List.of(), false, GRADUATION_PROJECT_OPTIONS,
                engineering, true, subMajors,
                List.of(
                        "전공필수 4과목(자료구조, 자료구조실습, 알고리즘, 응용소프트웨어실습) 이수 필요",
                        "졸업논문(작품)은 '택1'이 아니라 '가' 판정 필수 요건임(graduationProjectOptions로는 이 뉘앙스 표현 안 됨)",
                        "다전공 이수 시 전공학점 60→54학점으로 하향"));
    }
}