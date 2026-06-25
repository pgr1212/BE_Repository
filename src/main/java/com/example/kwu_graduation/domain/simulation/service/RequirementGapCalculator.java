package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckItem;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckStatus;
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

    private static final int DEFAULT_REQUIRED_COURSE_CREDIT = 3;

    private final GraduationCreditCalculator creditCalculator;

    public List<RequirementGapResponse> calculate(List<CheckItem> checkItems) {
        if (checkItems == null || checkItems.isEmpty()) {
            return List.of();
        }

        List<RequirementGapResponse> gaps = new ArrayList<>();
        for (CheckItem item : checkItems) {
            toGap(item).stream()
                    .filter(gap -> !gap.satisfied())
                    .forEach(gaps::add);
        }
        return gaps;
    }

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

    private java.util.Optional<RequirementGapResponse> toGap(CheckItem item) {
        if (item == null || item.status() != CheckStatus.INSUFFICIENT) {
            return java.util.Optional.empty();
        }

        String type = gapType(item);
        if (type == null) {
            return java.util.Optional.empty();
        }

        int remaining = remainingOf(item, type);
        if (remaining <= 0) {
            return java.util.Optional.empty();
        }

        int required = Math.max(item.required(), remaining);
        int completed = Math.max(required - remaining, 0);
        return java.util.Optional.of(new RequirementGapResponse(
                type,
                item.name(),
                required,
                completed,
                remaining,
                false
        ));
    }

    private String gapType(CheckItem item) {
        return switch (item.name()) {
            case "졸업 총 이수학점" -> "TOTAL_CREDIT";
            case "주전공(필수 포함)" -> "MAJOR_CREDIT";
            case "교양 총 이수학점" -> "CULTURE_CREDIT";
            case "균형교양 이수학점" -> "REQUIRED_AREA";
            default -> {
                if (item.name() != null && item.name().startsWith("필수교양 - ")) {
                    yield "REQUIRED_COURSE";
                }
                yield null;
            }
        };
    }

    private int remainingOf(CheckItem item, String type) {
        if ("REQUIRED_COURSE".equals(type) && item.lack() <= 0) {
            return DEFAULT_REQUIRED_COURSE_CREDIT;
        }
        return item.lack();
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
