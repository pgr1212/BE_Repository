package com.example.kwu_graduation.domain.student.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KlasStudentClient {

    private static final String KLAS_BASE_URL = "https://klas.kw.ac.kr";

    private final RestClient restClient;

    public KlasStudentClient() {
        this.restClient = RestClient.builder()
                .baseUrl(KLAS_BASE_URL)
                .build();
    }

    public String getStudentInfo(String cookie) {
        try {
            return restClient.post()
                    .uri("/std/hak/hakjuk/CompnoApplyResult.do")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.ALL)
                    .header(HttpHeaders.COOKIE, cookie)
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                    .header(HttpHeaders.ORIGIN, "https://klas.kw.ac.kr")
                    .header(HttpHeaders.REFERER, "https://klas.kw.ac.kr/std/cmn/frame/Frame.do")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body("")
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException e) {
            System.out.println("KLAS Student Info API Status = " + e.getStatusCode());
            System.out.println("KLAS Student Info API Body = " + e.getResponseBodyAsString());
            throw e;
        }
    }
}