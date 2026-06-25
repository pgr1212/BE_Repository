package com.example.kwu_graduation.domain.simulation.hakbun24.service;

import com.example.kwu_graduation.domain.simulation.hakbun24.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.simulation.hakbun24.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.simulation.hakbun24.spec.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 2025·2026학번 졸업요건 조회.
 * 입학 학번만 지정해 공통 점검 흐름({@link GraduationCheckService})을 호출한다.
 */
@Service("hakbun24RequirementService")
@RequiredArgsConstructor
public class RequirementService {

    private final GraduationCheckService graduationCheckService;

    public RequirementCheckResponse check(
            int admissionYear, Department department, String cookie, RequirementCheckRequest input) {
        return graduationCheckService.check(admissionYear, department, cookie, input);
    }
}
