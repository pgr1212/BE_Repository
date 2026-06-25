package com.example.kwu_graduation.domain.simulation.parser;

import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;
import java.util.List;

@Component
public class KlasGradeParser {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<KlasSemesterGradeResponse> parseSemesters(String rawGrades) {
        if (rawGrades == null || rawGrades.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 응답이 비어 있습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(rawGrades);
            JsonNode arrayNode = findSemesterArray(root);
            if (arrayNode == null || !arrayNode.isArray()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 응답에서 학기 목록을 찾을 수 없습니다.");
            }
            return objectMapper.convertValue(arrayNode, new TypeReference<List<KlasSemesterGradeResponse>>() {
            });
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 응답(JSON) 파싱에 실패했습니다.", e);
        }
    }

    private JsonNode findSemesterArray(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            return node;
        }
        if (!node.isObject()) {
            return null;
        }

        for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
            JsonNode child = it.next();
            if (isSemesterArray(child)) {
                return child;
            }
        }

        for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
            JsonNode found = findSemesterArray(it.next());
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private boolean isSemesterArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return false;
        }
        return node.isEmpty() || node.get(0).has("sungjukList");
    }
}
