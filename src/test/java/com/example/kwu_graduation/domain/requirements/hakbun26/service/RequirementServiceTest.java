package com.example.kwu_graduation.domain.requirements.hakbun26.service;

import com.example.kwu_graduation.domain.requirements.hakbun25.dto.RequirementResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementServiceTest {

    private final RequirementService service = new RequirementService();

    @Test
    @DisplayName("2026 정융: 교양 33학점, 균형교양 9영역, AI리터러시 필수교양")
    void jeongyung() {
        RequirementResponse r = service.get("jeongyung");

        assertEquals(2026, r.admissionYear());
        assertEquals(33, r.liberalArtsCredit());
        assertEquals(9, r.balanceAreas().size());
        assertTrue(r.balanceAreas().contains("기초학문융합"));
        assertTrue(r.requiredLiberalCourses().contains("AI리터러시"));
        assertTrue(r.balanceMandatoryAreasAnyOf().contains("인간과 철학"));
    }

    @Test
    @DisplayName("2026 컴정공: 공학인증 요건은 2025와 동일(MSC 27학점)")
    void computer() {
        RequirementResponse r = service.get("computer");

        assertEquals("컴퓨터정보공학부", r.department());
        assertEquals(33, r.liberalArtsCredit());
        assertEquals(27, r.engineering().mscCredit());
    }
}
