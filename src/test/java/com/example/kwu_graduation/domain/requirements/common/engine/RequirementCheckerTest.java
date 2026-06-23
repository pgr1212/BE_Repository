package com.example.kwu_graduation.domain.requirements.common.engine;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.requirements.common.dto.CheckItem;
import com.example.kwu_graduation.domain.requirements.common.dto.CheckStatus;
import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.common.spec.GraduationRequirement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementCheckerTest {

    private final RequirementChecker checker = new RequirementChecker(new BalanceAreaCatalog());

    private static final List<String> ALL_AREAS = List.of(
            "언어와 표현", "과학과 기술", "인간과 철학", "사회와 경제",
            "글로벌문화와 제2외국어", "예술과 체육", "수리와 자연", "기초학문융합", "대학실용영어");

    private GraduationRequirement spec2025() {
        return new GraduationRequirement(
                2025, "인공지능융합대학", "정보융합학부",
                133, 45, 31, 30,
                4, ALL_AREAS, List.of(),
                List.of(), true, List.of("졸업논문", "졸업작품"));
    }

    private GraduationRequirement spec2026() {
        return new GraduationRequirement(
                2026, "인공지능융합대학", "정보융합학부",
                133, 45, 33, 30,
                4, ALL_AREAS, List.of("인간과 철학", "사회와 경제"),
                List.of("AI리터러시"), true, List.of("졸업논문", "졸업작품"));
    }

    /** chiduk(취득) 학점만 채운 요약 */
    private CreditSummaryResponse credits(int total, int major, int culture) {
        return new CreditSummaryResponse(
                0, 0, 0, 0,
                total, major, culture, 0,
                0, 0, 0, 0,
                0, 0, 0);
    }

    /** 취득(완료)한 교양 3학점 과목 */
    private KlasSubjectGradeResponse subject(String name) {
        return new KlasSubjectGradeResponse(
                name, "교양", 3, "A+", null, null, null,
                "Y", "1", "N", null, 2025, "Y", "Y");
    }

    /** 4개 영역(언어와 표현·과학과 기술·인간과 철학·사회와 경제)에 걸친 균형교양 10과목 30학점 */
    private List<KlasSubjectGradeResponse> balanceSubjects() {
        List<KlasSubjectGradeResponse> list = new ArrayList<>();
        list.add(subject("발표와설득전략"));
        list.add(subject("한국문학읽기"));
        list.add(subject("문화비평연습"));
        list.add(subject("C프로그래밍"));
        list.add(subject("생활속의과학"));
        list.add(subject("자연과학사"));
        list.add(subject("과학철학의이해"));
        list.add(subject("현대사회와윤리"));
        list.add(subject("법과생활"));
        list.add(subject("생활속의경제"));
        return list;
    }

    private CheckItem find(RequirementCheckResponse res, String name) {
        return res.items().stream()
                .filter(i -> i.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("2025 정융 - 모든 요건 충족 시 졸업 가능")
    void allSatisfied() {
        List<KlasSubjectGradeResponse> subjects = new ArrayList<>(balanceSubjects());
        subjects.add(subject("졸업작품"));

        RequirementCheckResponse res = checker.check(
                spec2025(),
                credits(133, 45, 31),
                subjects,
                new RequirementCheckRequest(true, null, null));

        assertTrue(res.graduatable());
        assertEquals(CheckStatus.SATISFIED, find(res, "졸업 총 이수학점").status());
        assertEquals(CheckStatus.SATISFIED, find(res, "주전공(필수 포함)").status());
        assertEquals(CheckStatus.SATISFIED, find(res, "다전공 택1").status());
        assertEquals(CheckStatus.SATISFIED, find(res, "졸업논문/졸업작품 택1").status());
    }

    @Test
    @DisplayName("균형교양 영역 수와 학점이 과목명으로부터 자동 집계된다")
    void balanceAutoClassified() {
        RequirementCheckResponse res = checker.check(
                spec2025(),
                credits(133, 45, 31),
                balanceSubjects(),
                RequirementCheckRequest.empty());

        CheckItem areaItem = find(res, "균형교양 영역 충족");
        assertEquals(4, areaItem.earned());
        assertEquals(CheckStatus.SATISFIED, areaItem.status());

        CheckItem creditItem = find(res, "균형교양 이수학점");
        assertEquals(30, creditItem.earned());
        assertEquals(CheckStatus.SATISFIED, creditItem.status());
    }

    @Test
    @DisplayName("균형교양 영역에 없는 교양 과목은 균형교양에 집계되지 않고, 기타·교양총계 항목은 표시하지 않는다")
    void nonBalanceCourseNotCountedAndNotShown() {
        RequirementCheckResponse res = checker.check(
                spec2025(),
                credits(133, 45, 31),
                // 외국어로서의 한국어 강좌(예시) 등 비인정 과목은 균형교양에 들어가지 않음
                List.of(subject("한국어발음교육"), subject("졸업작품")),
                RequirementCheckRequest.empty());

        assertEquals(0, find(res, "균형교양 이수학점").earned());
        assertTrue(res.items().stream().noneMatch(i -> i.name().equals("기타 교양 이수학점")));
        assertTrue(res.items().stream().noneMatch(i -> i.name().equals("교양 총 이수학점")));
    }

    @Test
    @DisplayName("학점 부족 시 미충족 항목과 부족분이 표시된다")
    void insufficientCredit() {
        RequirementCheckResponse res = checker.check(
                spec2025(),
                credits(120, 40, 31),
                List.of(subject("졸업작품")),
                new RequirementCheckRequest(true, null, null));

        assertFalse(res.graduatable());
        CheckItem total = find(res, "졸업 총 이수학점");
        assertEquals(CheckStatus.INSUFFICIENT, total.status());
        assertEquals(13, total.lack());
        assertEquals(5, find(res, "주전공(필수 포함)").lack());
    }

    @Test
    @DisplayName("이수 과목이 없으면 균형교양 영역 충족은 미충족, 자기보고 항목은 NEEDS_INPUT")
    void emptyInput() {
        RequirementCheckResponse res = checker.check(
                spec2025(),
                credits(133, 45, 31),
                List.of(),
                RequirementCheckRequest.empty());

        assertFalse(res.graduatable());
        assertEquals(CheckStatus.NEEDS_INPUT, find(res, "다전공 택1").status());
        assertEquals(CheckStatus.INSUFFICIENT, find(res, "균형교양 영역 충족").status());
        assertEquals(CheckStatus.NEEDS_INPUT, find(res, "졸업논문/졸업작품 택1").status());
    }

    @Test
    @DisplayName("2026 이공계열 - 필수 포함 영역(인간과철학/사회와경제) 미이수 시 균형교양 영역 미충족")
    void mandatoryAreaMissing() {
        // 4개 영역을 이수했지만 인간과철학/사회와경제가 모두 빠진 경우
        List<KlasSubjectGradeResponse> subjects = List.of(
                subject("발표와설득전략"), subject("한국문학읽기"),      // 언어와 표현
                subject("C프로그래밍"), subject("생활속의과학"),         // 과학과 기술
                subject("초급중국어1"), subject("초급일본어1"),          // 글로벌문화와 제2외국어
                subject("연극의이해"), subject("미술의이해"),            // 예술과 체육
                subject("경영통계"), subject("선형대수학"));             // 수리와 자연

        RequirementCheckResponse res = checker.check(
                spec2026(),
                credits(133, 45, 33),
                subjects,
                new RequirementCheckRequest(true, true, null));

        CheckItem areaItem = find(res, "균형교양 영역 충족");
        assertEquals(5, areaItem.earned());
        assertEquals(CheckStatus.INSUFFICIENT, areaItem.status());
    }

    @Test
    @DisplayName("2026 필수교양(AI리터러시) 미이수 시 미충족, 이수 시 졸업 가능")
    void requiredLiberalCourseMissing() {
        RequirementCheckResponse res = checker.check(
                spec2026(),
                credits(133, 45, 33),
                List.of(subject("자료구조")),
                new RequirementCheckRequest(true, true, null));

        assertEquals(CheckStatus.INSUFFICIENT, find(res, "필수교양 - AI리터러시").status());

        // 과목명에 공백이 있어도 매칭되어야 한다 + 균형교양/필수교양을 모두 충족하면 졸업 가능
        List<KlasSubjectGradeResponse> subjects = new ArrayList<>(balanceSubjects());
        subjects.add(subject("AI 리터러시"));
        subjects.add(subject("졸업작품"));

        RequirementCheckResponse res2 = checker.check(
                spec2026(),
                credits(133, 45, 33),
                subjects,
                new RequirementCheckRequest(true, true, null));
        assertEquals(CheckStatus.SATISFIED, find(res2, "필수교양 - AI리터러시").status());
        assertTrue(res2.graduatable());
    }
}
