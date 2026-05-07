package com.example.kwu_graduation.domain.grade.dto;

public record KlasSubjectGradeResponse(
        String gwamokKname,
        String codeName1,
        Integer hakjumNum,
        String getGrade,
        String certname,
        String hakgwa,
        String hakjungNo,
        String finishOpt,
        String sungjukOpt,
        String retakeOpt,
        String retakeGetGrade,
        Integer entrytime,
        String termCheck,
        String termFinish
) {
}