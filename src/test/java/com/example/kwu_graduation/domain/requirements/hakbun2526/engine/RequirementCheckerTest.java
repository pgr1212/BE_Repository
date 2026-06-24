package com.example.kwu_graduation.domain.requirements.hakbun2526.engine;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckItem;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckStatus;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.EngineeringProgram;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.GraduationRequirement;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.MscArea;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.SubMajorRequirement;
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
                List.of(), true, List.of("졸업논문", "졸업작품"), null, false, List.of(), List.of());
    }

    private GraduationRequirement spec2026() {
        return new GraduationRequirement(
                2026, "인공지능융합대학", "정보융합학부",
                133, 45, 33, 30,
                4, ALL_AREAS, List.of("인간과 철학", "사회와 경제"),
                List.of("AI리터러시"), true, List.of("졸업논문", "졸업작품"), null, false, List.of(), List.of());
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
                new RequirementCheckRequest(true, null, null, null, null, null));

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
    @DisplayName("균형교양 영역에 없는 교양 과목은 기타로 집계된다")
    void nonBalanceCourseGoesToEtc() {
        RequirementCheckResponse res = checker.check(
                spec2025(),
                credits(133, 45, 31),
                // 외국어로서의 한국어 강좌(예시) + 필수교양 성격 과목은 균형교양 비인정 → 기타
                List.of(subject("한국어발음교육"), subject("AI리터러시")),
                RequirementCheckRequest.empty());

        assertEquals(0, find(res, "균형교양 이수학점").earned());
        assertEquals(6, find(res, "기타 교양 이수학점").earned());
    }

    @Test
    @DisplayName("학점 부족 시 미충족 항목과 부족분이 표시된다")
    void insufficientCredit() {
        RequirementCheckResponse res = checker.check(
                spec2025(),
                credits(120, 40, 31),
                List.of(subject("졸업작품")),
                new RequirementCheckRequest(true, null, null, null, null, null));

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
                new RequirementCheckRequest(true, true, null, null, null, null));

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
                new RequirementCheckRequest(true, true, null, null, null, null));

        assertEquals(CheckStatus.INSUFFICIENT, find(res, "필수교양 - AI리터러시").status());

        // 과목명에 공백이 있어도 매칭되어야 한다 + 균형교양/필수교양을 모두 충족하면 졸업 가능
        List<KlasSubjectGradeResponse> subjects = new ArrayList<>(balanceSubjects());
        subjects.add(subject("AI 리터러시"));
        subjects.add(subject("졸업작품"));

        RequirementCheckResponse res2 = checker.check(
                spec2026(),
                credits(133, 45, 33),
                subjects,
                new RequirementCheckRequest(true, true, null, null, null, null));
        assertEquals(CheckStatus.SATISFIED, find(res2, "필수교양 - AI리터러시").status());
        assertTrue(res2.graduatable());
    }

    // ===== 공학인증 학과(컴정공) — 공학/일반 프로그램 + MSC 판정 =====

    private static final List<String> CE_MSC_REQUIRED = List.of(
            "대학수학및연습1", "대학수학및연습2", "공학수학1",
            "대학물리학1", "대학물리학2", "대학화학및실험1", "C프로그래밍", "스마트사회와인공지능");
    private static final List<String> CE_MSC_POOL = List.of(
            "대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2", "선형대수학", "이산수학",
            "벡터해석학및연습", "확률및통계", "수치해석", "확률및불규칙신호론",
            "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2", "대학화학",
            "C프로그래밍", "스마트사회와인공지능");

    private GraduationRequirement computerSpec() {
        EngineeringProgram eng = new EngineeringProgram(
                27, CE_MSC_REQUIRED, List.of(), CE_MSC_POOL,
                List.of("공학설계입문", "수치해석"),
                List.of("산학협력캡스톤설계1", "산학협력캡스톤설계2"));
        return new GraduationRequirement(
                2025, "인공지능융합대학", "컴퓨터정보공학부",
                133, 45, 31, 30,
                4, ALL_AREAS, List.of(),
                List.of(), true, List.of("졸업논문", "졸업작품"), eng, false, List.of(), List.of());
    }

    /** 취득(완료)한 전공/MSC 과목 */
    private KlasSubjectGradeResponse mscSubject(String name, int credit) {
        return new KlasSubjectGradeResponse(
                name, "전공", credit, "A+", null, null, null,
                "Y", "1", "N", null, 2025, "Y", "Y");
    }

    @Test
    @DisplayName("공학인증 학과 - 프로그램 미선택 시 NEEDS_INPUT, 졸업 불가")
    void engineeringProgramNotSelected() {
        RequirementCheckResponse res = checker.check(
                computerSpec(), credits(133, 45, 31), List.of(),
                new RequirementCheckRequest(true, true, null, null, null, null));

        assertEquals(CheckStatus.NEEDS_INPUT, find(res, "졸업 프로그램 선택").status());
        assertFalse(res.graduatable());
    }

    @Test
    @DisplayName("일반프로그램 선택 시 MSC·공필 요건 면제(항목 미노출)")
    void generalProgramExemptsMsc() {
        RequirementCheckResponse res = checker.check(
                computerSpec(), credits(133, 45, 31), List.of(),
                new RequirementCheckRequest(true, true, null, false, null, null));

        assertEquals(CheckStatus.SATISFIED, find(res, "졸업 프로그램").status());
        assertTrue(res.items().stream().noneMatch(i -> i.name().equals("MSC 총 이수학점")));
        assertTrue(res.items().stream().noneMatch(i -> i.name().equals("MSC 필수과목 이수")));
    }

    @Test
    @DisplayName("공학프로그램 - MSC 필수과목·총학점·공필을 모두 충족")
    void engineeringProgramSatisfied() {
        List<KlasSubjectGradeResponse> subjects = new ArrayList<>();
        for (String name : CE_MSC_REQUIRED) {
            subjects.add(mscSubject(name, 3)); // 8과목 x 3 = 24학점
        }
        subjects.add(mscSubject("이산수학", 3));   // 풀 과목 추가 → 27학점
        subjects.add(mscSubject("공학설계입문", 3));
        subjects.add(mscSubject("수치해석", 3));   // 공필 + 풀
        subjects.add(mscSubject("산학협력캡스톤설계1", 3));

        RequirementCheckResponse res = checker.check(
                computerSpec(), credits(133, 45, 31), subjects,
                new RequirementCheckRequest(true, true, null, true, null, null));

        assertEquals(CheckStatus.SATISFIED, find(res, "졸업 프로그램").status());
        assertEquals(CheckStatus.SATISFIED, find(res, "MSC 필수과목 이수").status());
        CheckItem msc = find(res, "MSC 총 이수학점");
        assertEquals(CheckStatus.SATISFIED, msc.status());
        assertTrue(msc.earned() >= 27);
        assertEquals(CheckStatus.SATISFIED, find(res, "공학필수(공필) 과목 이수").status());
    }

    @Test
    @DisplayName("공학프로그램 - MSC 미달 시 미충족으로 표시")
    void engineeringProgramMscInsufficient() {
        RequirementCheckResponse res = checker.check(
                computerSpec(), credits(133, 45, 31),
                List.of(mscSubject("C프로그래밍", 3)),
                new RequirementCheckRequest(true, true, null, true, null, null));

        assertEquals(CheckStatus.INSUFFICIENT, find(res, "MSC 필수과목 이수").status());
        CheckItem msc = find(res, "MSC 총 이수학점");
        assertEquals(CheckStatus.INSUFFICIENT, msc.status());
        assertEquals(3, msc.earned());
        assertEquals(24, msc.lack());
    }

    // ===== 소프트웨어학부 — MSC 영역 + 세부전공 + TOPCIT =====

    private GraduationRequirement softwareSpec() {
        EngineeringProgram eng = new EngineeringProgram(
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
                List.of(
                        "대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
                        "선형대수학", "벡터해석학및연습", "확률및통계", "확률및불규칙신호론",
                        "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2",
                        "대학물리및실험1", "대학물리및실험2", "대학화학",
                        "C프로그래밍", "스마트사회와인공지능"),
                List.of("공학설계입문", "이산수학"),
                List.of("산학협력캡스톤설계1", "산학협력캡스톤설계2"));
        return new GraduationRequirement(
                2025, "인공지능융합대학", "소프트웨어학부",
                133, 45, 31, 30,
                4, ALL_AREAS, List.of(),
                List.of(), true, List.of("졸업논문", "졸업작품"), eng, true,
                List.of(
                        new SubMajorRequirement("소프트웨어전공", List.of(), 0),
                        new SubMajorRequirement("인공지능전공",
                                List.of("인공지능", "빅데이터처리및응용", "컴퓨터비전", "기계학습", "딥러닝실습"), 3)),
                List.of());
    }

    @Test
    @DisplayName("소프트 - 세부전공 미선택 시 NEEDS_INPUT")
    void subMajorNotSelected() {
        RequirementCheckResponse res = checker.check(
                softwareSpec(), credits(133, 45, 31), List.of(),
                new RequirementCheckRequest(true, true, null, false, true, null));

        assertEquals(CheckStatus.NEEDS_INPUT, find(res, "세부전공필수").status());
    }

    @Test
    @DisplayName("소프트 - 소프트웨어전공은 세부전공필수 없음(충족)")
    void softwareTrackHasNoRequired() {
        RequirementCheckResponse res = checker.check(
                softwareSpec(), credits(133, 45, 31), List.of(),
                new RequirementCheckRequest(true, true, null, false, true, "소프트웨어전공"));

        assertEquals(CheckStatus.SATISFIED, find(res, "세부전공필수 - 소프트웨어전공").status());
    }

    @Test
    @DisplayName("소프트 - 인공지능전공 3과목 이수 시 충족, 2과목이면 미충족")
    void aiTrackThreeCourses() {
        List<KlasSubjectGradeResponse> three = List.of(
                mscSubject("인공지능", 3), mscSubject("컴퓨터비전", 3), mscSubject("기계학습", 3));
        RequirementCheckResponse ok = checker.check(
                softwareSpec(), credits(133, 45, 31), three,
                new RequirementCheckRequest(true, true, null, false, true, "인공지능전공"));
        CheckItem item = find(ok, "세부전공필수 - 인공지능전공");
        assertEquals(CheckStatus.SATISFIED, item.status());
        assertEquals(3, item.earned());

        List<KlasSubjectGradeResponse> two = List.of(
                mscSubject("인공지능", 3), mscSubject("컴퓨터비전", 3));
        RequirementCheckResponse no = checker.check(
                softwareSpec(), credits(133, 45, 31), two,
                new RequirementCheckRequest(true, true, null, false, true, "인공지능전공"));
        assertEquals(CheckStatus.INSUFFICIENT, find(no, "세부전공필수 - 인공지능전공").status());
    }

    @Test
    @DisplayName("소프트 - TOPCIT 미확인 NEEDS_INPUT, 응시 시 충족")
    void topcitReported() {
        RequirementCheckResponse unknown = checker.check(
                softwareSpec(), credits(133, 45, 31), List.of(),
                new RequirementCheckRequest(true, true, null, false, null, "소프트웨어전공"));
        assertEquals(CheckStatus.NEEDS_INPUT, find(unknown, "TOPCIT 응시").status());

        RequirementCheckResponse done = checker.check(
                softwareSpec(), credits(133, 45, 31), List.of(),
                new RequirementCheckRequest(true, true, null, false, true, "소프트웨어전공"));
        assertEquals(CheckStatus.SATISFIED, find(done, "TOPCIT 응시").status());
    }

    @Test
    @DisplayName("소프트 공학프로그램 - MSC 영역별 학점이 영역 풀로 집계된다")
    void mscAreaAggregation() {
        List<KlasSubjectGradeResponse> subjects = List.of(
                mscSubject("대학수학및연습1", 3), mscSubject("대학수학및연습2", 3), mscSubject("공학수학1", 3), // 수학 9
                mscSubject("대학물리학1", 3),                                    // 기초과학 3
                mscSubject("C프로그래밍", 3), mscSubject("스마트사회와인공지능", 3)); // 공학기초 6 → 총 18

        RequirementCheckResponse res = checker.check(
                softwareSpec(), credits(133, 45, 31), subjects,
                new RequirementCheckRequest(true, true, null, true, true, "소프트웨어전공"));

        assertEquals(CheckStatus.SATISFIED, find(res, "MSC 수학 영역").status());
        assertEquals(9, find(res, "MSC 수학 영역").earned());
        assertEquals(CheckStatus.SATISFIED, find(res, "MSC 기초과학 영역").status());
        assertEquals(3, find(res, "MSC 기초과학 영역").earned());
        assertEquals(CheckStatus.SATISFIED, find(res, "MSC 공학기초 영역").status());
        CheckItem total = find(res, "MSC 총 이수학점");
        assertEquals(18, total.earned());
        assertEquals(CheckStatus.SATISFIED, total.status());
    }
}
