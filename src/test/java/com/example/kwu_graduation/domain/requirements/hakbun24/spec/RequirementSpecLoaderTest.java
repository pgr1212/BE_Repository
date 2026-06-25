package com.example.kwu_graduation.domain.requirements.hakbun24.spec;

import com.example.kwu_graduation.domain.simulation.hakbun24.spec.Department;
import com.example.kwu_graduation.domain.simulation.hakbun24.spec.GraduationRequirement;
import com.example.kwu_graduation.domain.simulation.hakbun24.spec.RequirementSpecLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementSpecLoaderTest {

    private final RequirementSpecLoader loader = new RequirementSpecLoader();

    @Test
    @DisplayName("2024학번 요건 JSON(3개 학과)이 로딩된다 - 균형 30학점·전공 60·광운인되기 폐지")
    void load2024() {
        GraduationRequirement jy = loader.load(2024, Department.JEONGYUNG);
        assertEquals(2024, jy.admissionYear());
        assertEquals(133, jy.totalCredit());
        assertEquals(60, jy.majorCredit());
        assertEquals(30, jy.balanceCredit());
        assertFalse(jy.multiMajorRequired());
        assertTrue(jy.requiredLiberalCourses().isEmpty()); // 광운인되기 폐지
        assertEquals(8, jy.balanceAreas().size());         // 수리와 자연 포함 8영역

        GraduationRequirement com = loader.load(2024, Department.COMPUTER);
        assertNotNull(com.engineering());
        assertEquals(30, com.engineering().mscCredit());   // 컴정공 24학번 MSC 30학점

        GraduationRequirement sw = loader.load(2024, Department.SOFTWARE);
        assertTrue(sw.topcitRequired());
        assertEquals(2, sw.subMajors().size());
    }
}
