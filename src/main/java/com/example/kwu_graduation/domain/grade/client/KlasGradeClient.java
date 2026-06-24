package com.example.kwu_graduation.domain.grade.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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
        try {
            return restClient.post()
                    .uri("/std/cps/inqire/AtnlcScreSungjukInfo.do")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.ALL)
                    .header(HttpHeaders.COOKIE, cookie)
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                    .header(HttpHeaders.ORIGIN, "https://klas.kw.ac.kr")
                    .header(HttpHeaders.REFERER, "https://klas.kw.ac.kr/std/cps/inqire/AtnlcScreSungjukInfo.do")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body("")
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException e) {
            System.out.println("KLAS Semester Grades API Status = " + e.getStatusCode());
            System.out.println("KLAS Semester Grades API Body = " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public String getGradeSummary(String cookie) {
        try {
            return restClient.post()
                    .uri("/std/cps/inqire/AtnlcScreSungjukTot.do")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.ALL)
                    .header(HttpHeaders.COOKIE, cookie)
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                    .header(HttpHeaders.ORIGIN, "https://klas.kw.ac.kr")
                    .header(HttpHeaders.REFERER, "https://klas.kw.ac.kr/std/cps/inqire/AtnlcScreSungjukInfo.do")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body("")
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException e) {
            System.out.println("KLAS Grade Summary API Status = " + e.getStatusCode());
            System.out.println("KLAS Grade Summary API Body = " + e.getResponseBodyAsString());
            throw e;
        }
    }
}