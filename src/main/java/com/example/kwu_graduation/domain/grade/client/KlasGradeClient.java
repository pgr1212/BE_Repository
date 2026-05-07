package com.example.kwu_graduation.domain.grade.client;

import com.example.kwu_graduation.domain.grade.dto.KlasCreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class KlasGradeClient {

    private static final String KLAS_BASE_URL = "https://klas.kw.ac.kr";

    private final RestClient restClient;

    public KlasGradeClient() {
        this.restClient = RestClient.builder()
                .baseUrl(KLAS_BASE_URL)
                .build();
    }

    public KlasCreditSummaryResponse getCreditSummary(String cookie) {
        return restClient.post()
                .uri("/std/cps/inqire/AtnlcScreSungjukTot.do")
                .contentType(MediaType.parseMediaType("application/json;charset=utf-8"))
                .header("X-Requested-With", "XMLHttpRequest")
                .header(HttpHeaders.COOKIE, cookie)
                .body(Map.of())
                .retrieve()
                .body(KlasCreditSummaryResponse.class);
    }

    public List<KlasSemesterGradeResponse> getSemesterGrades(String cookie) {
        return restClient.post()
                .uri("/std/cps/inqire/AtnlcScreSungjukInfo.do")
                .contentType(MediaType.parseMediaType("application/json;charset=utf-8"))
                .header("X-Requested-With", "XMLHttpRequest")
                .header(HttpHeaders.COOKIE, cookie)
                .body(Map.of())
                .retrieve()
                .body(new ParameterizedTypeReference<List<KlasSemesterGradeResponse>>() {});
    }
}
