package com.example.kwu_graduation.domain.requirements.hakbun25.controller;

import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.common.spec.Department;
import com.example.kwu_graduation.domain.requirements.hakbun25.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2025학번 졸업요건 조회 API.
 *
 * GET /api/requirements/hakbun25?department=jeongyung
 * 헤더 Klas-Cookie: KLAS 로그인 후 발급된 쿠키
 * 선택 파라미터(자기보고): multiMajorCompleted, graduationProjectCompleted, balanceAreasCompleted
 */
@RestController("hakbun25RequirementController")
@RequestMapping("/api/requirements/hakbun25")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping
    public RequirementCheckResponse check(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestParam(defaultValue = "jeongyung") String department,
            @RequestParam(required = false) Boolean multiMajorCompleted,
            @RequestParam(required = false) Boolean graduationProjectCompleted,
            @RequestParam(required = false) Integer balanceAreasCompleted
    ) {
        RequirementCheckRequest input = new RequirementCheckRequest(
                multiMajorCompleted, graduationProjectCompleted, balanceAreasCompleted);
        return requirementService.check(Department.fromCode(department), cookie, input);
    }
}
