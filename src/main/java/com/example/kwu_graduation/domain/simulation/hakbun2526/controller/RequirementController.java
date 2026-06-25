package com.example.kwu_graduation.domain.simulation.controller;

import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun2526.service.RequirementService;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2025·2026학번 졸업요건 점검 API.
 *
 * KLAS 성적을 기준으로 학번/학과별 졸업요건 충족 여부를 점검한다.
 *
 * 지원 학과 코드:
 * - jeongyung: 정보융합학부
 * - computer: 컴퓨터정보공학부
 * - software: 소프트웨어학부
 */
@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    /**
     * 2025학번 졸업요건 점검 API.
     *
     * ## Request
     *
     * ### <Method>
     *
     * ```http
     * GET /api/requirements/hakbun25
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
     * ### <Query Parameter>
     *
     * ```json
     * {
     *   "department": "jeongyung",
     *   "multiMajorCompleted": true,
     *   "graduationProjectCompleted": false,
     *   "balanceAreasCompleted": null,
     *   "engineeringProgram": null,
     *   "topcitCompleted": null,
     *   "subMajor": null
     * }
     * ```
     *
     * ### <Request Body>
     *
     * ```json
     *
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
     *   "department": "정보융합학부",
     *   "graduatable": false,
     *   "totalRequired": 133,
     *   "totalEarned": 121,
     *   "items": [
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
     * 졸업요건 점검 성공
     * ```
     *
     * ### <Status : `400`>
     *
     * ```json
     * ERROR : 지원하지 않는 department 등 요청값이 올바르지 않습니다.
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
     * @param cookie                     KLAS 로그인 후 발급된 쿠키
     * @param department                 학과 코드. jeongyung, computer, software
     * @param multiMajorCompleted        다전공 이수 또는 이수 예정 여부
     * @param graduationProjectCompleted 졸업논문/졸업작품/캡스톤 완료 여부
     * @param balanceAreasCompleted      기존 API 호환용 필드. 현재 판정에는 사용하지 않음
     * @param engineeringProgram         공학인증 학과용 프로그램 선택값
     * @param topcitCompleted            소프트웨어학부 TOPCIT 응시 여부
     * @param subMajor                   소프트웨어학부 세부전공
     * @return 2025학번 졸업요건 항목별 점검 결과
     */
    @GetMapping("/hakbun25")
    public RequirementCheckResponse checkHakbun25(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestParam(defaultValue = "jeongyung") String department,
            @RequestParam(required = false) Boolean multiMajorCompleted,
            @RequestParam(required = false) Boolean graduationProjectCompleted,
            @RequestParam(required = false) Integer balanceAreasCompleted,
            @RequestParam(required = false) Boolean engineeringProgram,
            @RequestParam(required = false) Boolean topcitCompleted,
            @RequestParam(required = false) String subMajor
    ) {
        return check(2025, cookie, department, multiMajorCompleted, graduationProjectCompleted,
                balanceAreasCompleted, engineeringProgram, topcitCompleted, subMajor);
    }

    /**
     * 2026학번 졸업요건 점검 API.
     *
     * ## Request
     *
     * ### <Method>
     *
     * ```http
     * GET /api/requirements/hakbun26
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
     * ### <Query Parameter>
     *
     * ```json
     * {
     *   "department": "jeongyung",
     *   "multiMajorCompleted": true,
     *   "graduationProjectCompleted": false,
     *   "balanceAreasCompleted": null,
     *   "engineeringProgram": null,
     *   "topcitCompleted": null,
     *   "subMajor": null
     * }
     * ```
     *
     * ### <Request Body>
     *
     * ```json
     *
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
     *   "admissionYear": 2026,
     *   "department": "정보융합학부",
     *   "graduatable": false,
     *   "totalRequired": 133,
     *   "totalEarned": 121,
     *   "items": [
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
     * 졸업요건 점검 성공
     * ```
     *
     * ### <Status : `400`>
     *
     * ```json
     * ERROR : 지원하지 않는 department 등 요청값이 올바르지 않습니다.
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
     * @param cookie                     KLAS 로그인 후 발급된 쿠키
     * @param department                 학과 코드. jeongyung, computer, software
     * @param multiMajorCompleted        다전공 이수 또는 이수 예정 여부
     * @param graduationProjectCompleted 졸업논문/졸업작품/캡스톤 완료 여부
     * @param balanceAreasCompleted      기존 API 호환용 필드. 현재 판정에는 사용하지 않음
     * @param engineeringProgram         공학인증 학과용 프로그램 선택값
     * @param topcitCompleted            소프트웨어학부 TOPCIT 응시 여부
     * @param subMajor                   소프트웨어학부 세부전공
     * @return 2026학번 졸업요건 항목별 점검 결과
     */
    @GetMapping("/hakbun26")
    public RequirementCheckResponse checkHakbun26(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestParam(defaultValue = "jeongyung") String department,
            @RequestParam(required = false) Boolean multiMajorCompleted,
            @RequestParam(required = false) Boolean graduationProjectCompleted,
            @RequestParam(required = false) Integer balanceAreasCompleted,
            @RequestParam(required = false) Boolean engineeringProgram,
            @RequestParam(required = false) Boolean topcitCompleted,
            @RequestParam(required = false) String subMajor
    ) {
        return check(2026, cookie, department, multiMajorCompleted, graduationProjectCompleted,
                balanceAreasCompleted, engineeringProgram, topcitCompleted, subMajor);
    }

    private RequirementCheckResponse check(
            int admissionYear, String cookie, String department,
            Boolean multiMajorCompleted, Boolean graduationProjectCompleted, Integer balanceAreasCompleted,
            Boolean engineeringProgram, Boolean topcitCompleted, String subMajor
    ) {
        RequirementCheckRequest input = new RequirementCheckRequest(
                multiMajorCompleted, graduationProjectCompleted, balanceAreasCompleted,
                engineeringProgram, topcitCompleted, subMajor);
        return requirementService.check(admissionYear, Department.fromCode(department), cookie, input);
    }
}
