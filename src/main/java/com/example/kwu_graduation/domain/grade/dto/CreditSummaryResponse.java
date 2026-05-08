package com.example.kwu_graduation.domain.grade.dto;

public record CreditSummaryResponse(
        int applyHakjum,
        int majorApplyHakjum,
        int cultureApplyHakjum,
        int etcApplyHakjum,

        int chidukHakjum,
        int majorChidukHakjum,
        int cultureChidukHakjum,
        int etcChidukHakjum,

        int delHakjum,
        int majorDelHakjum,
        int cultureDelHakjum,
        int etcDelHakjum,

        int retakeApplyHakjum,
        int retakeChidukHakjum,
        int retakeDelHakjum
) {
}