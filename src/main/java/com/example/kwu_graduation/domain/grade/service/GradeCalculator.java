package com.example.kwu_graduation.domain.grade.service;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeCalculator {

    public CreditSummaryResponse calculate(List<KlasSemesterGradeResponse> semesters) {
        int applyHakjum = 0;
        int majorApplyHakjum = 0;
        int cultureApplyHakjum = 0;
        int etcApplyHakjum = 0;

        int chidukHakjum = 0;
        int majorChidukHakjum = 0;
        int cultureChidukHakjum = 0;
        int etcChidukHakjum = 0;

        int delHakjum = 0;
        int majorDelHakjum = 0;
        int cultureDelHakjum = 0;
        int etcDelHakjum = 0;

        int retakeApplyHakjum = 0;
        int retakeChidukHakjum = 0;
        int retakeDelHakjum = 0;

        for (KlasSemesterGradeResponse semester : semesters) {
            for (KlasSubjectGradeResponse subject : semester.sungjukList()) {
                int credit = subject.hakjumNum() == null ? 0 : subject.hakjumNum();

                if (isCurrentTaking(subject)) {
                    applyHakjum += credit;

                    if (isMajor(subject)) {
                        majorApplyHakjum += credit;
                    } else if (isCulture(subject)) {
                        cultureApplyHakjum += credit;
                    } else {
                        etcApplyHakjum += credit;
                    }

                    if (isRetake(subject)) {
                        retakeApplyHakjum += credit;
                    }

                    continue;
                }

                if (isDeleted(subject)) {
                    delHakjum += credit;

                    if (isMajor(subject)) {
                        majorDelHakjum += credit;
                    } else if (isCulture(subject)) {
                        cultureDelHakjum += credit;
                    } else {
                        etcDelHakjum += credit;
                    }

                    retakeDelHakjum += credit;
                    continue;
                }

                if (isCompleted(subject)) {
                    chidukHakjum += credit;

                    if (isMajor(subject)) {
                        majorChidukHakjum += credit;
                    } else if (isCulture(subject)) {
                        cultureChidukHakjum += credit;
                    } else {
                        etcChidukHakjum += credit;
                    }

                    if (isRetake(subject)) {
                        retakeChidukHakjum += credit;
                    }
                }
            }
        }

        return new CreditSummaryResponse(
                applyHakjum,
                majorApplyHakjum,
                cultureApplyHakjum,
                etcApplyHakjum,
                chidukHakjum,
                majorChidukHakjum,
                cultureChidukHakjum,
                etcChidukHakjum,
                delHakjum,
                majorDelHakjum,
                cultureDelHakjum,
                etcDelHakjum,
                retakeApplyHakjum,
                retakeChidukHakjum,
                retakeDelHakjum
        );
    }

    private boolean isCurrentTaking(KlasSubjectGradeResponse subject) {
        return "N".equals(subject.termFinish())
                && subject.getGrade() != null
                && subject.getGrade().isBlank();
    }

    private boolean isCompleted(KlasSubjectGradeResponse subject) {
        return "Y".equals(subject.finishOpt())
                && "Y".equals(subject.termFinish())
                && !isDeleted(subject);
    }

    private boolean isDeleted(KlasSubjectGradeResponse subject) {
        return "3".equals(subject.sungjukOpt())
                || (subject.getGrade() != null && subject.getGrade().contains("삭제"));
    }

    private boolean isRetake(KlasSubjectGradeResponse subject) {
        return "Y".equals(subject.retakeOpt());
    }

    private boolean isMajor(KlasSubjectGradeResponse subject) {
        return subject.codeName1() != null && subject.codeName1().startsWith("전");
    }

    private boolean isCulture(KlasSubjectGradeResponse subject) {
        return subject.codeName1() != null && subject.codeName1().startsWith("교");
    }
}