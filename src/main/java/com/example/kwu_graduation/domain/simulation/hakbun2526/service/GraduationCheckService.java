package com.example.kwu_graduation.domain.simulation.hakbun2526.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.grade.service.GradeCalculator;
import com.example.kwu_graduation.domain.grade.service.KlasGradeService;
import com.example.kwu_graduation.domain.simulation.hakbun2526.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.simulation.hakbun2526.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.simulation.hakbun2526.engine.RequirementChecker;
import com.example.kwu_graduation.domain.simulation.hakbun2526.spec.Department;
import com.example.kwu_graduation.domain.simulation.hakbun2526.spec.GraduationRequirement;
import com.example.kwu_graduation.domain.simulation.hakbun2526.spec.RequirementSpecLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 학번별 졸업요건 점검의 공통 흐름을 담당한다.
 * 1) 요건 정의 로드 → 2) KLAS 성적 조회 → 3) 파싱 → 4) 학점 집계 → 5) 판정.
 * hakbun25/hakbun26 서비스는 입학 학번만 지정해 이 서비스를 호출한다.
 */
@Service
@RequiredArgsConstructor
public class GraduationCheckService {

    private final KlasGradeService klasGradeService;
    private final GradeCalculator gradeCalculator;
    private final RequirementSpecLoader specLoader;
    private final RequirementChecker requirementChecker;
    // KLAS가 향후 필드를 추가해도 깨지지 않도록 미지의 필드는 무시한다.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RequirementCheckResponse check(
            int admissionYear,
            Department department,
            String cookie,
            RequirementCheckRequest input
    ) {
        GraduationRequirement spec = specLoader.load(admissionYear, department);

        String rawGrades = klasGradeService.getSemesterGrades(cookie);
        List<KlasSemesterGradeResponse> semesters = parseSemesters(rawGrades);

        CreditSummaryResponse credits = gradeCalculator.calculate(semesters);
        List<KlasSubjectGradeResponse> subjects = semesters.stream()
                .flatMap(s -> s.sungjukList() == null ? Stream.empty() : s.sungjukList().stream())
                .toList();

        RequirementCheckRequest safeInput = input == null ? RequirementCheckRequest.empty() : input;
        return requirementChecker.check(spec, credits, subjects, safeInput);
    }

    /**
     * KLAS 성적 응답(JSON 문자열)을 학기 목록으로 파싱한다.
     * 응답이 학기 배열이거나, 배열을 감싼 객체 형태여도 대응할 수 있도록 배열 노드를 탐색한다.
     */
    private List<KlasSemesterGradeResponse> parseSemesters(String rawGrades) {
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

    private JsonNode findSemesterArray(JsonNode node) {
        if (node.isArray()) {
            return node;
        }
        if (node.isObject()) {
            // sungjukList 를 포함하는 학기 배열을 우선 탐색
            for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
                JsonNode child = it.next();
                if (child.isArray() && (child.isEmpty() || child.get(0).has("sungjukList"))) {
                    return child;
                }
            }
            // 못 찾으면 하위 객체를 재귀 탐색
            for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
                JsonNode found = findSemesterArray(it.next());
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
