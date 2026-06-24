package com.example.kwu_graduation.domain.requirements.hakbun24.spec;

import java.util.Arrays;

/**
 * 졸업요건 조회 대상 학과.
 * code 는 요건 JSON 파일명(requirements/hakbun{yy}/{code}.json)과 1:1로 매칭된다.
 * 학과가 추가되면 여기에 enum 상수와 JSON 파일만 추가하면 된다.
 */
public enum Department {

    JEONGYUNG("jeongyung", "정보융합학부"),
    COMPUTER("computer", "컴퓨터정보공학부"),
    SOFTWARE("software", "소프트웨어학부");

    private final String code;
    private final String displayName;

    Department(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Department fromCode(String code) {
        return Arrays.stream(values())
                .filter(d -> d.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 학과입니다: " + code));
    }
}
