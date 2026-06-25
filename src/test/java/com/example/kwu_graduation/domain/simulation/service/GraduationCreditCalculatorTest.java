package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.grade.service.GradeCalculator;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraduationCreditCalculatorTest {

    private final GraduationCreditCalculator calculator = new GraduationCreditCalculator(new GradeCalculator());

    @Test
    @DisplayName("현재 수강 중 과목 제외 시 취득 학점만 summary에 반영한다")
    void calculateSummaryWithoutInProgressCourses() {
        GraduationSummaryResponse summary = calculator.calculate(sampleSemesters(), false);

        assertEquals(9, summary.totalCredit());
        assertEquals(3, summary.majorCredit());
        assertEquals(3, summary.cultureCredit());
        assertEquals(3, summary.etcCredit());
    }

    @Test
    @DisplayName("현재 수강 중 과목 포함 시 신청 학점을 취득 학점처럼 합산한다")
    void calculateSummaryWithInProgressCourses() {
        GraduationSummaryResponse summary = calculator.calculate(sampleSemesters(), true);

        assertEquals(15, summary.totalCredit());
        assertEquals(6, summary.majorCredit());
        assertEquals(6, summary.cultureCredit());
        assertEquals(3, summary.etcCredit());
    }

    @Test
    @DisplayName("졸업요건 checker에 넘길 raw 학점도 현재 수강 중 포함 옵션을 반영한다")
    void calculateCreditsMergesInProgressIntoCompletedCredits() {
        CreditSummaryResponse credits = calculator.calculateCredits(sampleSemesters(), true);

        assertEquals(0, credits.applyHakjum());
        assertEquals(15, credits.chidukHakjum());
        assertEquals(6, credits.majorChidukHakjum());
        assertEquals(6, credits.cultureChidukHakjum());
        assertEquals(3, credits.etcChidukHakjum());
    }

    @Test
    @DisplayName("null 학기 목록과 null 과목 목록은 0학점으로 처리한다")
    void calculateHandlesNullLists() {
        GraduationSummaryResponse nullSummary = calculator.calculate(null, true);
        GraduationSummaryResponse emptySubjectSummary = calculator.calculate(
                List.of(new KlasSemesterGradeResponse("2026", "1", "1", null)),
                true
        );

        assertEquals(0, nullSummary.totalCredit());
        assertEquals(0, emptySubjectSummary.totalCredit());
    }

    private List<KlasSemesterGradeResponse> sampleSemesters() {
        return List.of(new KlasSemesterGradeResponse(
                "2026",
                "1",
                "1",
                List.of(
                        completedSubject("전공완료", "전공", 3),
                        completedSubject("교양완료", "교양", 3),
                        completedSubject("기타완료", "기타", 3),
                        inProgressSubject("전공수강중", "전공", 3),
                        inProgressSubject("교양수강중", "교양", 3),
                        deletedSubject("삭제과목", "전공", 3)
                )
        ));
    }

    private KlasSubjectGradeResponse completedSubject(String name, String codeName, int credit) {
        return subject(name, codeName, credit, "A0", "Y", "1", "Y");
    }

    private KlasSubjectGradeResponse inProgressSubject(String name, String codeName, int credit) {
        return subject(name, codeName, credit, "", "Y", "1", "N");
    }

    private KlasSubjectGradeResponse deletedSubject(String name, String codeName, int credit) {
        return subject(name, codeName, credit, "삭제", "Y", "3", "Y");
    }

    private KlasSubjectGradeResponse subject(
            String name,
            String codeName,
            int credit,
            String grade,
            String finishOpt,
            String sungjukOpt,
            String termFinish
    ) {
        return new KlasSubjectGradeResponse(
                name,
                codeName,
                credit,
                grade,
                null,
                null,
                null,
                finishOpt,
                sungjukOpt,
                "N",
                null,
                2026,
                null,
                termFinish
        );
    }
}
