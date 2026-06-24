package com.example.kwu_graduation.domain.requirements.hakbun23.controller;

import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2020~2023학번 졸업요건 조회 API. 로그인·성적 없이 학과별 졸업요건 정보를 내려준다.
 * 25/26학번과 동일한 패턴(RequirementResponse 조회 전용)을 따른다.
 *
 * GET /api/klas/requirements/2023/check?department=jeongyung
 * department: jeongyung(정보융합학부) | computer(컴퓨터정보공학부) | software(소프트웨어학부)
 */
@RestController("requirementController2023")
@RequestMapping("/api/klas/requirements/2023")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping("/check")
    public RequirementResponse get(
            @RequestParam(defaultValue = "jeongyung") String department
    ) {
        return requirementService.get(department);
    }
}