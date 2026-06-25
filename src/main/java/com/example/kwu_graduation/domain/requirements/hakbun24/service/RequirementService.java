package com.example.kwu_graduation.domain.requirements.hakbun24.service;

import com.example.kwu_graduation.domain.requirements.hakbun24.dto.EngineeringRequirement;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.MscArea;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementResponse;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.SubMajorRequirement;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 2024학번 졸업요건 조회. 학사 정책 값을 그대로 상수로 담아 학과별로 내려준다.
 * department: jeongyung(정보융합학부) | computer(컴퓨터정보공학부) | software(소프트웨어학부)
 *
 * <p>23학번 대비 변경점(「24학번_졸업요건.pdf」 확인):
 * - 컴퓨터정보공학부 MSC 필수과목명: C프로그래밍→고급C프로그래밍및설계, 인공지능과컴퓨팅사고→스마트사회와인공지능
 * - 컴퓨터정보공학부 MSC 총학점 30→27, MSC 풀에서 이산수학·수치해석 제외(전공선택으로 분리)
 * - 소프트웨어학부는 24학번 자료집상 과목명이 아직 "인공지능과컴퓨팅사고"로 표기되어 있어 컴공과 다르게 유지함
 *   (25학번 코드에서는 SW도 "스마트사회와인공지능"으로 바뀐 걸 보면 25학번부터 변경된 것으로 보임 - 확인 필요)
 * - 정보융합학부 기초교양필수 과목명도 스마트사회와인공지능으로 변경
 */
@Service("requirementService2024")
public class RequirementService {

    private static final int ADMISSION_YEAR = 2024;
    private static final String COLLEGE = "인공지능융합대학";

    private static final int TOTAL_CREDIT = 133;
    private static final int MAJOR_CREDIT = 60; // 다전공 이수 시 54로 하향
    private static final int LIBERAL_ARTS_CREDIT = 31;
    private static final int BALANCE_CREDIT = 30;
    private static final int BALANCE_MIN_AREAS = 0; // 24학번도 "영역별 의무이수요건 없음"으로 자료집에 명시됨

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
                List.of("스마트사회와인공지능"), false, GRADUATION_PROJECT_OPTIONS,
                null, false, List.of(),
                List.of(
                        "캡스톤(졸업작품/캡스톤디자인/산학협력캡스톤설계1·2) 중 1개 이수 필요",
                        "다전공 이수 시 전공학점 60→54학점으로 하향"));
    }

    private RequirementResponse computer() {
        EngineeringRequirement engineering = new EngineeringRequirement(
                27,
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1",
                        "대학물리학1", "대학물리학2", "고급C프로그래밍및설계", "스마트사회와인공지능"),
                List.of(),
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                        "선형대수학", "벡터해석학및연습", "확률및통계",
                        "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2", "대학화학",
                        "고급C프로그래밍및설계", "스마트사회와인공지능"),
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
                        "TOPCIT 필수 응시 대상인지 학과 확인 필요"));
    }

    private RequirementResponse software() {
        EngineeringRequirement engineering = new EngineeringRequirement(
                12,
                List.of("C프로그래밍", "인공지능과컴퓨팅사고"),
                List.of(
                        new MscArea("수학", 6, List.of(
                                "대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                                "선형대수학", "벡터해석학및연습", "확률및통계")),
                        new MscArea("기초과학", 3, List.of(
                                "대학물리학1", "대학물리학2", "대학물리및실험1", "대학물리및실험2",
                                "대학화학및실험1", "대학화학및실험2", "대학화학"))),
                List.of("대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                        "선형대수학", "벡터해석학및연습", "확률및통계",
                        "대학물리학1", "대학물리학2", "대학물리및실험1", "대학물리및실험2",
                        "대학화학및실험1", "대학화학및실험2", "대학화학",
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
                        "졸업논문(작품)은 '택1'이 아니라 '가' 판정 필수 요건임",
                        "졸업논문 제출 시 산학협력캡스톤설계1/2 이수 필요(2027학년도 2월 졸업예정자부터 시행)",
                        "다전공 이수 시 전공학점 60→54학점으로 하향"));
    }
}