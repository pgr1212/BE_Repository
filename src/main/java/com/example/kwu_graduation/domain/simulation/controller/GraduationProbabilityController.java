package com.example.kwu_graduation.domain.simulation.controller;

import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationProbabilityResponse;
import com.example.kwu_graduation.domain.simulation.service.GraduationProbabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 졸업 가능 확률 조회 API.
 *
 * <p>현재 누적 평점(KLAS 성적 기반)과 다음 학기에 들을 학점 수를 비교해,
 * 졸업 시점의 누적 평점이 목표 누적 평점에 도달할 수 있는 확률을 계산한다.
 *
 * <h3>Request</h3>
 * <pre>
 * POST /api/klas/simulation/probability
 * Header: Klas-Cookie: {KLAS 로그인 후 발급된 쿠키}
 * Body:
 * {
 *   "nextSemesterMajorCount": 3,
 *   "additionalCultureCount": 4,
 *   "retakeCourses": ["자료구조", "운영체제"],
 *   "targetGpa": 4.0,
 *   "creditPerCourse": 3
 * }
 * </pre>
 *
 * <h3>Response</h3>
 * <pre>
 * {
 *   "probability": 80,
 *   "currentGpa": 3.85,
 *   "targetGpa": 4.0,
 *   "requiredAverageGpa": 4.2,
 *   "plannedCourseCount": 9,
 *   "plannedCredits": 27,
 *   "message": "현재 누적 평점 3.85, 목표 누적 평점 4.0 달성 가능 확률은 80%입니다. (남은 27학점에서 평균 4.20 이상 필요)"
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/klas/simulation/probability")
@RequiredArgsConstructor
public class GraduationProbabilityController {

    private final GraduationProbabilityService graduationProbabilityService;

    @PostMapping
    public GraduationProbabilityResponse calculate(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestBody GraduationProbabilityRequest request
    ) {
        return graduationProbabilityService.calculate(cookie, request);
    }
}
