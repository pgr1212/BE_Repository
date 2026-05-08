package com.example.kwu_graduation.domain.grade.dto;

import java.util.List;

public record KlasSemesterGradeResponse(
        String thisYear,
        String hakgi,
        String hakgiOrder,
        List<KlasSubjectGradeResponse> sungjukList
) {
}