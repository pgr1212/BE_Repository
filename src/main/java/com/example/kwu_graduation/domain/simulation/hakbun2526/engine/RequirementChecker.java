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

        // 3. 교양 총 이수학점(필수교양 + 균형교양)
        items.add(CheckItem.ofCredit("교양", "교양 총 이수학점",
                spec.liberalArtsCredit(), credits.cultureChidukHakjum()));

        // 4. 교양 과목을 균형교양/기타로 자동 분류(과목명 → 영역 매핑)
        BalanceResult balance = classifyBalance(spec, subjects);

        // 4-1. 균형교양 이수학점
        items.add(CheckItem.ofCredit("교양", "균형교양 이수학점",
                spec.balanceCredit(), balance.balanceCredit()));

        // 4-2. 균형교양 영역 충족(이수한 과목으로부터 영역 수를 자동 판정)
        items.add(balanceAreaItem(spec, balance));

        // 4-3. 기타 교양(균형교양에 포함되지 않는 교양) 이수학점 — 참고용
        items.add(etcCultureItem(balance));

        // 5. 필수교양 과목 이수 여부(과목명으로 자동 감지)
        for (String course : spec.requiredLiberalCourses()) {
            items.add(requiredCourseItem(course, subjects));
        }

        // 6. 다전공 택1
        if (spec.multiMajorRequired()) {
            items.add(multiMajorItem(input));
        }

        // 7. 졸업논문/졸업작품 택1(과목명 자동 감지 + 자기보고 보완)
        items.add(graduationProjectItem(spec, subjects, input));

        // 8. 공학인증 학과(컴정공·소프트): 공학/일반 프로그램 선택 + 공학프로그램일 때 MSC·공필 자동 판정
        if (spec.engineering() != null) {
            addEngineeringItems(items, spec.engineering(), subjects, input);
        }

        // 9. 세부전공(트랙)별 세부전공필수 — 트랙 선택(자기보고) + 지정 과목 이수 수 자동 판정
        if (!spec.subMajors().isEmpty()) {
            items.add(subMajorItem(spec, subjects, input));
        }

        // 10. TOPCIT 응시(소프트) — 성적으로 알 수 없어 자기보고 값으로 판정
        if (spec.topcitRequired()) {
            items.add(topcitItem(input));
        }

        // 11. 학과별 추가 요건(설계학점 등) — 성적만으로 자동 판정 불가, 사용자 확인 안내
        for (String requirement : spec.additionalRequirements()) {
            items.add(CheckItem.ofStatus("학과추가요건", requirement,
                    CheckStatus.NEEDS_INPUT, "이수 여부를 직접 확인해 주세요."));
        }

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
     * 이수한 교양 과목을 균형교양/기타로 분류한다.
     * <p>균형교양 인정 조건: 교양(교필/교선) 과목 중 취득(완료)했고, 3·4학점이며,
     * 해당 학번·학과의 균형교양 영역 카탈로그에 존재하는 과목.
     * 그 외 교양 과목(실기·1·2학점·서울권역 e-러닝·K-MOOC·매치업·외국어로서의 한국어 등)은 기타로 집계한다.
     */
    private BalanceResult classifyBalance(GraduationRequirement spec, List<KlasSubjectGradeResponse> subjects) {
        Set<String> specAreas = spec.balanceAreas().stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        int balanceCredit = 0;
        int etcCultureCredit = 0;
        Set<String> completedAreas = new LinkedHashSet<>();

        for (KlasSubjectGradeResponse subject : subjects) {
            if (!isCompletedCulture(subject)) {
                continue;
            }
            int credit = subject.hakjumNum() == null ? 0 : subject.hakjumNum();

            Optional<String> area = (credit == 3 || credit == 4)
                    ? balanceAreaCatalog.areaOf(subject.gwamokKname())
                    : Optional.empty();

            if (area.isPresent() && specAreas.contains(normalize(area.get()))) {
                balanceCredit += credit;
                completedAreas.add(area.get());
            } else {
                etcCultureCredit += credit;
            }
        }
        return new BalanceResult(balanceCredit, etcCultureCredit, completedAreas);
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

    private CheckItem etcCultureItem(BalanceResult balance) {
        return new CheckItem("교양", "기타 교양 이수학점", 0, balance.etcCultureCredit(), 0,
                CheckStatus.SATISFIED,
                "균형교양 외 교양(필수교양·실기·1·2학점·비인정 강좌 등) %d학점".formatted(balance.etcCultureCredit()));
    }

    /** 교양(교필/교선) 과목 중 취득(완료)한 과목인지 확인한다. */
    private boolean isCompletedCulture(KlasSubjectGradeResponse subject) {
        return subject.codeName1() != null
                && subject.codeName1().startsWith("교")
                && "Y".equals(subject.finishOpt())
                && "Y".equals(subject.termFinish())
                && notDeleted(subject);
    }

    /** 균형교양/기타 분류 집계 결과. */
    private record BalanceResult(int balanceCredit, int etcCultureCredit, Set<String> completedAreas) {
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

    /**
     * 공학인증 학과(컴정공·소프트)의 공학/일반 프로그램 선택과 공학프로그램 요건을 판정해 항목으로 추가한다.
     * <p>입력 {@code engineeringProgram} 자기보고 값에 따라 분기한다.
     * <ul>
     *   <li>null  → 프로그램 미선택(NEEDS_INPUT)</li>
     *   <li>false → 일반프로그램(MSC·공필 요건 면제)</li>
     *   <li>true  → 공학프로그램: MSC 필수과목·MSC 총학점·공필 과목을 성적으로 자동 판정</li>
     * </ul>
     */
    private void addEngineeringItems(
            List<CheckItem> items,
            EngineeringProgram eng,
            List<KlasSubjectGradeResponse> subjects,
            RequirementCheckRequest input
    ) {
        Boolean isEngineering = input.engineeringProgram();
        if (isEngineering == null) {
            items.add(CheckItem.ofStatus("공학인증", "졸업 프로그램 선택", CheckStatus.NEEDS_INPUT,
                    "공학프로그램/일반프로그램 중 어느 과정인지 확인해 주세요. (공학프로그램만 MSC·공필 요건 적용)"));
            return;
        }
        if (!isEngineering) {
            items.add(CheckItem.ofStatus("공학인증", "졸업 프로그램", CheckStatus.SATISFIED,
                    "일반프로그램 (MSC·설계·공필 요건 면제)"));
            return;
        }

        items.add(CheckItem.ofStatus("공학인증", "졸업 프로그램", CheckStatus.SATISFIED, "공학프로그램"));

        // MSC 필수과목(모두 이수)
        List<String> missing = eng.mscRequiredCourses().stream()
                .filter(course -> !hasPassedCourse(subjects, course))
                .toList();
        items.add(CheckItem.ofStatus("공학인증", "MSC 필수과목 이수",
                missing.isEmpty() ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT,
                missing.isEmpty() ? "필수과목 전체 이수" : "미이수: " + String.join(", ", missing)));

        // MSC 영역별 최소학점(소프트: 수학 6 / 기초과학 3 / 공학기초 6). 영역 구분이 없으면 생략.
        for (MscArea area : eng.mscAreas()) {
            int areaEarned = sumPassedCredit(subjects, area.courses());
            items.add(CheckItem.ofCredit("공학인증", "MSC " + area.name() + " 영역", area.minCredit(), areaEarned));
        }

        // MSC 총 이수학점(인정 과목 풀에서 취득학점 합산)
        int mscEarned = sumPassedCredit(subjects, eng.mscPoolCourses());
        items.add(CheckItem.ofCredit("공학인증", "MSC 총 이수학점", eng.mscCredit(), mscEarned));

        // 공학필수(공필) 과목
        if (!eng.requiredCourses().isEmpty() || !eng.requiredCoursesOneOf().isEmpty()) {
            items.add(engineeringRequiredCourseItem(eng, subjects));
        }
    }

    private CheckItem engineeringRequiredCourseItem(EngineeringProgram eng, List<KlasSubjectGradeResponse> subjects) {
        List<String> missingMandatory = eng.requiredCourses().stream()
                .filter(course -> !hasPassedCourse(subjects, course))
                .toList();
        boolean oneOfOk = eng.requiredCoursesOneOf().isEmpty()
                || eng.requiredCoursesOneOf().stream().anyMatch(course -> hasPassedCourse(subjects, course));

        boolean satisfied = missingMandatory.isEmpty() && oneOfOk;
        String oneOfNote = eng.requiredCoursesOneOf().isEmpty()
                ? ""
                : " / %s 중 1개".formatted(String.join(" 또는 ", eng.requiredCoursesOneOf()));
        String label = "공학필수(공필) 과목 이수";
        if (satisfied) {
            return CheckItem.ofStatus("공학인증", label, CheckStatus.SATISFIED, "공필 과목 이수 완료");
        }
        StringBuilder msg = new StringBuilder();
        if (!missingMandatory.isEmpty()) {
            msg.append("미이수: ").append(String.join(", ", missingMandatory));
        }
        if (!oneOfOk) {
            if (msg.length() > 0) {
                msg.append(" / ");
            }
            msg.append(String.join(" 또는 ", eng.requiredCoursesOneOf())).append(" 중 1개 미이수");
        }
        return new CheckItem("공학인증", label + oneOfNote, 0, 0, 0, CheckStatus.INSUFFICIENT, msg.toString());
    }

    /**
     * 세부전공(트랙) 선택 + 세부전공필수 과목 이수 수를 판정한다.
     * 트랙 미선택 시 NEEDS_INPUT, 선택한 트랙에 세부필수가 없으면 충족,
     * 있으면 지정 과목 중 이수 과목 수가 기준 이상인지 판정한다.
     */
    private CheckItem subMajorItem(
            GraduationRequirement spec,
            List<KlasSubjectGradeResponse> subjects,
            RequirementCheckRequest input
    ) {
        List<String> names = spec.subMajors().stream().map(SubMajorRequirement::name).toList();
        String selected = input.subMajor();
        if (selected == null || selected.isBlank()) {
            return CheckItem.ofStatus("세부전공", "세부전공필수", CheckStatus.NEEDS_INPUT,
                    "세부전공을 선택해 주세요. (%s)".formatted(String.join(" / ", names)));
        }

        SubMajorRequirement track = spec.subMajors().stream()
                .filter(s -> normalize(s.name()).equals(normalize(selected)))
                .findFirst()
                .orElse(null);
        if (track == null) {
            return CheckItem.ofStatus("세부전공", "세부전공필수", CheckStatus.NEEDS_INPUT,
                    "알 수 없는 세부전공: %s (가능: %s)".formatted(selected, String.join(" / ", names)));
        }

        if (track.requiredCount() <= 0) {
            return CheckItem.ofStatus("세부전공", "세부전공필수 - " + track.name(), CheckStatus.SATISFIED,
                    "세부전공필수 과목 없음(해당사항 없음)");
        }

        int taken = countPassedCourses(subjects, track.requiredCourses());
        int lack = Math.max(0, track.requiredCount() - taken);
        CheckStatus status = lack == 0 ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT;
        String message = lack == 0
                ? "%d과목 이수(충족)".formatted(taken)
                : "%d과목 부족 (%s 중 %d과목 이상)".formatted(lack, String.join(", ", track.requiredCourses()), track.requiredCount());
        return new CheckItem("세부전공", "세부전공필수 - " + track.name(),
                track.requiredCount(), taken, lack, status, message);
    }

    /** TOPCIT 응시 여부를 자기보고 값으로 판정한다. */
    private CheckItem topcitItem(RequirementCheckRequest input) {
        Boolean done = input.topcitCompleted();
        if (done == null) {
            return CheckItem.ofStatus("TOPCIT", "TOPCIT 응시", CheckStatus.NEEDS_INPUT,
                    "TOPCIT 응시 여부를 확인해 주세요. (성적 무관, 응시해야 졸업 가능)");
        }
        return CheckItem.ofStatus("TOPCIT", "TOPCIT 응시",
                done ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT,
                done ? "응시 완료" : "미응시(졸업요건상 필수 응시)");
    }

    /** 지정 과목 목록 중 (삭제되지 않고 F가 아닌) 이수한 서로 다른 과목 수를 센다. */
    private int countPassedCourses(List<KlasSubjectGradeResponse> subjects, List<String> courseNames) {
        return (int) courseNames.stream()
                .filter(course -> hasPassedCourse(subjects, course))
                .count();
    }

    /** (삭제되지 않고 F가 아닌) 해당 과목을 이수했는지 공백·대소문자 무시하고 정확히 매칭한다. */
    private boolean hasPassedCourse(List<KlasSubjectGradeResponse> subjects, String courseName) {
        String target = normalize(courseName);
        return subjects.stream()
                .filter(this::isPassedSubject)
                .anyMatch(s -> s.gwamokKname() != null && normalize(s.gwamokKname()).equals(target));
    }

    /** 과목 풀(pool)에 속하면서 이수(통과)한 과목들의 취득학점 합. 동일 과목 중복은 과목명 기준 1회만 합산한다. */
    private int sumPassedCredit(List<KlasSubjectGradeResponse> subjects, List<String> poolCourses) {
        Set<String> pool = poolCourses.stream().map(this::normalize).collect(Collectors.toSet());
        Set<String> counted = new java.util.HashSet<>();
        int sum = 0;
        for (KlasSubjectGradeResponse s : subjects) {
            if (s.gwamokKname() == null || !isPassedSubject(s)) {
                continue;
            }
            String key = normalize(s.gwamokKname());
            if (pool.contains(key) && counted.add(key)) {
                sum += s.hakjumNum() == null ? 0 : s.hakjumNum();
            }
        }
        return sum;
    }

    /** 전공/MSC 과목의 이수(통과) 여부: 삭제되지 않았고 성적이 있으며 F가 아님. */
    private boolean isPassedSubject(KlasSubjectGradeResponse subject) {
        if (!notDeleted(subject)) {
            return false;
        }
        String grade = subject.getGrade();
        return grade != null && !grade.isBlank() && !"F".equalsIgnoreCase(grade.trim());
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
