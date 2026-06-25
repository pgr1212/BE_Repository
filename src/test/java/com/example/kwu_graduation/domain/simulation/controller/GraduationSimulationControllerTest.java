package com.example.kwu_graduation.domain.simulation.controller;

import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationResponse;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSummaryResponse;
import com.example.kwu_graduation.domain.simulation.service.GraduationSimulationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraduationSimulationControllerTest {

    @Test
    @DisplayName("POST /api/simulation 컨트롤러는 KLAS 쿠키와 요청 본문을 서비스로 위임한다")
    void simulateDelegatesToService() {
        FakeGraduationSimulationService service = new FakeGraduationSimulationService();
        GraduationSimulationController controller = new GraduationSimulationController(service);
        GraduationSimulationRequest request = new GraduationSimulationRequest(
                2025,
                "software",
                4,
                1,
                false,
                null,
                null,
                null,
                null,
                null
        );

        GraduationSimulationResponse response = controller.simulate("cookie-value", request);

        assertEquals("cookie-value", service.cookie);
        assertEquals(request, service.request);
        assertEquals(2025, response.admissionYear());
        assertEquals("소프트웨어학부", response.department());
    }

    private static final class FakeGraduationSimulationService extends GraduationSimulationService {
        private String cookie;
        private GraduationSimulationRequest request;

        private FakeGraduationSimulationService() {
            super(null, null, null, null, null);
        }

        @Override
        public GraduationSimulationResponse simulate(String cookie, GraduationSimulationRequest request) {
            this.cookie = cookie;
            this.request = request;
            return new GraduationSimulationResponse(
                    2025,
                    "소프트웨어학부",
                    false,
                    133,
                    121,
                    new GraduationSummaryResponse(121, 57, 31, 33),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }
    }
}
