package com.example.kwu_graduation.domain.simulation.controller;

import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationRequest;
import com.example.kwu_graduation.domain.simulation.dto.GraduationSimulationResponse;
import com.example.kwu_graduation.domain.simulation.service.GraduationSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class GraduationSimulationController {

    private final GraduationSimulationService graduationSimulationService;

    @PostMapping
    public GraduationSimulationResponse simulate(
            @RequestHeader("Klas-Cookie") String cookie,
            @RequestBody GraduationSimulationRequest request
    ) {
        return graduationSimulationService.simulate(cookie, request);
    }
}
