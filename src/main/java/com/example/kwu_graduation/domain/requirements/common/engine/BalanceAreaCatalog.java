package com.example.kwu_graduation.domain.requirements.common.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 균형교양 영역별 교과목 카탈로그.
 * resources/requirements/common/balance_areas.json 을 읽어
 * "과목명 → 균형교양 영역" 매핑을 제공한다.
 *
 * <p>과목명은 공백·대소문자를 무시하고 정규화하여 비교한다.
 * 1순위로 정확히 일치하는 영역을 찾고, 없으면 카탈로그 과목명이 성적의 과목명에
 * 포함되는지(접두 매칭 등)로 보완 탐색한다.
 */
@Component
public class BalanceAreaCatalog {

    private static final String DEFAULT_RESOURCE = "requirements/common/balance_areas.json";

    /** 정규화된 과목명 → 영역(표시용 이름) */
    private final Map<String, String> exactIndex = new LinkedHashMap<>();
    /** 보완 탐색용: (정규화된 카탈로그 과목명, 영역). 긴 이름부터 우선 매칭. */
    private final List<Entry> containsIndex = new ArrayList<>();

    private record Entry(String normalizedName, String area) {
    }

    public BalanceAreaCatalog() {
        this(DEFAULT_RESOURCE);
    }

    public BalanceAreaCatalog(String resourcePath) {
        Map<String, List<String>> raw = load(resourcePath);
        for (Map.Entry<String, List<String>> areaEntry : raw.entrySet()) {
            String area = areaEntry.getKey();
            if (area.startsWith("_") || areaEntry.getValue() == null) {
                continue; // "_comment" 등 메타데이터 건너뜀
            }
            for (String course : areaEntry.getValue()) {
                String key = normalize(course);
                if (key.isBlank()) {
                    continue;
                }
                exactIndex.putIfAbsent(key, area);
                containsIndex.add(new Entry(key, area));
            }
        }
        // 보완 탐색 시 더 구체적인(긴) 과목명이 먼저 매칭되도록 정렬
        containsIndex.sort((a, b) -> Integer.compare(b.normalizedName().length(), a.normalizedName().length()));
    }

    /**
     * 과목명이 속한 균형교양 영역을 반환한다.
     * 어떤 균형교양 영역에도 속하지 않으면 비어 있는 Optional 을 반환한다(→ 기타 처리).
     */
    public Optional<String> areaOf(String courseName) {
        if (courseName == null) {
            return Optional.empty();
        }
        String normalized = normalize(courseName);
        if (normalized.isBlank()) {
            return Optional.empty();
        }
        String exact = exactIndex.get(normalized);
        if (exact != null) {
            return Optional.of(exact);
        }
        for (Entry entry : containsIndex) {
            if (entry.normalizedName().length() >= 4 && normalized.contains(entry.normalizedName())) {
                return Optional.of(entry.area());
            }
        }
        return Optional.empty();
    }

    /**
     * 영역명 → 과목명 배열 형태로 로드한다.
     * 값이 배열이 아닌 메타데이터(예: "_comment" 문자열)는 건너뛰어 파싱이 깨지지 않도록 한다.
     */
    private Map<String, List<String>> load(String resourcePath) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode root = mapper.readTree(is);
            Map<String, List<String>> result = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> field = it.next();
                if (!field.getValue().isArray()) {
                    continue;
                }
                List<String> courses = new ArrayList<>();
                field.getValue().forEach(node -> courses.add(node.asText()));
                result.put(field.getKey(), courses);
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("균형교양 영역 카탈로그(%s) 로드에 실패했습니다.".formatted(resourcePath), e);
        }
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", "").toLowerCase();
    }
}
