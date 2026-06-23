package com.example.kwu_graduation.domain.scholarship.controller;

import com.example.kwu_graduation.domain.scholarship.dto.ScholarshipCheckRequest;
import com.example.kwu_graduation.domain.scholarship.dto.ScholarshipCheckResponse;
import com.example.kwu_graduation.domain.scholarship.service.ScholarshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scholarship")
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    @PostMapping("/check")
    public ScholarshipCheckResponse check(@RequestBody ScholarshipCheckRequest request) {
        return scholarshipService.check(request);
    }
}