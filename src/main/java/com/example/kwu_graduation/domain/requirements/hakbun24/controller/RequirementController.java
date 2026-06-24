package com.example.kwu_graduation.domain.requirements.hakbun24.controller;

import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.hakbun24.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun24.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requirements/2024")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping("/check")
    public RequirementCheckResponse check(
            @RequestParam String department,
            @RequestHeader("Klas-Cookie") String klasCookie,
            @RequestBody RequirementCheckRequest request
    ) {
        return requirementService.check(department, klasCookie, request);
    }
}