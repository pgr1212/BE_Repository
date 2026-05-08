package com.example.kwu_graduation.domain.auth.client;

import com.example.kwu_graduation.domain.auth.util.RsaEncryptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KlasClient {

    private static final String KLAS_BASE_URL = "https://klas.kw.ac.kr";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KlasClient() {
        this.restClient = RestClient.builder()
                .baseUrl(KLAS_BASE_URL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String login(String studentId, String password) {
        ResponseEntity<String> securityResponse = restClient.post()
                .uri("/usr/cmn/login/LoginSecurity.do")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Requested-With", "XMLHttpRequest")
                .retrieve()
                .toEntity(String.class);

        String securityCookie = extractCookie(securityResponse.getHeaders());
        String publicKey = extractPublicKey(securityResponse.getBody());
        String loginToken = createLoginToken(studentId, password, publicKey);

        Map<String, String> loginConfirmBody = Map.of(
                "loginToken", loginToken,
                "redirectUrl", "",
                "redirectTabUrl", ""
        );

        ResponseEntity<String> loginResponse = restClient.post()
                .uri("/usr/cmn/login/LoginConfirm.do")
                .contentType(MediaType.parseMediaType("application/json;charset=utf-8"))
                .header("X-Requested-With", "XMLHttpRequest")
                .header(HttpHeaders.COOKIE, securityCookie)
                .body(loginConfirmBody)
                .retrieve()
                .toEntity(String.class);

        if (!loginResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("KLAS 로그인에 실패했습니다.");
        }

        String finalCookie = mergeCookies(securityCookie, loginResponse.getHeaders());

        System.out.println("LoginConfirm Body = " + loginResponse.getBody());
        System.out.println("LoginConfirm Set-Cookie = " + loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
        System.out.println("Final Cookie = " + finalCookie);

        return finalCookie;
    }

    private String extractCookie(HttpHeaders headers) {
        List<String> setCookies = headers.get(HttpHeaders.SET_COOKIE);

        if (setCookies == null || setCookies.isEmpty()) {
            throw new IllegalStateException("KLAS 응답에서 Set-Cookie를 찾을 수 없습니다.");
        }

        return setCookies.stream()
                .map(cookie -> cookie.split(";", 2)[0])
                .filter(cookie -> cookie.contains("="))
                .collect(Collectors.joining("; ", "", ";"));
    }

    private String mergeCookies(String originalCookie, HttpHeaders headers) {
        Map<String, String> cookieMap = cookieStringToMap(originalCookie);

        List<String> setCookies = headers.get(HttpHeaders.SET_COOKIE);

        if (setCookies != null) {
            setCookies.stream()
                    .map(cookie -> cookie.split(";", 2)[0])
                    .filter(cookie -> cookie.contains("="))
                    .map(cookie -> cookie.split("=", 2))
                    .forEach(arr -> cookieMap.put(arr[0], arr[1]));
        }

        return cookieMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; ", "", ";"));
    }

    private Map<String, String> cookieStringToMap(String cookie) {
        return List.of(cookie.split(";")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .filter(value -> value.contains("="))
                .map(value -> value.split("=", 2))
                .collect(Collectors.toMap(
                        arr -> arr[0],
                        arr -> arr[1],
                        (oldValue, newValue) -> newValue
                ));
    }

    private String extractPublicKey(String responseBody) {
        try {
            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException("KLAS LoginSecurity 응답 바디가 비어 있습니다.");
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode publicKeyNode = jsonNode.get("publicKey");

            if (publicKeyNode == null || publicKeyNode.asText().isBlank()) {
                throw new IllegalStateException("publicKey가 없습니다.");
            }

            return publicKeyNode.asText();
        } catch (Exception e) {
            throw new IllegalStateException("publicKey 파싱에 실패했습니다.", e);
        }
    }

    private String createLoginToken(String studentId, String password, String publicKey) {
        try {
            Map<String, String> payload = Map.of(
                    "loginId", studentId,
                    "loginPwd", password,
                    "storeIdYn", "N"
            );

            String payloadJson = objectMapper.writeValueAsString(payload);

            return RsaEncryptor.encryptWithPublicKey(payloadJson, publicKey);
        } catch (Exception e) {
            throw new IllegalStateException("loginToken 생성에 실패했습니다.", e);
        }
    }
}