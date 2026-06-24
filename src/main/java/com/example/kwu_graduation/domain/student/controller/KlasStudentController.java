package com.example.kwu_graduation.domain.student.controller;

import com.example.kwu_graduation.domain.student.service.KlasStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/klas/student")
@RequiredArgsConstructor
public class KlasStudentController {

    private final KlasStudentService klasStudentService;

    @GetMapping("/info")
    public String getStudentInfo(
            @RequestHeader("Klas-Cookie") String cookie
    ) {
        return klasStudentService.getStudentInfo(cookie);
    }
}