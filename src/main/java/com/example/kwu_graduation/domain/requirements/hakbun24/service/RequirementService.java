package com.example.kwu_graduation.domain.requirements.hakbun24.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.grade.service.GradeCalculator;
import com.example.kwu_graduation.domain.grade.service.KlasGradeService;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

/**
 * 24학번(2024학년도 입학자) 졸업요건 판정.
 * 기존(creditSummary/subjects를 Body로 직접 받던 임시 구조)과 달리,
 * Klas-Cookie 헤더만 받아 KLAS에서 성적을 직접 조회·계산한다.
 * 학과별 판정 상수/로직은 이전과 동일(MSC 27학점, 스마트사회와인공지능 등 24학번 반영분 유지).
 */
@Service
@RequiredArgsConstructor
public class RequirementService {

    private final KlasGradeService klasGradeService;
    private final GradeCalculator gradeCalculator;
    // KLAS가 향후 필드를 추가해도 깨지지 않도록 미지의 필드는 무시한다.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final int CULTURE_REQUIRED = 31; // 필수교양1 + 균형교양30
    private static final int MAJOR_SINGLE_REQUIRED = 60;
    private static final int MAJOR_DOUBLE_REQUIRED = 54;
    private static final int TOTAL_REQUIRED = 133;

    // ===== 컴퓨터정보공학부 전용 MSC =====
    private static final Set<String> CE_MSC_REQUIRED_SUBJECTS = Set.of(
            "대학수학및연습1", "대학수학및연습2", "공학수학1",
            "대학물리학1", "대학물리학2",
            "고급C프로그래밍및설계", "스마트사회와인공지능"
    );
    private static final Set<String> CE_MSC_POOL_SUBJECTS = Set.of(
            "대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
            "선형대수학", "벡터해석학및연습", "확률및통계",
            "대학물리학1", "대학물리학2", "대학화학및실험1", "대학화학및실험2", "대학화학",
            "고급C프로그래밍및설계", "스마트사회와인공지능"
    );
    private static final int CE_MSC_TOTAL_REQUIRED = 27;

    // ===== 소프트웨어학부 전용 MSC =====
    private static final Set<String> SW_MSC_POOL_SUBJECTS = Set.of(
            "대학수학및연습1", "대학수학및연습2", "공학수학1", "공학수학2",
            "선형대수학", "벡터해석학및연습", "확률및통계", "확률및불규칙신호론",
            "대학물리학1", "대학물리학2", "대학물리및실험1", "대학물리및실험2",
            "대학화학및실험1", "대학화학및실험2", "대학화학",
            "C프로그래밍", "인공지능과컴퓨팅사고"
    );
    private static final int SW_MSC_TOTAL_REQUIRED = 12;

    // ===== 설계(컴공·SW 공통) =====
    private static final Map<String, Integer> DESIGN_CREDIT_MAP = Map.of(
            "공학설계입문", 3,
            "산학협력캡스톤설계1", 3,
            "산학협력캡스톤설계2", 3
    );
    private static final int DESIGN_REQUIRED = 12;
    private static final Set<String> CE_DESIGN_MANDATORY = Set.of("공학설계입문", "이산수학");
    private static final Set<String> CE_DESIGN_ONE_OF = Set.of("산학협력캡스톤설계1", "산학협력캡스톤설계2");

    // ===== 정보융합학부 전용 =====
    private static final Set<String> CAPSTONE_SUBJECTS = Set.of(
            "졸업작품", "캡스톤디자인", "산학협력캡스톤설계1", "산학협력캡스톤설계2"
    );
    private static final String JEONGYUNG_REQUIRED_BASIC_COURSE = "스마트사회와인공지능";

    // ===== 소프트웨어학부 전용 =====
    private static final Set<String> SOFTWARE_REQUIRED_SUBJECTS = Set.of(
            "자료구조", "자료구조실습", "알고리즘", "응용소프트웨어실습"
    );
    private static final Set<String> AI_TRACK_SUBJECTS = Set.of(
            "인공지능", "빅데이터처리및응용", "컴퓨터비전", "기계학습", "딥러닝실습"
    );
    private static final int AI_TRACK_REQUIRED_COUNT = 3;
    private static final Set<String> SW_DESIGN_MANDATORY = Set.of("공학설계입문");

    public RequirementCheckResponse check(String department, String klasCookie, RequirementCheckRequest req) {
        List<KlasSemesterGradeResponse> semesters = fetchSemesters(klasCookie);
        CreditSummaryResponse credits = gradeCalculator.calculate(semesters);
        List<KlasSubjectGradeResponse> subjects = semesters.stream()
                .flatMap(s -> s.sungjukList() == null ? Stream.empty() : s.sungjukList().stream())
                .toList();

        return switch (department) {
            case "정보융합학부" -> checkInfoConvergence(credits, subjects, req);
            case "컴퓨터정보공학부" -> checkComputerEngineering(credits, subjects, req);
            case "소프트웨어학부" -> checkSoftware(credits, subjects, req);
            default -> throw new IllegalArgumentException("지원하지 않는 학과입니다: " + department);
        };
    }

    /** KLAS 성적 응답(JSON 문자열)을 학기 목록으로 파싱한다. */
    private List<KlasSemesterGradeResponse> fetchSemesters(String klasCookie) {
        String rawGrades = klasGradeService.getSemesterGrades(klasCookie);
        try {
            JsonNode root = objectMapper.readTree(rawGrades);
            JsonNode arrayNode = findSemesterArray(root);
            if (arrayNode == null || !arrayNode.isArray()) {
                throw new IllegalStateException("성적 응답에서 학기 목록을 찾을 수 없습니다.");
            }
            return objectMapper.convertValue(arrayNode, new TypeReference<List<KlasSemesterGradeResponse>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("성적 응답(JSON) 파싱에 실패했습니다.", e);
        }
    }

    /** 응답이 학기 배열이거나, 배열을 감싼 객체 형태여도 대응할 수 있도록 배열 노드를 탐색한다. */
    private JsonNode findSemesterArray(JsonNode node) {
        if (node.isArray()) {
            return node;
        }
        if (node.isObject()) {
            for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
                JsonNode child = it.next();
                if (child.isArray() && (child.isEmpty() || child.get(0).has("sungjukList"))) {
                    return child;
                }
            }
            for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
                JsonNode found = findSemesterArray(it.next());
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private RequirementCheckResponse checkInfoConvergence(CreditSummaryResponse c, List<KlasSubjectGradeResponse> subjects,
                                                          RequirementCheckRequest req) {
        boolean capstoneDone = hasTakenAny(subjects, CAPSTONE_SUBJECTS);
        boolean basicCourseDone = hasTakenAny(subjects, Set.of(JEONGYUNG_REQUIRED_BASIC_COURSE));
        int majorRequired = req.isDoubleMajor() ? MAJOR_DOUBLE_REQUIRED : MAJOR_SINGLE_REQUIRED;

        List<RequirementItem> items = new ArrayList<>(List.of(
                RequirementItem.of("총 취득학점", c.chidukHakjum(), TOTAL_REQUIRED),
                RequirementItem.of("전공 취득학점", c.majorChidukHakjum(), majorRequired),
                RequirementItem.of("교양 취득학점", c.cultureChidukHakjum(), CULTURE_REQUIRED)
        ));
        items.add(new RequirementItem("기초교양필수(" + JEONGYUNG_REQUIRED_BASIC_COURSE + ")",
                basicCourseDone ? 1 : 0, 1, basicCourseDone));
        items.add(new RequirementItem("캡스톤 이수", capstoneDone ? 1 : 0, 1, capstoneDone));

        return RequirementCheckResponse.of(items);
    }

    private RequirementCheckResponse checkComputerEngineering(CreditSummaryResponse c, List<KlasSubjectGradeResponse> subjects,
                                                              RequirementCheckRequest req) {
        int majorRequired = req.isDoubleMajor() ? MAJOR_DOUBLE_REQUIRED : MAJOR_SINGLE_REQUIRED;

        List<RequirementItem> items = new ArrayList<>(List.of(
                RequirementItem.of("전공 취득학점(설계 포함)", c.majorChidukHakjum(), majorRequired),
                RequirementItem.of("교양 취득학점", c.cultureChidukHakjum(), CULTURE_REQUIRED)
        ));

        if (req.isEngineeringProgram()) {
            boolean mscRequiredTaken = CE_MSC_REQUIRED_SUBJECTS.stream()
                    .allMatch(name -> hasTakenAny(subjects, Set.of(name)));
            int mscTotalCredit = sumCredit(subjects, CE_MSC_POOL_SUBJECTS);
            int designCredit = sumDesignCredit(subjects);
            boolean designMinimumOk = hasMinimumDesignRequirement(subjects, CE_DESIGN_MANDATORY, CE_DESIGN_ONE_OF);

            items.add(new RequirementItem("MSC 필수과목 7개 이수", mscRequiredTaken ? 1 : 0, 1, mscRequiredTaken));
            items.add(RequirementItem.of("MSC 총 이수학점(27학점)", mscTotalCredit, CE_MSC_TOTAL_REQUIRED));
            items.add(RequirementItem.of("설계 학점", designCredit, DESIGN_REQUIRED));
            items.add(new RequirementItem("전공 최소설계 필수과목(공학설계입문·이산수학·캡스톤1or2)", designMinimumOk ? 1 : 0, 1, designMinimumOk));
        }

        boolean topcit = Boolean.TRUE.equals(req.topcitPassed());
        items.add(new RequirementItem("TOPCIT 응시", topcit ? 1 : 0, 1, topcit));

        return RequirementCheckResponse.of(items);
    }

    private RequirementCheckResponse checkSoftware(CreditSummaryResponse c, List<KlasSubjectGradeResponse> subjects,
                                                   RequirementCheckRequest req) {
        int majorRequired = req.isDoubleMajor() ? MAJOR_DOUBLE_REQUIRED : MAJOR_SINGLE_REQUIRED;

        List<RequirementItem> items = new ArrayList<>(List.of(
                RequirementItem.of("전공 취득학점", c.majorChidukHakjum(), majorRequired),
                RequirementItem.of("교양 취득학점", c.cultureChidukHakjum(), CULTURE_REQUIRED)
        ));

        boolean majorRequiredTaken = SOFTWARE_REQUIRED_SUBJECTS.stream()
                .allMatch(name -> hasTakenAny(subjects, Set.of(name)));
        items.add(new RequirementItem("전공필수 4과목 이수", majorRequiredTaken ? 1 : 0, 1, majorRequiredTaken));

        if ("인공지능전공".equals(req.subMajor())) {
            int aiTakenCount = countTaken(subjects, AI_TRACK_SUBJECTS);
            items.add(RequirementItem.of("인공지능전공 세부필수(3과목 이상)", aiTakenCount, AI_TRACK_REQUIRED_COUNT));
        }

        boolean topcit = Boolean.TRUE.equals(req.topcitPassed());
        boolean thesis = Boolean.TRUE.equals(req.thesisPassed());
        items.add(new RequirementItem("TOPCIT 응시", topcit ? 1 : 0, 1, topcit));
        items.add(new RequirementItem("졸업논문 '가' 판정", thesis ? 1 : 0, 1, thesis));

        if (req.isEngineeringProgram()) {
            int mscTotalCredit = sumCredit(subjects, SW_MSC_POOL_SUBJECTS);
            int designCredit = sumDesignCredit(subjects);
            boolean designMinimumOk = hasMinimumDesignRequirement(subjects, SW_DESIGN_MANDATORY, Set.of());

            items.add(RequirementItem.of("MSC 총 이수학점(12학점)", mscTotalCredit, SW_MSC_TOTAL_REQUIRED));
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