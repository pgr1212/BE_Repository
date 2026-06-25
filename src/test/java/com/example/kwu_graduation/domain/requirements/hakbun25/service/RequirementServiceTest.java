package com.example.kwu_graduation.domain.requirements.hakbun25.service;

import com.example.kwu_graduation.domain.requirements.hakbun25.dto.RequirementResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementServiceTest {

    private final RequirementService service = new RequirementService();

    @Test
    @DisplayName("2025 정융: 교양 31학점, 균형교양 8영역, 공학인증 없음")
    void jeongyung() {
        RequirementResponse r = service.get("jeongyung");

        assertEquals(2025, r.admissionYear());
        assertEquals("정보융합학부", r.department());
        assertEquals(31, r.liberalArtsCredit());
        assertEquals(8, r.balanceAreas().size());
        assertTrue(r.requiredLiberalCourses().isEmpty());
        assertNull(r.engineering());
    }

    @Test
    @DisplayName("2025 컴정공: MSC 27학점 공학인증 요건 포함")
    void computer() {
        RequirementResponse r = service.get("computer");

        assertEquals("컴퓨터정보공학부", r.department());
        assertNotNull(r.engineering());
        assertEquals(27, r.engineering().mscCredit());
        assertTrue(r.engineering().mscAreas().isEmpty()); // 컴정공은 영역 구분 없음
    }

    @Test
    @DisplayName("2025 소프트: TOPCIT 필수 + 세부전공(인공지능 3과목)")
    void software() {
        RequirementResponse r = service.get("software");

        assertEquals("소프트웨어학부", r.department());
        assertTrue(r.topcitRequired());
        assertEquals(18, r.engineering().mscCredit());
        assertEquals(3, r.engineering().mscAreas().size());
        assertTrue(r.subMajors().stream()
                .anyMatch(s -> s.name().equals("인공지능전공") && s.requiredCount() == 3));
    }

    @Test
    @DisplayName("지원하지 않는 학과 코드는 예외")
    void unsupportedDepartment() {
        assertThrows(IllegalArgumentException.class, () -> service.get("unknown"));
    }
}
