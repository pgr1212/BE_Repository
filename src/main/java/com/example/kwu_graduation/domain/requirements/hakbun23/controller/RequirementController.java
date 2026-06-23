package com.example.kwu_graduation.domain.requirements.hakbun23.controller;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.hakbun23.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requirements/2020-2023")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @PostMapping("/check")
    public RequirementCheckResponse check(
            @RequestParam(defaultValue = "false") boolean isDoubleMajor,
            @RequestBody CreditSummaryResponse creditSummary
    ) {
        return requirementService.check(isDoubleMajor, creditSummary);
    }
}