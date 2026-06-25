package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.grade.service.GradeCalculator;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSummaryResponse;
import com.example.kwu_graduation.domain.simulation.model.CourseCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GraduationCreditCalculator {

    private final GradeCalculator gradeCalculator;

    public GraduationSummaryResponse calculate(
            List<KlasSemesterGradeResponse> semesters,
            boolean includeInProgressCourses
    ) {
        CreditSummaryResponse summary = calculateCredits(semesters, includeInProgressCourses);
        return new GraduationSummaryResponse(
                summary.chidukHakjum(),
                summary.majorChidukHakjum(),
                summary.cultureChidukHakjum(),
                summary.etcChidukHakjum()
        );
    }

    public CreditSummaryResponse calculateCredits(
            List<KlasSemesterGradeResponse> semesters,
            boolean includeInProgressCourses
    ) {
        CreditSummaryResponse summary = gradeCalculator.calculate(normalizeSemesters(semesters));
        if (!includeInProgressCourses) {
            return summary;
        }

        return new CreditSummaryResponse(
                0,
                0,
                0,
                0,
                summary.chidukHakjum() + summary.applyHakjum(),
                summary.majorChidukHakjum() + summary.majorApplyHakjum(),
                summary.cultureChidukHakjum() + summary.cultureApplyHakjum(),
                summary.etcChidukHakjum() + summary.etcApplyHakjum(),
                summary.delHakjum(),
                summary.majorDelHakjum(),
                summary.cultureDelHakjum(),
                summary.etcDelHakjum(),
                0,
                summary.retakeChidukHakjum() + summary.retakeApplyHakjum(),
                summary.retakeDelHakjum()
        );
    }

    public int completedCreditByArea(List<KlasSemesterGradeResponse> semesters, String areaCode) {
        if (areaCode == null || areaCode.isBlank()) {
            return 0;
        }

        return safeSemesters(semesters).stream()
                .flatMap(semester -> safeSubjects(semester).stream())
                .filter(this::isCompleted)
                .filter(subject -> areaCode.equalsIgnoreCase(subject.certname()))
                .mapToInt(subject -> subject.hakjumNum() == null ? 0 : subject.hakjumNum())
                .sum();
    }

    public boolean hasCompletedRequiredCourse(
            List<KlasSemesterGradeResponse> semesters,
            String courseCode,
            String courseName
    ) {
        return safeSemesters(semesters).stream()
                .flatMap(semester -> safeSubjects(semester).stream())
                .filter(this::isCompleted)
                .anyMatch(subject -> matchesCourse(subject, courseCode, courseName));
    }

    public CourseCategory categoryOf(KlasSubjectGradeResponse subject) {
        return CourseCategory.fromKlasCodeName(subject.codeName1());
    }

    private List<KlasSemesterGradeResponse> safeSemesters(List<KlasSemesterGradeResponse> semesters) {
        return semesters == null ? List.of() : semesters;
    }

    private List<KlasSemesterGradeResponse> normalizeSemesters(List<KlasSemesterGradeResponse> semesters) {
        return safeSemesters(semesters).stream()
                .map(semester -> {
                    if (semester == null) {
                        return new KlasSemesterGradeResponse(null, null, null, List.of());
                    }
                    return new KlasSemesterGradeResponse(
                            semester.thisYear(),
                            semester.hakgi(),
                            semester.hakgiOrder(),
                            safeSubjects(semester)
                    );
                })
                .toList();
    }

    private List<KlasSubjectGradeResponse> safeSubjects(KlasSemesterGradeResponse semester) {
        if (semester == null || semester.sungjukList() == null) {
            return List.of();
        }
        return semester.sungjukList();
    }

    private boolean isCompleted(KlasSubjectGradeResponse subject) {
        return "Y".equals(subject.finishOpt())
                && "Y".equals(subject.termFinish())
                && !"3".equals(subject.sungjukOpt())
                && (subject.getGrade() == null || !subject.getGrade().contains("삭제"));
    }

    private boolean matchesCourse(KlasSubjectGradeResponse subject, String courseCode, String courseName) {
        if (courseCode != null && !courseCode.isBlank() && courseCode.equalsIgnoreCase(subject.hakjungNo())) {
            return true;
        }
        return courseName != null && !courseName.isBlank() && courseName.equalsIgnoreCase(subject.gwamokKname());
    }
}
