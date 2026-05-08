package com.example.kwu_graduation.domain.grade.controller;

import com.example.kwu_graduation.domain.auth.dto.KlasLoginRequest;
import com.example.kwu_graduation.domain.auth.service.KlasAuthService;
import com.example.kwu_graduation.domain.grade.service.KlasGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/klas/test")
@RequiredArgsConstructor
public class KlasGradeTestController {

    private final KlasAuthService klasAuthService;
    private final KlasGradeService klasGradeService;

    @PostMapping("/semesters")
    public String getSemesterGradesAfterLogin(
            @RequestBody KlasLoginRequest request
    ) {
        String cookie = klasAuthService.login(request.studentId(), request.password()).cookie();

        System.out.println("Test Final Cookie = " + cookie);

        return klasGradeService.getSemesterGrades(cookie);
    }
}