package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.GpaStats;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.service.GpaCalculator;
import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 졸업 가능 확률 계산 흐름을 조립한다.
 *
 * <p>KLAS 쿠키로 성적을 조회 → 현재 평점/편차 계산 → 목표 평점 달성 확률 계산.
 */
@Service
@RequiredArgsConstructor
public class GraduationProbabilityService {

    private final SimulationKlasGradeReader klasGradeReader;
    private final GpaCalculator gpaCalculator;
    private final GraduationProbabilityCalculator probabilityCalculator;

    public GraduationProbabilityResponse calculate(String cookie, GraduationProbabilityRequest request) {
        List<KlasSemesterGradeResponse> semesters = klasGradeReader.readSemesters(cookie);
        GpaStats stats = gpaCalculator.calculate(semesters);
        return probabilityCalculator.calculate(stats, request);
    }
}
