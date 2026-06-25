package com.example.kwu_graduation.domain.simulation.hakbun24.spec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * classpath(resources/requirements/...)에 정의된 졸업요건 JSON을 읽어 캐싱한다.
 * 요건 값은 학사 정책이라 자주 바뀌지 않으므로 한 번 읽어 메모리에 보관한다.
 */
@Component("hakbun24RequirementSpecLoader")
public class RequirementSpecLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, GraduationRequirement> cache = new ConcurrentHashMap<>();

    public GraduationRequirement load(int admissionYear, Department department) {
        String path = "requirements/hakbun24/%s.json".formatted(department.getCode());
        return cache.computeIfAbsent(path, this::read);
    }

    private GraduationRequirement read(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalArgumentException("졸업요건 정의 파일을 찾을 수 없습니다: " + path);
        }
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, GraduationRequirement.class);
        } catch (IOException e) {
            throw new IllegalStateException("졸업요건 정의 파일 로딩에 실패했습니다: " + path, e);
        }
    }
}
