package com.example.kwu_graduation.domain.grade.service;

import com.example.kwu_graduation.domain.grade.dto.GpaStats;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * KLAS 성적(과목별 등급)으로부터 누적 평점과 평점 편차를 계산한다.
 *
 * <p>광운대 4.5 만점 등급 체계를 사용하며, P/NP 등 평점이 없는 과목과
 * 재수강으로 삭제된 과목은 평점 산출에서 제외한다.
 */
@Service
public class GpaCalculator {

    /** 등급 -> 평점(4.5 만점) 매핑. P/NP/S/U 등은 평점 없음으로 처리한다. */
    private static final Map<String, Double> GRADE_POINTS = Map.ofEntries(
            Map.entry("A+", 4.5),
            Map.entry("A0", 4.0),
            Map.entry("A", 4.0),
            Map.entry("B+", 3.5),
            Map.entry("B0", 3.0),
            Map.entry("B", 3.0),
            Map.entry("C+", 2.5),
            Map.entry("C0", 2.0),
            Map.entry("C", 2.0),
            Map.entry("D+", 1.5),
            Map.entry("D0", 1.0),
            Map.entry("D", 1.0),
            Map.entry("F", 0.0)
    );

    public GpaStats calculate(List<KlasSemesterGradeResponse> semesters) {
        double weightedPointSum = 0.0;
        int totalCredits = 0;
        int courseCount = 0;
        // {평점, 학점} 쌍을 모아 두었다가 편차를 계산한다.
        List<double[]> gradedCourses = new ArrayList<>();

        for (KlasSemesterGradeResponse semester : safeSemesters(semesters)) {
            for (KlasSubjectGradeResponse subject : safeSubjects(semester)) {
                if (!isCompleted(subject)) {
                    continue;
                }
                Double point = gradePoint(subject.getGrade());
                if (point == null) {
                    continue;
                }
                int credit = subject.hakjumNum() == null ? 0 : subject.hakjumNum();
                if (credit <= 0) {
                    continue;
                }

                weightedPointSum += point * credit;
                totalCredits += credit;
                courseCount++;
                gradedCourses.add(new double[]{point, credit});
            }
        }

        if (totalCredits == 0) {
            return new GpaStats(0.0, 0.0, 0, 0);
        }

        double gpa = weightedPointSum / totalCredits;
        double stdDeviation = creditWeightedStd(gradedCourses, gpa, totalCredits);
        return new GpaStats(round2(gpa), round2(stdDeviation), totalCredits, courseCount);
    }

    /** 학점 가중 표준편차: sqrt( Σ credit_i * (point_i - gpa)^2 / Σ credit_i ). */
    private double creditWeightedStd(List<double[]> gradedCourses, double gpa, int totalCredits) {
        if (gradedCourses.size() <= 1) {
            return 0.0;
        }
        double weightedSquaredSum = 0.0;
        for (double[] course : gradedCourses) {
            double point = course[0];
            double credit = course[1];
            double diff = point - gpa;
            weightedSquaredSum += credit * diff * diff;
        }
        return Math.sqrt(weightedSquaredSum / totalCredits);
    }

    private Double gradePoint(String grade) {
        if (grade == null) {
            return null;
        }
        return GRADE_POINTS.get(grade.trim().toUpperCase());
    }

    private boolean isCompleted(KlasSubjectGradeResponse subject) {
        return "Y".equals(subject.finishOpt())
                && "Y".equals(subject.termFinish())
                && !"3".equals(subject.sungjukOpt())
                && (subject.getGrade() == null || !subject.getGrade().contains("삭제"));
    }

    private List<KlasSemesterGradeResponse> safeSemesters(List<KlasSemesterGradeResponse> semesters) {
        return semesters == null ? List.of() : semesters;
    }

    private List<KlasSubjectGradeResponse> safeSubjects(KlasSemesterGradeResponse semester) {
        if (semester == null || semester.sungjukList() == null) {
            return List.of();
        }
        return semester.sungjukList();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
