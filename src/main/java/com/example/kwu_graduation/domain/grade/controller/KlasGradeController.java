package com.example.kwu_graduation.domain.grade.controller;

import com.example.kwu_graduation.domain.grade.dto.KlasCreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import com.example.kwu_graduation.domain.grade.service.KlasGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/klas/grades")
@RequiredArgsConstructor
public class KlasGradeController {

    private final KlasGradeService klasGradeService;

    @GetMapping("/summary")
    public KlasCreditSummaryResponse getCreditSummary(
            @RequestHeader("Klas-Cookie") String cookie
    ) {
        return klasGradeService.getCreditSummary(cookie);
    }

    @GetMapping("/semesters")
    public List<KlasSemesterGradeResponse> getSemesterGrades(
            @RequestHeader("Klas-Cookie") String cookie
    ) {
        return klasGradeService.getSemesterGrades(cookie);
    }
}
