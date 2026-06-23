package com.example.kwu_graduation.domain.requirements.common.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementSpecLoaderTest {

    private final RequirementSpecLoader loader = new RequirementSpecLoader();

    @Test
    @DisplayName("2025 정융 요건 JSON이 올바르게 로딩된다")
    void load2025() {
        GraduationRequirement spec = loader.load(2025, Department.JEONGYUNG);

        assertEquals(2025, spec.admissionYear());
        assertEquals("정보융합학부", spec.department());
        assertEquals(133, spec.totalCredit());
        assertEquals(45, spec.majorCredit());
        assertEquals(31, spec.liberalArtsCredit());
        assertTrue(spec.multiMajorRequired());
        assertTrue(spec.requiredLiberalCourses().isEmpty());
    }

    @Test
    @DisplayName("2026 정융 요건 JSON이 올바르게 로딩된다(AI리터러시 필수교양)")
    void load2026() {
        GraduationRequirement spec = loader.load(2026, Department.JEONGYUNG);

        assertEquals(2026, spec.admissionYear());
        assertEquals(33, spec.liberalArtsCredit());
        assertEquals(1, spec.requiredLiberalCourses().size());
        assertTrue(spec.requiredLiberalCourses().contains("AI리터러시"));
    }
}
