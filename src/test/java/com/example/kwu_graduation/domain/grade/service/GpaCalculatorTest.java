package com.example.kwu_graduation.domain.grade.service;

import com.example.kwu_graduation.domain.grade.dto.GpaStats;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GpaCalculatorTest {

    private final GpaCalculator calculator = new GpaCalculator();

    @Test
    @DisplayName("완료 과목의 학점 가중 평점과 표준편차를 계산한다")
    void calculateGpaAndStd() {
        List<KlasSemesterGradeResponse> semesters = List.of(new KlasSemesterGradeResponse(
                "2026", "1", "1",
                List.of(
                        completed("전공A", "전공", 3, "A+"), // 4.5
                        completed("전공B", "전공", 3, "B0")  // 3.0
                )
        ));

        GpaStats stats = calculator.calculate(semesters);

        // (4.5*3 + 3.0*3) / 6 = 3.75
        assertEquals(3.75, stats.gpa());
        // sqrt((3*0.75^2 + 3*0.75^2)/6) = 0.75
        assertEquals(0.75, stats.stdDeviation());
        assertEquals(6, stats.gradedCredits());
        assertEquals(2, stats.gradedCourseCount());
    }

    @Test
    @DisplayName("P/NP·삭제·수강중 과목은 평점 산출에서 제외한다")
    void excludeNonGradedCourses() {
        List<KlasSemesterGradeResponse> semesters = List.of(new KlasSemesterGradeResponse(
                "2026", "1", "1",
                List.of(
                        completed("전공A", "전공", 3, "A0"),     // 4.0 포함
                        completed("PNP과목", "교양", 3, "P"),     // 평점 없음 제외
                        deleted("삭제과목", "전공", 3),            // 삭제 제외
                        inProgress("수강중", "전공", 3)            // 수강중 제외
                )
        ));

        GpaStats stats = calculator.calculate(semesters);

        assertEquals(4.0, stats.gpa());
        assertEquals(3, stats.gradedCredits());
        assertEquals(1, stats.gradedCourseCount());
    }

    @Test
    @DisplayName("평점 대상 과목이 없으면 0을 반환한다")
    void emptyReturnsZero() {
        GpaStats stats = calculator.calculate(List.of());
        assertEquals(0.0, stats.gpa());
        assertEquals(0, stats.gradedCourseCount());
    }

    private KlasSubjectGradeResponse completed(String name, String codeName, int credit, String grade) {
        return subject(name, codeName, credit, grade, "Y", "1", "Y");
    }

    private KlasSubjectGradeResponse deleted(String name, String codeName, int credit) {
        return subject(name, codeName, credit, "삭제", "Y", "3", "Y");
    }

    private KlasSubjectGradeResponse inProgress(String name, String codeName, int credit) {
        return subject(name, codeName, credit, "", "Y", "1", "N");
    }

    private KlasSubjectGradeResponse subject(
            String name, String codeName, int credit, String grade,
            String finishOpt, String sungjukOpt, String termFinish
    ) {
        return new KlasSubjectGradeResponse(
                name, codeName, credit, grade, null, null, null,
                finishOpt, sungjukOpt, "N", null, 2026, null, termFinish
        );
    }
}
