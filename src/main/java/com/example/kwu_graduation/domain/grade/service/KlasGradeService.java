package com.example.kwu_graduation.domain.grade.service;

import com.example.kwu_graduation.domain.grade.client.KlasGradeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KlasGradeService {

    private final KlasGradeClient klasGradeClient;

    public String getSemesterGrades(String cookie) {
        return klasGradeClient.getSemesterGrades(cookie);
    }
}