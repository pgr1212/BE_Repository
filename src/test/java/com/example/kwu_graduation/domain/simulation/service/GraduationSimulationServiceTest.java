package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.grade.service.GradeCalculator;
import com.example.kwu_graduation.domain.requirements.hakbun2526.engine.BalanceAreaCatalog;
import com.example.kwu_graduation.domain.requirements.hakbun2526.engine.RequirementChecker;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.Department;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.GraduationRequirement;
import com.example.kwu_graduation.domain.requirements.hakbun2526.spec.RequirementSpecLoader;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduationSimulationServiceTest {

    @Test
    @DisplayName("KLAS 성적, 학점 계산, 졸업요건 점검, gap 변환을 조립해 응답을 만든다")
    void simulateBuildsResponse() {
        GraduationSimulationService service = serviceWith(sampleSemesters(), sampleRequirement());

        GraduationSimulationResponse response = service.simulate("cookie", new GraduationSimulationRequest(
                2025,
                "software",
                4,
                1,
                false,
                null,
                true,
                null,
                null,
                null
        ));

        assertEquals(2025, response.admissionYear());
        assertEquals("소프트웨어학부", response.department());
        assertEquals(12, response.totalRequired());
        assertEquals(9, response.totalEarned());
        assertEquals(9, response.summary().totalCredit());
        assertEquals(3, response.summary().majorCredit());
        assertEquals(2, response.gaps().size());
        assertTrue(response.gaps().stream().anyMatch(gap -> "TOTAL_CREDIT".equals(gap.type()) && gap.remaining() == 3));
        assertTrue(response.gaps().stream().anyMatch(gap -> "MAJOR_CREDIT".equals(gap.type()) && gap.remaining() == 3));
        assertTrue(response.warnings().isEmpty());
        assertTrue(response.requirementItems().stream().anyMatch(item -> "졸업 총 이수학점".equals(item.name())));
    }

    @Test
    @DisplayName("지원하지 않는 입학연도는 400으로 거절한다")
    void invalidAdmissionYearThrowsBadRequest() {
        GraduationSimulationService service = serviceWith(List.of(), sampleRequirement());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.simulate("cookie", new GraduationSimulationRequest(
                        2024,
                        "software",
                        4,
                        1,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("지원하지 않는 학기값은 400으로 거절한다")
    void invalidCurrentSemesterThrowsBadRequest() {
        GraduationSimulationService service = serviceWith(List.of(), sampleRequirement());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.simulate("cookie", new GraduationSimulationRequest(
                        2025,
                        "software",
                        4,
                        3,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private GraduationSimulationService serviceWith(
            List<KlasSemesterGradeResponse> semesters,
            GraduationRequirement requirement
    ) {
        GraduationCreditCalculator creditCalculator = new GraduationCreditCalculator(new GradeCalculator());
        return new GraduationSimulationService(
                new FakeSimulationKlasGradeReader(semesters),
                creditCalculator,
                new FakeRequirementSpecLoader(requirement),
                new RequirementChecker(new BalanceAreaCatalog()),
                new RequirementGapCalculator(creditCalculator)
        );
    }

    private GraduationRequirement sampleRequirement() {
        return new GraduationRequirement(
                2025,
                "인공지능융합대학",
                "소프트웨어학부",
                12,
                6,
                3,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                List.of("졸업작품"),
                null,
                false,
                List.of(),
                List.of()
        );
    }

    private List<KlasSemesterGradeResponse> sampleSemesters() {
        return List.of(new KlasSemesterGradeResponse(
                "2026",
                "1",
                "1",
                List.of(
                        completedSubject("전공완료", "전공", 3),
                        completedSubject("교양완료", "교양", 3),
                        completedSubject("기타완료", "기타", 3)
                )
        ));
    }

    private KlasSubjectGradeResponse completedSubject(String name, String codeName, int credit) {
        return new KlasSubjectGradeResponse(
                name,
                codeName,
                credit,
                "A0",
                null,
                null,
                null,
                "Y",
                "1",
                "N",
                null,
                2026,
                null,
                "Y"
        );
    }

    private static final class FakeSimulationKlasGradeReader extends SimulationKlasGradeReader {
        private final List<KlasSemesterGradeResponse> semesters;

        private FakeSimulationKlasGradeReader(List<KlasSemesterGradeResponse> semesters) {
            super(null, null);
            this.semesters = semesters;
        }

        @Override
        public List<KlasSemesterGradeResponse> readSemesters(String cookie) {
            return semesters;
        }
    }

    private static final class FakeRequirementSpecLoader extends RequirementSpecLoader {
        private final GraduationRequirement requirement;

        private FakeRequirementSpecLoader(GraduationRequirement requirement) {
            this.requirement = requirement;
        }

        @Override
        public GraduationRequirement load(int admissionYear, Department department) {
            return requirement;
        }
    }
}
