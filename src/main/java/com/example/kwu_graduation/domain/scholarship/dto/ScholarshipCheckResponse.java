package com.example.kwu_graduation.domain.scholarship.dto;

import java.util.List;

public record ScholarshipCheckResponse(
        List<ScholarshipResult> results
) {
    public static ScholarshipCheckResponse of(List<ScholarshipResult> results) {
        return new ScholarshipCheckResponse(results);
    }
}