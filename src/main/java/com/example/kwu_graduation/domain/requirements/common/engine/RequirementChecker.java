package com.example.kwu_graduation.domain.requirements.common.engine;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.requirements.common.dto.CheckItem;
import com.example.kwu_graduation.domain.requirements.common.dto.CheckStatus;
import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.common.spec.GraduationRequirement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 졸업요건 정의(spec) + 취득학점 요약 + 과목 목록 + 사용자 자기보고 값을 받아
 * 항목별 충족 여부를 판정하는 순수 로직.
 * 균형교양 영역 분류만 {@link BalanceAreaCatalog} 에 의존하며 그 외 외부 의존성이 없어
 * 단위 테스트가 용이하다.
 */
@Component
public class RequirementChecker {

    private final BalanceAreaCatalog balanceAreaCatalog;

    public RequirementChecker(BalanceAreaCatalog balanceAreaCatalog) {
        this.balanceAreaCatalog = balanceAreaCatalog;
    }

    public RequirementCheckResponse check(
            GraduationRequirement spec,
            CreditSummaryResponse credits,
            List<KlasSubjectGradeResponse> subjects,
            RequirementCheckRequest input
    ) {
        List<CheckItem> items = new ArrayList<>();

        // 1. 졸업 총 이수학점
        items.add(CheckItem.ofCredit("총이수학점", "졸업 총 이수학점",
                spec.totalCredit(), credits.chidukHakjum()));

        // 2. 주전공(필수 포함)
        items.add(CheckItem.ofCredit("전공", "주전공(필수 포함)",
                spec.majorCredit(), credits.majorChidukHakjum()));

        // 3. 교양 과목 중 균형교양만 따로 집계(과목명 → 영역 매핑).
        //    교양 총 이수학점은 기타교양까지 합산되어 졸업 판정을 왜곡하므로 별도 항목으로 두지 않는다.
        //    (기타교양은 표시하지 않고, 졸업 총 이수학점에만 합산된다.)
        BalanceResult balance = classifyBalance(spec, subjects);

        // 3-1. 균형교양 이수학점
        items.add(CheckItem.ofCredit("교양", "균형교양 이수학점",
                spec.balanceCredit(), balance.balanceCredit()));

        // 3-2. 균형교양 영역 충족(이수한 과목으로부터 영역 수를 자동 판정)
        items.add(balanceAreaItem(spec, balance));

        // 4. 필수교양 과목 이수 여부(과목명으로 자동 감지)
        for (String course : spec.requiredLiberalCourses()) {
            items.add(requiredCourseItem(course, subjects));
        }

        // 6. 다전공 택1
        if (spec.multiMajorRequired()) {
            items.add(multiMajorItem(input));
        }

        // 7. 졸업논문/졸업작품 택1(과목명 자동 감지 + 자기보고 보완)
        items.add(graduationProjectItem(spec, subjects, input));

        boolean graduatable = items.stream()
                .allMatch(item -> item.status() == CheckStatus.SATISFIED);

        return new RequirementCheckResponse(
                spec.admissionYear(),
                spec.department(),
                graduatable,
                spec.totalCredit(),
                credits.chidukHakjum(),
                items
        );
    }

    /**
     * 이수한 교양 과목 중 균형교양으로 인정되는 과목만 골라 학점·영역을 집계한다.
     * <p>균형교양 인정 조건: 교양(교필/교선) 과목 중 취득(완료)했고, 3·4학점이며,
     * 해당 학번·학과의 균형교양 영역 카탈로그에 존재하는 과목.
     * 그 외 교양 과목(필수교양·실기·1·2학점·서울권역 e-러닝·K-MOOC·매치업·외국어로서의 한국어 등)은
     * 균형교양으로 집계하지 않으며, 졸업 총 이수학점에만 합산된다(별도 표시하지 않음).
     */
    private BalanceResult classifyBalance(GraduationRequirement spec, List<KlasSubjectGradeResponse> subjects) {
        Set<String> specAreas = spec.balanceAreas().stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        int balanceCredit = 0;
        Set<String> completedAreas = new LinkedHashSet<>();

        for (KlasSubjectGradeResponse subject : subjects) {
            if (!isCompletedCulture(subject)) {
                continue;
            }
            int credit = subject.hakjumNum() == null ? 0 : subject.hakjumNum();
            if (credit != 3 && credit != 4) {
                continue;
            }

            Optional<String> area = balanceAreaCatalog.areaOf(subject.gwamokKname());
            if (area.isPresent() && specAreas.contains(normalize(area.get()))) {
                balanceCredit += credit;
                completedAreas.add(area.get());
            }
        }
        return new BalanceResult(balanceCredit, completedAreas);
    }

    private CheckItem balanceAreaItem(GraduationRequirement spec, BalanceResult balance) {
        List<String> mandatory = spec.balanceMandatoryAreasAnyOf();
        String mandatoryNote = mandatory.isEmpty()
                ? ""
                : " (단, %s 중 1개 영역 필수 포함)".formatted(String.join(" 또는 ", mandatory));

        int done = balance.completedAreas().size();
        int areaLack = Math.max(0, spec.balanceMinAreas() - done);

        boolean mandatoryOk = mandatory.isEmpty() || mandatory.stream()
                .map(this::normalize)
                .anyMatch(m -> balance.completedAreas().stream().map(this::normalize).anyMatch(m::equals));

        CheckStatus status = (areaLack == 0 && mandatoryOk) ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT;

        String areas = balance.completedAreas().isEmpty()
                ? "이수 영역 없음"
                : "이수 영역: " + String.join(", ", balance.completedAreas());
        String message;
        if (status == CheckStatus.SATISFIED) {
            message = "%d개 영역 이수(충족)%s. %s".formatted(done, mandatoryNote, areas);
        } else if (!mandatoryOk && areaLack == 0) {
            message = "%d개 영역 이수했으나 필수 포함 영역 미이수%s. %s".formatted(done, mandatoryNote, areas);
        } else {
            message = "%d개 영역 부족%s. %s".formatted(areaLack, mandatoryNote, areas);
        }
        return new CheckItem("교양", "균형교양 영역 충족", spec.balanceMinAreas(), done, areaLack, status, message);
    }

    /** 교양(교필/교선) 과목 중 취득(완료)한 과목인지 확인한다. */
    private boolean isCompletedCulture(KlasSubjectGradeResponse subject) {
        return subject.codeName1() != null
                && subject.codeName1().startsWith("교")
                && "Y".equals(subject.finishOpt())
                && "Y".equals(subject.termFinish())
                && notDeleted(subject);
    }

    /** 균형교양 집계 결과(인정 학점 + 이수 영역 집합). */
    private record BalanceResult(int balanceCredit, Set<String> completedAreas) {
    }

    private CheckItem requiredCourseItem(String course, List<KlasSubjectGradeResponse> subjects) {
        boolean taken = hasCourse(subjects, course);
        return CheckItem.ofStatus("필수교양", "필수교양 - " + course,
                taken ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT,
                taken ? "이수 완료" : "미이수");
    }

    private CheckItem multiMajorItem(RequirementCheckRequest input) {
        Boolean completed = input.multiMajorCompleted();
        if (completed == null) {
            return CheckItem.ofStatus("다전공", "다전공 택1", CheckStatus.NEEDS_INPUT,
                    "복수/부/심화/연계/마이크로/학생설계융합전공 중 1개 이수 여부를 확인해 주세요.");
        }
        return CheckItem.ofStatus("다전공", "다전공 택1",
                completed ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT,
                completed ? "다전공 이수(예정)" : "다전공 미이수(졸업요건상 택1 필수)");
    }

    private CheckItem graduationProjectItem(
            GraduationRequirement spec,
            List<KlasSubjectGradeResponse> subjects,
            RequirementCheckRequest input
    ) {
        String options = String.join(" 또는 ", spec.graduationProjectOptions());

        boolean detected = spec.graduationProjectOptions().stream().anyMatch(opt -> hasCourse(subjects, opt))
                || hasCourse(subjects, "캡스톤");
        if (detected) {
            return CheckItem.ofStatus("졸업작품", "졸업논문/졸업작품 택1", CheckStatus.SATISFIED,
                    "관련 교과목 이수 확인됨");
        }

        Boolean completed = input.graduationProjectCompleted();
        if (completed == null) {
            return CheckItem.ofStatus("졸업작품", "졸업논문/졸업작품 택1", CheckStatus.NEEDS_INPUT,
                    "%s 중 1개 완료 여부를 확인해 주세요.".formatted(options));
        }
        return CheckItem.ofStatus("졸업작품", "졸업논문/졸업작품 택1",
                completed ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT,
                completed ? "완료" : "미완료(%s 중 택1 필요)".formatted(options));
    }

    /** 과목 목록에서 (삭제되지 않은) 해당 과목명을 이수했는지 공백·대소문자 무시하고 확인한다. */
    private boolean hasCourse(List<KlasSubjectGradeResponse> subjects, String courseName) {
        String target = normalize(courseName);
        return subjects.stream()
                .filter(this::notDeleted)
                .anyMatch(s -> s.gwamokKname() != null && normalize(s.gwamokKname()).contains(target));
    }

    private boolean notDeleted(KlasSubjectGradeResponse subject) {
        if ("3".equals(subject.sungjukOpt())) {
            return false;
        }
        return subject.getGrade() == null || !subject.getGrade().contains("삭제");
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", "").toLowerCase();
    }
}
