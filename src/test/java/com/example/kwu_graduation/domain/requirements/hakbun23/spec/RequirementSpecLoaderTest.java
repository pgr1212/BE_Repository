package com.example.kwu_graduation.domain.requirements.hakbun23.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementSpecLoaderTest {

    private final RequirementSpecLoader loader = new RequirementSpecLoader();

    @Test
    @DisplayName("2023학번 요건 JSON(3개 학과)이 로딩된다 - 균형 21/18학점·7영역·수리와자연 별도 안내")
    void load2023() {
        GraduationRequirement jy = loader.load(2023, Department.JEONGYUNG);
        assertEquals(2023, jy.admissionYear());
        assertEquals(133, jy.totalCredit());
        assertEquals(60, jy.majorCredit());
        assertEquals(21, jy.balanceCredit());
        assertEquals(7, jy.balanceAreas().size());         // 수리와 자연은 균형교양에서 제외(별도 12학점)
        assertTrue(jy.additionalRequirements().stream().anyMatch(s -> s.contains("수리와자연")));

        GraduationRequirement com = loader.load(2023, Department.COMPUTER);
        assertEquals(18, com.balanceCredit());             // 공학프로그램 기준 균형 18학점
        assertNotNull(com.engineering());
        assertEquals(30, com.engineering().mscCredit());   // 컴정공 23학번 MSC 30학점

        GraduationRequirement sw = loader.load(2023, Department.SOFTWARE);
        assertTrue(sw.topcitRequired());
        assertEquals(18, sw.balanceCredit());
        assertEquals(2, sw.subMajors().size());
    }
}
