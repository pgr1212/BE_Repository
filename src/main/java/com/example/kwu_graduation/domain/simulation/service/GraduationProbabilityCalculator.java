package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.GpaStats;
import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * 졸업 가능 확률 계산기.
 *
 * <p>"졸업 가능 확률"은 <b>현재 누적 평점</b>과 <b>남은(다음 학기에 들을) 학점 수</b>를 비교했을 때,
 * 남은 학점 동안 받을 성적까지 합산한 <b>졸업 시점 누적 평점</b>이 <b>목표 누적 평점</b>에
 * 도달할 수 있는 확률이다.
 *
 * <h3>계산 모델</h3>
 * <ul>
 *   <li>이미 취득한 학점 {@code C_earned}와 평점합 {@code P_earned = μ · C_earned}는 고정값이다.</li>
 *   <li>학생의 실력(평균)은 현재 누적 평점 {@code μ}, 성적 일관성(편차)은 과목별 평점 표준편차 {@code σ}로 본다.</li>
 *   <li>남은 {@code k}개 과목({@code C_plan}학점)의 평균 평점 {@code G}는 중심극한정리에 따라
 *       정규분포 {@code N(μ, σ²/k)}를 따른다고 가정한다.</li>
 *   <li>졸업 시점 누적 평점 = {@code (P_earned + G · C_plan) / (C_earned + C_plan)}.</li>
 *   <li>목표 누적 평점 {@code T}를 만족하려면 남은 과목 평균이
 *       {@code G ≥ requiredAvg = (T · (C_earned + C_plan) − P_earned) / C_plan} 여야 한다.</li>
 *   <li>달성 확률 = {@code P(G ≥ requiredAvg) = 1 − Φ((requiredAvg − μ) / (σ/√k))}.</li>
 * </ul>
 *
 * <p>참고: 재수강 과목도 남은 학점에 포함해 계산한다(원 학점을 다시 채우는 것으로 근사).
 */
@Component
public class GraduationProbabilityCalculator {

    private static final double MAX_GPA = 4.5;
    /** 성적 이력이 부족해 편차를 신뢰할 수 없을 때 사용하는 기본 표준편차. */
    private static final double DEFAULT_STD = 0.5;
    /** 편차가 너무 작아도 최소한의 불확실성은 둔다. */
    private static final double MIN_STD = 0.1;

    public GraduationProbabilityResponse calculate(GpaStats stats, GraduationProbabilityRequest request) {
        validate(request);

        int courseCount = request.plannedCourseCount();
        int plannedCredits = courseCount * request.creditPerCourseOrDefault();
        double currentGpa = stats.gpa();
        double targetGpa = request.targetGpa();

        double requiredAvg = requiredAverageGpa(stats, targetGpa, plannedCredits);
        int probability = computeProbability(stats, requiredAvg, courseCount);
        String message = buildMessage(currentGpa, targetGpa, plannedCredits, requiredAvg, probability);

        return new GraduationProbabilityResponse(
                probability,
                currentGpa,
                targetGpa,
                round2(Math.max(0.0, requiredAvg)),
                courseCount,
                plannedCredits,
                message
        );
    }

    /** 목표 누적 평점 달성을 위해 남은 과목에서 받아야 하는 평균 평점. */
    private double requiredAverageGpa(GpaStats stats, double targetGpa, int plannedCredits) {
        int earnedCredits = stats.gradedCredits();
        double earnedPointSum = stats.gpa() * earnedCredits;
        return (targetGpa * (earnedCredits + plannedCredits) - earnedPointSum) / plannedCredits;
    }

    private int computeProbability(GpaStats stats, double requiredAvg, int courseCount) {
        // 남은 과목을 전부 A+ 받아도 목표에 못 미치면 0%, 전부 F(0점) 받아도 목표를 넘으면 100%.
        if (requiredAvg > MAX_GPA) {
            return 0;
        }
        if (requiredAvg <= 0.0) {
            return 100;
        }

        double mu = stats.gpa();
        double sigma = effectiveStd(stats);
        double semesterStd = sigma / Math.sqrt(courseCount); // 남은 과목 평균의 표준편차 σ/√k

        if (semesterStd < 1e-9) {
            return requiredAvg <= mu ? 100 : 0;
        }

        double z = (requiredAvg - mu) / semesterStd;
        double probability = 1.0 - normalCdf(z); // P(남은 과목 평균 ≥ requiredAvg)
        return clampPercent(probability);
    }

    /** 이력이 부족하거나 편차가 비정상적으로 작으면 기본값으로 보정한다. */
    private double effectiveStd(GpaStats stats) {
        if (stats.gradedCourseCount() < 3 || stats.stdDeviation() < MIN_STD) {
            return DEFAULT_STD;
        }
        return stats.stdDeviation();
    }

    private void validate(GraduationProbabilityRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 본문은 필수입니다.");
        }
        if (request.nextSemesterMajorCount() < 0
                || request.additionalCultureCount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "과목 수는 0 이상이어야 합니다.");
        }
        if (request.plannedCourseCount() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "다음 학기에 들을 과목이 최소 1개 이상이어야 합니다.");
        }
        if (request.targetGpa() <= 0 || request.targetGpa() > MAX_GPA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "목표 평점은 0 초과 4.5 이하여야 합니다.");
        }
    }

    private String buildMessage(double currentGpa, double targetGpa, int plannedCredits,
                                double requiredAvg, int probability) {
        if (requiredAvg > MAX_GPA) {
            return "현재 누적 평점 %.2f으로는 남은 %d학점을 모두 A+ 받아도 목표 누적 평점 %.1f에 도달할 수 없습니다."
                    .formatted(currentGpa, plannedCredits, targetGpa);
        }
        return "현재 누적 평점 %.2f, 목표 누적 평점 %.1f 달성 가능 확률은 %d%%입니다. (남은 %d학점에서 평균 %.2f 이상 필요)"
                .formatted(currentGpa, targetGpa, probability, plannedCredits, Math.max(0.0, requiredAvg));
    }

    private int clampPercent(double probability) {
        int percent = (int) Math.round(probability * 100.0);
        return Math.max(0, Math.min(100, percent));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** 표준정규분포 누적분포함수 Φ(z). */
    private double normalCdf(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    /** 오차함수 erf(x) 근사 (Abramowitz & Stegun 7.1.26, 오차 < 1.5e-7). */
    private double erf(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t
                - 0.284496736) * t + 0.254829592) * t * Math.exp(-x * x);
        return Math.signum(x) * y;
    }
}
