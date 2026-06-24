package com.example.kwu_graduation.domain.grade.controller;

import com.example.kwu_graduation.domain.grade.service.KlasGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/klas/grades")
@RequiredArgsConstructor
public class KlasGradeController {

    private final KlasGradeService klasGradeService;

    @GetMapping("/semesters")
    public String getSemesterGrades(
            @RequestHeader("Klas-Cookie") String cookie
    ) {
        return klasGradeService.getSemesterGrades(cookie);
    }

    @GetMapping("/summary")
    public String getGradeSummary(
            @RequestHeader("Klas-Cookie") String cookie
    ) {
        return klasGradeService.getGradeSummary(cookie);
    }
}