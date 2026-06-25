package com.example.kwu_graduation.domain.simulation.controller;

import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationResponse;
import com.example.kwu_graduation.domain.simulation.service.GraduationSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/klas/simulation")
@RequiredArgsConstructor
public class GraduationSimulationController {

    private final GraduationSimulationService graduationSimulationService;

    /**
     * KLAS 성적 기반 졸업 시뮬레이션 API.
     *
     * 프론트는 성적 JSON, 학기 목록, 과목 목록, 취득 학점 요약을 요청 본문에 넣지 않는다.
     * 백엔드는 Klas-Cookie로 KLAS 성적을 직접 조회한 뒤 졸업 가능 여부와 추천 이수 계획을 계산한다.
     *
     * ## Request
     *
     * ### <Method>
     *
     * ```http
     * POST /api/simulation
     * ```
     *
     * ### <Header>
     *
     * ```json
     * {
     *   "Klas-Cookie": "{KLAS 로그인 후 발급된 쿠키}",
     *   "content-type": "application/json"
     * }
     * ```
     *
     * ### <Request Body>
     *
     * ```json
     * {
     *   "admissionYear": 2025,
     *   "department": "software",
     *   "currentGradeYear": 4,
     *   "currentSemester": 1,
     *   "maxCreditPerSemester": 18,
     *   "includeInProgressCourses": false,
     *   "multiMajorCompleted": null,
     *   "graduationProjectCompleted": null,
     *   "engineeringProgram": null,
     *   "topcitCompleted": null,
     *   "subMajor": null
     * }
     * ```
     *
     * ## Response
     *
     * ### <Header>
     *
     * ```json
     * {
     *   "content-type": "application/json"
     * }
     * ```
     *
     * ### <Response Body>
     *
     * ```json
     * {
     *   "admissionYear": 2025,
     *   "department": "소프트웨어학부",
     *   "graduatableNow": false,
     *   "totalRequired": 133,
     *   "totalEarned": 121,
     *   "summary": {
     *     "totalCredit": 121,
     *     "majorCredit": 57,
     *     "cultureCredit": 31,
     *     "etcCredit": 33
     *   },
     *   "gaps": [
     *     {
     *       "type": "MAJOR_CREDIT",
     *       "name": "전공 학점",
     *       "required": 60,
     *       "completed": 57,
     *       "remaining": 3,
     *       "satisfied": false
     *     }
     *   ],
     *   "steps": [
     *     {
     *       "gradeYear": 4,
     *       "semester": 2,
     *       "recommendedCourses": [
     *         {
     *           "courseCode": null,
     *           "courseName": "전공 선택 과목",
     *           "category": "MAJOR",
     *           "credit": 3
     *         }
     *       ],
     *       "addedCredit": 3,
     *       "remainingGaps": [],
     *       "graduationAvailable": true,
     *       "message": "4학년 2학기: 1과목, 3학점 추가 이수 시 모든 자동 검증 조건 충족 -> 졸업 가능"
     *     }
     *   ],
     *   "warnings": [
     *     "TOPCIT 응시: 사용자 확인 필요"
     *   ],
     *   "requirementItems": [
     *     {
     *       "category": "교양",
     *       "name": "균형교양 영역 충족",
     *       "required": 6,
     *       "earned": 5,
     *       "lack": 1,
     *       "status": "INSUFFICIENT",
     *       "message": "1개 영역 부족"
     *     }
     *   ]
     * }
     * ```
     *
     * ### <Status : `200`>
     *
     * ```json
     * 졸업 시뮬레이션 계산 성공
     * ```
     *
     * ### <Status : `400`>
     *
     * ```json
     * ERROR : 요청 본문 누락, 지원하지 않는 admissionYear/department, 학년/학기 값이 올바르지 않습니다.
     * ```
     *
     * ### <Status : `404`>
     *
     * ```json
     * ERROR : 해당 학번/학과의 졸업요건 정보를 찾을 수 없습니다.
     * ```
     *
     * ### <Status : `502`>
     *
     * ```json
     * ERROR : KLAS 성적 조회 또는 파싱에 실패했습니다.
     * ```
     *
     * ### <Status : `500`>
     *
     * ```json
     * ERROR : 서버 내부 오류가 발생했습니다.
     * ```
     *
     * @param cookie  KLAS 로그인 후 발급된 쿠키
     * @param request 졸업 시뮬레이션 입력값
     * @return 졸업 가능 여부, 학점 요약, 부족 조건, 추천 이수 계획, 수동 확인 항목을 포함한 시뮬레이션 결과
     */
    @PostMapping
    public GraduationSimulationResponse simulate(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestBody GraduationSimulationRequest request
    ) {
        return graduationSimulationService.simulate(cookie, request);
    }
}
