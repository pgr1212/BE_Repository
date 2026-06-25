package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.GpaStats;
import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduationProbabilityCalculatorTest {

    private final GraduationProbabilityCalculator calculator = new GraduationProbabilityCalculator();

    @Test
    @DisplayName("목표 누적 평점이 현재 누적 평점과 같으면 확률은 50%다")
    void targetEqualsCurrentGivesFifty() {
        GpaStats stats = new GpaStats(3.5, 0.5, 60, 20);
        GraduationProbabilityResponse response = calculator.calculate(stats, request(3, 2, 3.5));

        assertEquals(50, response.probability());
        assertEquals(3.5, response.currentGpa());
        // 남은 과목 평균이 현재 평점과 같아야 목표 유지 가능
        assertEquals(3.5, response.requiredAverageGpa());
    }

    @Test
    @DisplayName("목표 누적 평점이 현재보다 낮으면 확률은 50%보다 높다")
    void lowerTargetGivesHighProbability() {
        GpaStats stats = new GpaStats(3.5, 0.5, 60, 20);
        GraduationProbabilityResponse response = calculator.calculate(stats, request(3, 2, 3.0));

        assertTrue(response.probability() > 50, "actual=" + response.probability());
    }

    @Test
    @DisplayName("목표 누적 평점이 현재보다 높으면 확률은 50%보다 낮다")
    void higherTargetGivesLowProbability() {
        GpaStats stats = new GpaStats(3.5, 0.5, 60, 20);
        GraduationProbabilityResponse response = calculator.calculate(stats, request(3, 2, 4.0));

        assertTrue(response.probability() < 50, "actual=" + response.probability());
    }

    @Test
    @DisplayName("이미 취득한 학점을 반영해 남은 과목에 필요한 평균 평점을 계산한다")
    void computesRequiredAverageGpaFromEarnedCredits() {
        // 현재 3.0, 30학점 취득. 남은 6학점에서 평균 4.2 받아야 누적 3.2 달성.
        GpaStats stats = new GpaStats(3.0, 0.4, 30, 10);
        GraduationProbabilityResponse response = calculator.calculate(stats, request(2, 0, 3.2));

        assertEquals(4.2, response.requiredAverageGpa());
    }

    @Test
    @DisplayName("남은 과목을 모두 A+ 받아도 목표에 못 미치면 확률은 0%다")
    void impossibleTargetGivesZero() {
        // 현재 3.0, 60학점 취득. 남은 3학점으로 누적 4.0은 불가능.
        GpaStats stats = new GpaStats(3.0, 0.5, 60, 20);
        GraduationProbabilityResponse response = calculator.calculate(stats, request(1, 0, 4.0));

        assertEquals(0, response.probability());
    }

    @Test
    @DisplayName("남은 과목을 모두 F 받아도 목표를 넘으면 확률은 100%다")
    void alreadyGuaranteedGivesHundred() {
        // 현재 4.0, 60학점 취득, 목표 3.0이면 사실상 달성 확정.
        GpaStats stats = new GpaStats(4.0, 0.5, 60, 20);
        GraduationProbabilityResponse response = calculator.calculate(stats, request(1, 0, 3.0));

        assertEquals(100, response.probability());
    }

    @Test
    @DisplayName("재수강 과목과 과목당 학점이 남은 학점에 반영된다")
    void plannedCreditsIncludeRetakes() {
        GpaStats stats = new GpaStats(3.5, 0.5, 60, 20);
        GraduationProbabilityRequest req = new GraduationProbabilityRequest(
                3, 2, List.of("자료구조", "운영체제"), 3.5, 3
        );

        GraduationProbabilityResponse response = calculator.calculate(stats, req);

        assertEquals(7, response.plannedCourseCount()); // 3 + 2 + 2
        assertEquals(21, response.plannedCredits());    // 7 * 3
    }

    @Test
    @DisplayName("목표 평점이 4.5를 초과하면 400 예외")
    void targetAboveMaxThrows() {
        GpaStats stats = new GpaStats(3.5, 0.5, 60, 20);
        assertThrows(ResponseStatusException.class, () -> calculator.calculate(stats, request(3, 2, 4.6)));
    }

    @Test
    @DisplayName("들을 과목이 하나도 없으면 400 예외")
    void noPlannedCourseThrows() {
        GpaStats stats = new GpaStats(3.5, 0.5, 60, 20);
        assertThrows(ResponseStatusException.class, () -> calculator.calculate(stats, request(0, 0, 3.5)));
    }

    private GraduationProbabilityRequest request(int majorCount, int cultureCount, double targetGpa) {
        return new GraduationProbabilityRequest(majorCount, cultureCount, List.of(), targetGpa, 3);
    }
}
