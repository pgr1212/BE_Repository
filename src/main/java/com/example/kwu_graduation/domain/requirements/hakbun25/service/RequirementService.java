package com.example.kwu_graduation.domain.requirements.hakbun25.service;

import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.requirements.common.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.requirements.common.service.GraduationCheckService;
import com.example.kwu_graduation.domain.requirements.common.spec.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 2025학번 졸업요건 조회.
 */
@Service("hakbun25RequirementService")
@RequiredArgsConstructor
public class RequirementService {

    private static final int ADMISSION_YEAR = 2025;

    private final GraduationCheckService graduationCheckService;

    public RequirementCheckResponse check(Department department, String cookie, RequirementCheckRequest input) {
        return graduationCheckService.check(ADMISSION_YEAR, department, cookie, input);
    }
}
