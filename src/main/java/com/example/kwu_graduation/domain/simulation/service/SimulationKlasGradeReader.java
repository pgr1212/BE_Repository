package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.service.KlasGradeService;
import com.example.kwu_graduation.domain.simulation.parser.KlasGradeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SimulationKlasGradeReader {

    private final KlasGradeService klasGradeService;
    private final KlasGradeParser klasGradeParser;

    public List<KlasSemesterGradeResponse> readSemesters(String cookie) {
        if (cookie == null || cookie.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Klas-Cookie 헤더는 필수입니다.");
        }

        try {
            String rawGrades = klasGradeService.getSemesterGrades(cookie);
            return klasGradeParser.parseSemesters(rawGrades);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "KLAS 성적 조회에 실패했습니다.", e);
        }
    }
}
