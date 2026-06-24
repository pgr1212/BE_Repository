package com.example.kwu_graduation.domain.requirements.hakbun24.controller;

import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun24.service.RequirementService;
import com.example.kwu_graduation.domain.requirements.hakbun24.spec.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2024학번 졸업요건 조회 API.
 *
 * GET /api/requirements/hakbun24?department=jeongyung  (2024학번)
 * department: jeongyung(정보융합학부) | computer(컴퓨터정보공학부) | software(소프트웨어학부)
 * 헤더 Klas-Cookie: KLAS 로그인 후 발급된 쿠키
 * 선택 파라미터(자기보고): multiMajorCompleted, graduationProjectCompleted, balanceAreasCompleted,
 *   engineeringProgram(공학인증 학과 한정: true=공학프로그램 / false=일반프로그램),
 *   topcitCompleted(소프트: TOPCIT 응시 여부), subMajor(소프트: 소프트웨어전공 / 인공지능전공)
 */
@RestController("hakbun24RequirementController")
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping("/hakbun24")
    public RequirementCheckResponse checkHakbun24(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestParam(defaultValue = "jeongyung") String department,
            @RequestParam(required = false) Boolean multiMajorCompleted,
            @RequestParam(required = false) Boolean graduationProjectCompleted,
            @RequestParam(required = false) Integer balanceAreasCompleted,
            @RequestParam(required = false) Boolean engineeringProgram,
            @RequestParam(required = false) Boolean topcitCompleted,
            @RequestParam(required = false) String subMajor
    ) {
        RequirementCheckRequest input = new RequirementCheckRequest(
                multiMajorCompleted, graduationProjectCompleted, balanceAreasCompleted,
                engineeringProgram, topcitCompleted, subMajor);
        return requirementService.check(2024, Department.fromCode(department), cookie, input);
    }
}
