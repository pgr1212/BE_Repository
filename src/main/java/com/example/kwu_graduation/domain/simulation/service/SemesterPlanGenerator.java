package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.simulation.dto.RecommendedCourseResponse;
import com.example.kwu_graduation.domain.simulation.dto.RequirementGapResponse;
import com.example.kwu_graduation.domain.simulation.dto.SimulationStepResponse;
import com.example.kwu_graduation.domain.simulation.model.CourseCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SemesterPlanGenerator {

    private static final int DEFAULT_COURSE_CREDIT = 3;
    private static final int MAX_SIMULATION_SEMESTERS = 12;

    public List<SimulationStepResponse> generate(
            int currentGradeYear,
            int currentSemester,
            int maxCreditPerSemester,
            List<RequirementGapResponse> gaps
    ) {
        CreditNeeds needs = CreditNeeds.from(gaps == null ? List.of() : gaps);
        if (needs.totalRemainingCredit() == 0) {
            return List.of(new SimulationStepResponse(
                    currentGradeYear,
                    currentSemester,
                    List.of(),
                    0,
                    List.of(),
                    true,
                    "%d학년 %d학기: 현재 모든 자동 검증 조건 충족 -> 졸업 가능".formatted(currentGradeYear, currentSemester)
            ));
        }

        List<SimulationStepResponse> steps = new ArrayList<>();
        int gradeYear = currentGradeYear;
        int semester = currentSemester;

        for (int index = 0; index < MAX_SIMULATION_SEMESTERS && needs.totalRemainingCredit() > 0; index++) {
            int[] next = nextSemester(gradeYear, semester);
            gradeYear = next[0];
            semester = next[1];

            List<RecommendedCourseResponse> recommendedCourses = new ArrayList<>();
            int addedCredit = fillSemester(recommendedCourses, maxCreditPerSemester, needs);
            List<RequirementGapResponse> remainingGaps = needs.toGaps();
            boolean graduationAvailable = remainingGaps.isEmpty();

            steps.add(new SimulationStepResponse(
                    gradeYear,
                    semester,
                    recommendedCourses,
                    addedCredit,
                    remainingGaps,
                    graduationAvailable,
                    buildMessage(gradeYear, semester, recommendedCourses.size(), addedCredit, graduationAvailable)
            ));
        }

        return steps;
    }

    private int fillSemester(
            List<RecommendedCourseResponse> recommendedCourses,
            int maxCreditPerSemester,
            CreditNeeds needs
    ) {
        int addedCredit = 0;
        addedCredit += addCourses(recommendedCourses, CourseCategory.MAJOR, needs, maxCreditPerSemester - addedCredit);
        addedCredit += addCourses(recommendedCourses, CourseCategory.CULTURE, needs, maxCreditPerSemester - addedCredit);
        addedCredit += addCourses(recommendedCourses, CourseCategory.ETC, needs, maxCreditPerSemester - addedCredit);
        addedCredit += addCourses(recommendedCourses, CourseCategory.ANY, needs, maxCreditPerSemester - addedCredit);
        return addedCredit;
    }

    private int addCourses(
            List<RecommendedCourseResponse> recommendedCourses,
            CourseCategory category,
            CreditNeeds needs,
            int availableCredit
    ) {
        int addedCredit = 0;
        while (availableCredit > 0 && needs.remaining(category) > 0) {
            int credit = Math.min(DEFAULT_COURSE_CREDIT, Math.min(availableCredit, needs.remaining(category)));
            recommendedCourses.add(new RecommendedCourseResponse(
                    null,
                    category.recommendationName(),
                    category == CourseCategory.ANY ? CourseCategory.ETC : category,
                    credit
            ));
            needs.consume(category, credit);
            availableCredit -= credit;
            addedCredit += credit;
        }
        return addedCredit;
    }

    private int[] nextSemester(int gradeYear, int semester) {
        if (semester == 1) {
            return new int[]{gradeYear, 2};
        }
        return new int[]{gradeYear + 1, 1};
    }

    private String buildMessage(
            int gradeYear,
            int semester,
            int courseCount,
            int addedCredit,
            boolean graduationAvailable
    ) {
        if (graduationAvailable) {
            return "%d학년 %d학기: %d과목, %d학점 추가 이수 시 모든 자동 검증 조건 충족 -> 졸업 가능"
                    .formatted(gradeYear, semester, courseCount, addedCredit);
        }
        return "%d학년 %d학기: %d과목, %d학점 추가 이수 후에도 추가 조건 필요"
                .formatted(gradeYear, semester, courseCount, addedCredit);
    }

    private static final class CreditNeeds {
        private final Map<CourseCategory, Integer> remainingCredits = new EnumMap<>(CourseCategory.class);

        private CreditNeeds(int major, int culture, int etc, int any) {
            remainingCredits.put(CourseCategory.MAJOR, major);
            remainingCredits.put(CourseCategory.CULTURE, culture);
            remainingCredits.put(CourseCategory.ETC, etc);
            remainingCredits.put(CourseCategory.ANY, any);
        }

        static CreditNeeds from(List<RequirementGapResponse> gaps) {
            int total = remaining(gaps, "TOTAL_CREDIT");
            int major = remaining(gaps, "MAJOR_CREDIT");
            int culture = remaining(gaps, "CULTURE_CREDIT");
            int etc = remaining(gaps, "ETC_CREDIT");
            int requiredCourseCredit = gaps.stream()
                    .filter(gap -> "REQUIRED_COURSE".equals(gap.type()))
                    .mapToInt(RequirementGapResponse::remaining)
                    .sum();
            int requiredAreaCredit = gaps.stream()
                    .filter(gap -> "REQUIRED_AREA".equals(gap.type()))
                    .mapToInt(RequirementGapResponse::remaining)
                    .sum();

            int categorySpecific = major + culture + etc;
            int any = Math.max(total - categorySpecific, 0) + requiredCourseCredit + requiredAreaCredit;
            return new CreditNeeds(major, culture, etc, any);
        }

        int remaining(CourseCategory category) {
            return remainingCredits.getOrDefault(category, 0);
        }

        void consume(CourseCategory category, int credit) {
            int remaining = Math.max(remaining(category) - credit, 0);
            remainingCredits.put(category, remaining);
        }

        int totalRemainingCredit() {
            return remainingCredits.values().stream().mapToInt(Integer::intValue).sum();
        }

        List<RequirementGapResponse> toGaps() {
            List<RequirementGapResponse> gaps = new ArrayList<>();
            addGap(gaps, "MAJOR_CREDIT", "전공 학점", CourseCategory.MAJOR);
            addGap(gaps, "CULTURE_CREDIT", "교양 학점", CourseCategory.CULTURE);
            addGap(gaps, "ETC_CREDIT", "기타 학점", CourseCategory.ETC);
            addGap(gaps, "TOTAL_CREDIT", "총 이수 학점", CourseCategory.ANY);
            return gaps;
        }

        private void addGap(List<RequirementGapResponse> gaps, String type, String name, CourseCategory category) {
            int remaining = remaining(category);
            if (remaining > 0) {
                gaps.add(new RequirementGapResponse(type, name, remaining, 0, remaining, false));
            }
        }

        private static int remaining(List<RequirementGapResponse> gaps, String type) {
            return gaps.stream()
                    .filter(gap -> type.equals(gap.type()))
                    .mapToInt(RequirementGapResponse::remaining)
                    .sum();
        }
    }
}
