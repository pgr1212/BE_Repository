package com.example.kwu_graduation.domain.scholarship.dto;

public record ScholarshipResult(
        String name,
        String grade,   // "A" (확정 가능), "B" (학점 충족, 추가확인 필요), "FAIL" (학점 미충족)
        String reason
) {
    public static ScholarshipResult confirmed(String name, String reason) {
        return new ScholarshipResult(name, "A", reason);
    }
    public static ScholarshipResult partial(String name, String reason) {
        return new ScholarshipResult(name, "B", reason);
    }
    public static ScholarshipResult fail(String name, String reason) {
        return new ScholarshipResult(name, "FAIL", reason);
    }
}