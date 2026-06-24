package com.example.kwu_graduation.domain.simulation.parser;

import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KlasGradeParser {

    private static final List<String> SEMESTER_LIST_FIELD_NAMES = List.of(
            "data",
            "list",
            "semesterList",
            "semesters"
    );

    private final ObjectMapper objectMapper;

    public List<KlasSemesterGradeResponse> parse(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 응답이 비어 있습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode semestersNode = findSemestersNode(root);
            return objectMapper.convertValue(semestersNode, new TypeReference<>() {
            });
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 응답 형식이 올바르지 않습니다.", e);
        }
    }

    private JsonNode findSemestersNode(JsonNode root) {
        if (root.isArray()) {
            return root;
        }

        if (!root.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 학기 목록을 찾을 수 없습니다.");
        }

        for (String fieldName : SEMESTER_LIST_FIELD_NAMES) {
            JsonNode candidate = root.get(fieldName);
            if (candidate != null && candidate.isArray()) {
                return candidate;
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 학기 목록을 찾을 수 없습니다.");
    }
}
