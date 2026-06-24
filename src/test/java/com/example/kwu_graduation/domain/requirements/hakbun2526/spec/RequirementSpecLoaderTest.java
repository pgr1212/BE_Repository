package com.example.kwu_graduation.domain.requirements.hakbun2526.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    @DisplayName("컴퓨터정보공학부 요건 JSON(25·26)이 로딩되고 학과별 추가 요건을 포함한다")
    void loadComputer() {
        GraduationRequirement spec25 = loader.load(2025, Department.COMPUTER);
        assertEquals("컴퓨터정보공학부", spec25.department());
        assertEquals(31, spec25.liberalArtsCredit());
        assertTrue(spec25.requiredLiberalCourses().isEmpty());
        assertFalse(spec25.additionalRequirements().isEmpty());

        // 공학인증 블록(MSC 27학점, 단일 풀, 필수과목)이 파싱된다
        assertNotNull(spec25.engineering());
        assertEquals(27, spec25.engineering().mscCredit());
        assertTrue(spec25.engineering().mscRequiredCourses().contains("C프로그래밍"));
        assertTrue(spec25.engineering().mscAreas().isEmpty()); // 컴정공은 영역 구분 없음
        // 대학물리학2는 PDF상 MSC 필수가 아님(필수는 대학물리학1)
        assertFalse(spec25.engineering().mscRequiredCourses().contains("대학물리학2"));

        GraduationRequirement spec26 = loader.load(2026, Department.COMPUTER);
        assertEquals(33, spec26.liberalArtsCredit());
        assertTrue(spec26.requiredLiberalCourses().contains("AI리터러시"));
    }

    @Test
    @DisplayName("소프트웨어학부 요건 JSON(25·26)이 로딩되고 TOPCIT 등 추가 요건을 포함한다")
    void loadSoftware() {
        GraduationRequirement spec25 = loader.load(2025, Department.SOFTWARE);
        assertEquals("소프트웨어학부", spec25.department());
        assertEquals(133, spec25.totalCredit());
        assertEquals(45, spec25.majorCredit());

        // TOPCIT 필수 + 세부전공(소프트웨어/인공지능전공) 구조가 파싱된다
        assertTrue(spec25.topcitRequired());
        assertEquals(2, spec25.subMajors().size());
        assertTrue(spec25.subMajors().stream().anyMatch(s -> s.name().equals("인공지능전공") && s.requiredCount() == 3));

        // 소프트웨어학부 MSC는 18학점이며 영역별(수학6·기초과학3·공학기초6) 최소학점이 정의된다
        assertNotNull(spec25.engineering());
        assertEquals(18, spec25.engineering().mscCredit());
        assertEquals(3, spec25.engineering().mscAreas().size());

        GraduationRequirement spec26 = loader.load(2026, Department.SOFTWARE);
        assertEquals(33, spec26.liberalArtsCredit());
        assertTrue(spec26.balanceMandatoryAreasAnyOf().contains("인간과 철학"));
    }
}
