package com.example.kwu_graduation.domain.requirements.hakbun23.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementItem;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RequirementService {

    private static final int CULTURE_REQUIRED = 22;
    private static final int MAJOR_SINGLE_REQUIRED = 60;
    private static final int MAJOR_DOUBLE_REQUIRED = 54;
    private static final int TOTAL_REQUIRED = 133;

    // ===== 공학프로그램 공통 (컴퓨터정보공학부, 소프트웨어학부 둘 다 사용) =====

    private static final Set<String> MSC_REQUIRED_SUBJECTS = Set.of(
            "대학수학및연습1", "대학수학및연습2", "공학수학1",
            "대학물리학1", "대학물리학2",
            "C프로그래밍", "인공지능과컴퓨팅사고"
    );
    private static final Set<String> MSC_POOL_SUBJECTS = Set.of(
            "대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
            "선형대수학", "이산수학", "벡터해석학및연습", "확률및통계", "수치해석", "확률및불규칙신호론",
            "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2", "대학화학",
            "C프로그래밍", "인공지능과컴퓨팅사고"
    );
    private static final int MSC_TOTAL_REQUIRED = 30;

    private static final Map<String, Integer> DESIGN_CREDIT_MAP = Map.of(
            "공학설계입문", 3,
            "산학협력캡스톤설계1", 3,
            "산학협력캡스톤설계2", 3
    );
    private static final int DESIGN_REQUIRED = 12;

    // ===== 컴퓨터정보공학부 전용 =====

    // 전공 최소설계 필수: 공학설계입문 + 이산수학 (필수) + 캡스톤1 또는 2 (택1)
    private static final Set<String> CE_DESIGN_MANDATORY = Set.of("공학설계입문", "이산수학");
    private static final Set<String> CE_DESIGN_ONE_OF = Set.of("산학협력캡스톤설계1", "산학협력캡스톤설계2");

    // ===== 정보융합학부 전용 =====

    private static final Set<String> CAPSTONE_SUBJECTS = Set.of(
            "졸업작품", "캡스톤디자인", "산학협력캡스톤설계1", "산학협력캡스톤설계2"
    );

    // ===== 소프트웨어학부 전용 =====

    // 전공필수 4과목 (2024학번까지 적용)
    private static final Set<String> SOFTWARE_REQUIRED_SUBJECTS = Set.of(
            "자료구조", "자료구조실습", "알고리즘", "응용소프트웨어실습"
    );
    // 인공지능전공 세부전공필수 풀 (5개 중 3개 이상)
    private static final Set<String> AI_TRACK_SUBJECTS = Set.of(
            "인공지능", "빅데이터처리및응용", "컴퓨터비전", "기계학습", "딥러닝실습"
    );
    private static final int AI_TRACK_REQUIRED_COUNT = 3;
    // 전공 최소설계 필수: 공학설계입문만
    private static final Set<String> SW_DESIGN_MANDATORY = Set.of("공학설계입문");

    public RequirementCheckResponse check(String department, RequirementCheckRequest req) {
        return switch (department) {
            case "정보융합학부" -> checkInfoConvergence(req);
            case "컴퓨터정보공학부" -> checkComputerEngineering(req);
            case "소프트웨어학부" -> checkSoftware(req);
            default -> throw new IllegalArgumentException("지원하지 않는 학과입니다: " + department);
        };
    }

    private RequirementCheckResponse checkInfoConvergence(RequirementCheckRequest req) {
        CreditSummaryResponse c = req.creditSummary();
        boolean capstoneDone = hasTakenAny(req.subjects(), CAPSTONE_SUBJECTS);
        int majorRequired = req.isDoubleMajor() ? MAJOR_DOUBLE_REQUIRED : MAJOR_SINGLE_REQUIRED;

        List<RequirementItem> items = new ArrayList<>(List.of(
                RequirementItem.of("총 취득학점", c.chidukHakjum(), TOTAL_REQUIRED),
                RequirementItem.of("전공 취득학점", c.majorChidukHakjum(), majorRequired),
                RequirementItem.of("교양 취득학점", c.cultureChidukHakjum(), CULTURE_REQUIRED)
        ));
        items.add(new RequirementItem("캡스톤 이수", capstoneDone ? 1 : 0, 1, capstoneDone));

        return RequirementCheckResponse.of(items);
    }

    private RequirementCheckResponse checkComputerEngineering(RequirementCheckRequest req) {
        CreditSummaryResponse c = req.creditSummary();
        int majorRequired = req.isDoubleMajor() ? MAJOR_DOUBLE_REQUIRED : MAJOR_SINGLE_REQUIRED;

        List<RequirementItem> items = new ArrayList<>(List.of(
                RequirementItem.of("전공 취득학점(설계 포함)", c.majorChidukHakjum(), majorRequired),
                RequirementItem.of("교양 취득학점", c.cultureChidukHakjum(), CULTURE_REQUIRED)
        ));

        if (req.isEngineeringProgram()) {
            boolean mscRequiredTaken = MSC_REQUIRED_SUBJECTS.stream()
                    .allMatch(name -> hasTakenAny(req.subjects(), Set.of(name)));
            int mscTotalCredit = sumCredit(req.subjects(), MSC_POOL_SUBJECTS);
            int designCredit = sumDesignCredit(req.subjects());
            boolean designMinimumOk = hasMinimumDesignRequirement(req.subjects(), CE_DESIGN_MANDATORY, CE_DESIGN_ONE_OF);

            items.add(new RequirementItem("MSC 필수과목 7개 이수", mscRequiredTaken ? 1 : 0, 1, mscRequiredTaken));
            items.add(RequirementItem.of("MSC 총 이수학점(30학점)", mscTotalCredit, MSC_TOTAL_REQUIRED));
            items.add(RequirementItem.of("설계 학점", designCredit, DESIGN_REQUIRED));
            items.add(new RequirementItem("전공 최소설계 필수과목(공학설계입문·이산수학·캡스톤1or2)", designMinimumOk ? 1 : 0, 1, designMinimumOk));
        }
        // 일반 프로그램: MSC·설계 요건 면제

        return RequirementCheckResponse.of(items);
    }

    private RequirementCheckResponse checkSoftware(RequirementCheckRequest req) {
        CreditSummaryResponse c = req.creditSummary();
        int majorRequired = req.isDoubleMajor() ? MAJOR_DOUBLE_REQUIRED : MAJOR_SINGLE_REQUIRED;

        List<RequirementItem> items = new ArrayList<>(List.of(
                RequirementItem.of("전공 취득학점", c.majorChidukHakjum(), majorRequired),
                RequirementItem.of("교양 취득학점", c.cultureChidukHakjum(), CULTURE_REQUIRED)
        ));

        // 1. 전공필수 4과목
        boolean majorRequiredTaken = SOFTWARE_REQUIRED_SUBJECTS.stream()
                .allMatch(name -> hasTakenAny(req.subjects(), Set.of(name)));
        items.add(new RequirementItem("전공필수 4과목 이수", majorRequiredTaken ? 1 : 0, 1, majorRequiredTaken));

        // 2. 세부전공필수 (세부전공에 따라 다름)
        if ("인공지능전공".equals(req.subMajor())) {
            int aiTakenCount = countTaken(req.subjects(), AI_TRACK_SUBJECTS);
            items.add(RequirementItem.of("인공지능전공 세부필수(3과목 이상)", aiTakenCount, AI_TRACK_REQUIRED_COUNT));
        }
        // 소프트웨어전공은 해당사항 없음 -> 항목 추가 안 함

        // 3. 졸업논문, 4. TOPCIT (KLAS 성적 데이터로 판별 불가 -> 외부 입력값 그대로 반영)
        boolean topcit = Boolean.TRUE.equals(req.topcitPassed());
        boolean thesis = Boolean.TRUE.equals(req.thesisPassed());
        items.add(new RequirementItem("TOPCIT 응시", topcit ? 1 : 0, 1, topcit));
        items.add(new RequirementItem("졸업논문 '가' 판정", thesis ? 1 : 0, 1, thesis));

        // 5. 프로그램별 졸업이수요건 = 공학프로그램 요건(MSC 30학점 + 설계 12학점 + 공학설계입문)
        if (req.isEngineeringProgram()) {
            int mscTotalCredit = sumCredit(req.subjects(), MSC_POOL_SUBJECTS);
            int designCredit = sumDesignCredit(req.subjects());
            boolean designMinimumOk = hasMinimumDesignRequirement(req.subjects(), SW_DESIGN_MANDATORY, Set.of());

            items.add(RequirementItem.of("MSC 총 이수학점(30학점)", mscTotalCredit, MSC_TOTAL_REQUIRED));
            items.add(RequirementItem.of("설계 학점", designCredit, DESIGN_REQUIRED));
            items.add(new RequirementItem("전공 최소설계 필수과목(공학설계입문)", designMinimumOk ? 1 : 0, 1, designMinimumOk));
        }

        return RequirementCheckResponse.of(items);
    }

    private boolean hasTakenAny(List<KlasSubjectGradeResponse> subjects, Set<String> names) {
        if (subjects == null) return false;
        return subjects.stream().anyMatch(s -> names.contains(s.gwamokKname()) && isPassed(s));
    }

    private int countTaken(List<KlasSubjectGradeResponse> subjects, Set<String> names) {
        if (subjects == null) return 0;
        return (int) subjects.stream()
                .filter(s -> names.contains(s.gwamokKname()) && isPassed(s))
                .map(KlasSubjectGradeResponse::gwamokKname)
                .distinct()
                .count();
    }

    private int sumCredit(List<KlasSubjectGradeResponse> subjects, Set<String> names) {
        if (subjects == null) return 0;
        return subjects.stream()
                .filter(s -> names.contains(s.gwamokKname()) && isPassed(s))
                .mapToInt(KlasSubjectGradeResponse::hakjumNum)
                .sum();
    }

    private int sumDesignCredit(List<KlasSubjectGradeResponse> subjects) {
        if (subjects == null) return 0;
        return subjects.stream()
                .filter(this::isPassed)
                .mapToInt(s -> DESIGN_CREDIT_MAP.getOrDefault(s.gwamokKname(), 0))
                .sum();
    }

    private boolean hasMinimumDesignRequirement(List<KlasSubjectGradeResponse> subjects,
                                                Set<String> mandatorySubjects,
                                                Set<String> oneOfSubjects) {
        boolean mandatoryOk = mandatorySubjects.stream()
                .allMatch(name -> hasTakenAny(subjects, Set.of(name)));
        boolean oneOfOk = oneOfSubjects.isEmpty() || hasTakenAny(subjects, oneOfSubjects);
        return mandatoryOk && oneOfOk;
    }

    private boolean isPassed(KlasSubjectGradeResponse s) {
        return s.getGrade() != null && !s.getGrade().equals("F");
    }
}