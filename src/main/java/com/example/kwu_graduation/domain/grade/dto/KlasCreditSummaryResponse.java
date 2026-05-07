package com.example.kwu_graduation.domain.grade.dto;

import java.math.BigDecimal;

public record KlasCreditSummaryResponse(
        Integer applyHakjum,
        Integer majorApplyHakjum,
        Integer cultureApplyHakjum,
        Integer etcApplyHakjum,

        Integer chidukHakjum,
        Integer majorChidukHakjum,
        Integer cultureChidukHakjum,
        Integer etcChidukHakjum,

        Integer delHakjum,
        Integer majorDelHakjum,
        Integer cultureDelHakjum,
        Integer etcDelHakjum,

        Integer retakeApplyHakjum,
        Integer retakeMajorApplyHakjum,
        Integer retakeCultureApplyHakjum,
        Integer retakeEtcApplyHakjum,

        Integer retakeChidukHakjum,
        Integer retakeMajorChidukHakjum,
        Integer retakeCultureChidukHakjum,
        Integer retakeEtcChidukHakjum,

        Integer retakeDelHakjum,
        Integer retakeMajorDelHakjum,
        Integer retakeCultureDelHakjum,
        Integer retakeEtcDelHakjum,

        BigDecimal hwakinScoresum,
        BigDecimal jaechulScoresum
) {
}