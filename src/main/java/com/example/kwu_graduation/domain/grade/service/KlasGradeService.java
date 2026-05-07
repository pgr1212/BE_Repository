package com.example.kwu_graduation.domain.grade.service;

import com.example.kwu_graduation.domain.grade.client.KlasGradeClient;
import com.example.kwu_graduation.domain.grade.dto.KlasCreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSemesterGradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KlasGradeService {

    private final KlasGradeClient klasGradeClient;

    public KlasCreditSummaryResponse getCreditSummary(String cookie) {
        return klasGradeClient.getCreditSummary(cookie);
    }

    public List<KlasSemesterGradeResponse> getSemesterGrades(String cookie) {
        return klasGradeClient.getSemesterGrades(cookie);
    }
}