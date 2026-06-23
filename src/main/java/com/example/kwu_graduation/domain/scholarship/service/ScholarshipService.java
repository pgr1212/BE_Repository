package com.example.kwu_graduation.domain.scholarship.service;

import com.example.kwu_graduation.domain.scholarship.dto.ScholarshipCheckRequest;
import com.example.kwu_graduation.domain.scholarship.dto.ScholarshipCheckResponse;
import com.example.kwu_graduation.domain.scholarship.dto.ScholarshipResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScholarshipService {

    public ScholarshipCheckResponse check(ScholarshipCheckRequest req) {
        List<ScholarshipResult> results = new ArrayList<>();

        int creditThreshold15 = req.grade() == 4 ? 12 : 15; // 4학년은 12학점 기준 완화

        // 1. 단과대학 수석/참빛/비마장학금 (학점만으로 100% 판별 가능)
        results.add(checkTopScholarship("단과대학 수석/참빛/비마장학금", req, creditThreshold15, 3.0));

        // 2. 화도/동해장학금 (가정형편 곤란 - 자동판별 불가)
        results.add(checkHardshipScholarship("화도/동해장학금", req, 12, 2.0));

        // 3. 프론티어장학금 (학과 봉사활동 인정 - 자동판별 불가)
        results.add(checkServiceScholarship("프론티어장학금", req, creditThreshold15, 2.5));

        // 4. 가족장학금 (직계가족 2명 이상 재학 - 자동판별 불가)
        results.add(checkFamilyScholarship("가족장학금", req, 15, 2.5));

        // 5. 봉사장학금 (학생회·교내단체 공로 - 자동판별 불가)
        results.add(checkCouncilScholarship("봉사장학금", req, 15, 2.0));

        return ScholarshipCheckResponse.of(results);
    }

    private ScholarshipResult checkTopScholarship(String name, ScholarshipCheckRequest req,
                                                  int creditRequired, double gpaRequired) {
        if (req.recentSemesterCredit() >= creditRequired && req.recentSemesterGpa() >= gpaRequired) {
            return ScholarshipResult.confirmed(name,
                    String.format("직전학기 %d학점, 평점 %.1f → 조건 충족", req.recentSemesterCredit(), req.recentSemesterGpa()));
        }
        return ScholarshipResult.fail(name,
                String.format("학점/평점 기준 미달 (필요: %d학점·평점%.1f)", creditRequired, gpaRequired));
    }

    private ScholarshipResult checkHardshipScholarship(String name, ScholarshipCheckRequest req,
                                                       int creditRequired, double gpaRequired) {
        boolean creditOk = req.recentSemesterCredit() >= creditRequired && req.recentSemesterGpa() >= gpaRequired;
        if (!creditOk) {
            return ScholarshipResult.fail(name, "학점/평점 기준 미달");
        }
        if (Boolean.TRUE.equals(req.isFinancialHardship())) {
            return ScholarshipResult.confirmed(name, "학점 충족 + 가정형편 곤란 확인됨");
        }
        return ScholarshipResult.partial(name, "학점 조건 충족 — 가정형편 심사 별도 확인 필요");
    }

    private ScholarshipResult checkServiceScholarship(String name, ScholarshipCheckRequest req,
                                                      int creditRequired, double gpaRequired) {
        boolean creditOk = req.recentSemesterCredit() >= creditRequired && req.recentSemesterGpa() >= gpaRequired;
        if (!creditOk) {
            return ScholarshipResult.fail(name, "학점/평점 기준 미달");
        }
        if (Boolean.TRUE.equals(req.hasDeptServiceRecognition())) {
            return ScholarshipResult.confirmed(name, "학점 충족 + 학과 봉사활동 인정됨");
        }
        return ScholarshipResult.partial(name, "학점 조건 충족 — 학과 봉사활동 인정 여부 확인 필요");
    }

    private ScholarshipResult checkFamilyScholarship(String name, ScholarshipCheckRequest req,
                                                     int creditRequired, double gpaRequired) {
        boolean creditOk = req.recentSemesterCredit() >= creditRequired && req.recentSemesterGpa() >= gpaRequired;
        if (!creditOk) {
            return ScholarshipResult.fail(name, "학점/평점 기준 미달");
        }
        if (req.familyMembersEnrolled() != null && req.familyMembersEnrolled() >= 2) {
            return ScholarshipResult.confirmed(name, "학점 충족 + 직계가족 2명 이상 재학 확인됨");
        }
        return ScholarshipResult.partial(name, "학점 조건 충족 — 직계가족 재학 인원 확인 필요");
    }

    private ScholarshipResult checkCouncilScholarship(String name, ScholarshipCheckRequest req,
                                                      int creditRequired, double gpaRequired) {
        boolean creditOk = req.recentSemesterCredit() >= creditRequired && req.recentSemesterGpa() >= gpaRequired;
        if (!creditOk) {
            return ScholarshipResult.fail(name, "학점/평점 기준 미달");
        }
        if (Boolean.TRUE.equals(req.hasCouncilOrOrgContribution())) {
            return ScholarshipResult.confirmed(name, "학점 충족 + 학생회·교내단체 공로 확인됨");
        }
        return ScholarshipResult.partial(name, "학점 조건 충족 — 공로 인정 여부 확인 필요");
    }
}