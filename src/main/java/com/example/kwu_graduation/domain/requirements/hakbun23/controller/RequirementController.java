package com.example.kwu_graduation.domain.requirements.hakbun23.controller;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.grade.service.GradeCalculator;
import com.example.kwu_graduation.domain.grade.service.KlasGradeService;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.service.RequirementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/requirements/2020-2023")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;
    private final KlasGradeService klasGradeService;
    private final GradeCalculator gradeCalculator;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @GetMapping("/check")
    public RequirementCheckResponse check(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestParam String department,
            @RequestParam(defaultValue = "false") boolean isDoubleMajor,
            @RequestParam(defaultValue = "false") boolean isEngineeringProgram,
            @RequestParam(required = false) Boolean topcitPassed,
            @RequestParam(required = false) Boolean thesisPassed,
            @RequestParam(required = false) String subMajor
    ) {
        // 1. KLAS에서 성적 원본 조회
        String rawGrades = klasGradeService.getSemesterGrades(cookie);
        List<KlasSemesterGradeResponse> semesters = parseSemesters(rawGrades);

        // 2. 학점 집계 + 과목 목록 평탄화
        CreditSummaryResponse creditSummary = gradeCalculator.calculate(semesters);
        List<KlasSubjectGradeResponse> subjects = semesters.stream()
                .flatMap(s -> s.sungjukList() == null ? Stream.empty() : s.sungjukList().stream())
                .toList();

        // 3. 판정 요청 구성 (기존 RequirementService 그대로 사용)
        RequirementCheckRequest request = new RequirementCheckRequest(
                creditSummary, subjects, isDoubleMajor, isEngineeringProgram,
                topcitPassed, thesisPassed, subMajor
        );

        return requirementService.check(department, request);
    }

    /** KLAS 성적 응답(JSON 문자열)을 학기 목록으로 파싱한다. */
    private List<KlasSemesterGradeResponse> parseSemesters(String rawGrades) {
        try {
            JsonNode root = objectMapper.readTree(rawGrades);
            JsonNode arrayNode = findSemesterArray(root);
            if (arrayNode == null || !arrayNode.isArray()) {
                throw new IllegalStateException("성적 응답에서 학기 목록을 찾을 수 없습니다.");
            }
            return objectMapper.convertValue(arrayNode, new TypeReference<List<KlasSemesterGradeResponse>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("성적 응답(JSON) 파싱에 실패했습니다.", e);
        }
    }

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
                if (found != null) return found;
            }
        }
        return null;
    }
}