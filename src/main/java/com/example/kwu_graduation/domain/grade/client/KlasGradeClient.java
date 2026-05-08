package com.example.kwu_graduation.domain.grade.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    public String getSemesterGrades(String cookie) {
        return restClient.post()
                .uri("/std/cps/inqire/AtnlcScreSungjukInfo.do")
                .contentType(MediaType.parseMediaType("application/json;charset=utf-8"))
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Requested-With", "XMLHttpRequest")
                .header(HttpHeaders.ORIGIN, "https://klas.kw.ac.kr")
                .header(HttpHeaders.REFERER, "https://klas.kw.ac.kr/std/cps/inqire/AtnlcScreSungjukInfo.do")
                .header(HttpHeaders.COOKIE, cookie)
                .body(Map.of())
                .retrieve()
                .body(String.class);
    }
}