package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSummaryResponse;
import com.example.kwu_graduation.domain.simulation.dto.RequirementGapResponse;
import com.example.kwu_graduation.domain.simulation.model.GraduationRequirement;
import com.example.kwu_graduation.domain.simulation.model.ManualCheck;
import com.example.kwu_graduation.domain.simulation.model.RequiredArea;
import com.example.kwu_graduation.domain.simulation.model.RequiredCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RequirementGapCalculator {

    private final GraduationCreditCalculator creditCalculator;

    public List<RequirementGapResponse> calculate(
            GraduationRequirement requirement,
            GraduationSummaryResponse summary,
            List<KlasSemesterGradeResponse> semesters
    ) {
        List<RequirementGapResponse> gaps = new ArrayList<>();

        addCreditGap(gaps, "TOTAL_CREDIT", "총 이수 학점", requirement.totalCredit(), summary.totalCredit());
        addCreditGap(gaps, "MAJOR_CREDIT", "전공 학점", requirement.majorCredit(), summary.majorCredit());
        addCreditGap(gaps, "CULTURE_CREDIT", "교양 학점", requirement.cultureCredit(), summary.cultureCredit());
        addCreditGap(gaps, "ETC_CREDIT", "기타 학점", requirement.etcCredit(), summary.etcCredit());

        for (RequiredCourse course : requirement.requiredCourses()) {
            boolean completed = creditCalculator.hasCompletedRequiredCourse(
                    semesters,
                    course.courseCode(),
                    course.courseName()
            );
            if (!completed) {
                gaps.add(new RequirementGapResponse(
                        "REQUIRED_COURSE",
                        course.courseName(),
                        course.credit(),
                        0,
                        course.credit(),
                        false
                ));
            }
        }

        for (RequiredArea area : requirement.requiredAreas()) {
            int completedCredit = creditCalculator.completedCreditByArea(semesters, area.areaCode());
            addCreditGap(gaps, "REQUIRED_AREA", area.areaName(), area.requiredCredit(), completedCredit);
        }

        for (ManualCheck manualCheck : requirement.manualChecks()) {
            gaps.add(new RequirementGapResponse(
                    "MANUAL_CHECK",
                    manualCheck.name(),
                    1,
                    0,
                    1,
                    false
            ));
        }

        return gaps;
    }

    private void addCreditGap(List<RequirementGapResponse> gaps, String type, String name, int required, int completed) {
        if (required <= 0) {
            return;
        }
        RequirementGapResponse gap = RequirementGapResponse.of(type, name, required, completed);
        if (!gap.satisfied()) {
            gaps.add(gap);
        }
    }
}
