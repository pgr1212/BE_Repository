package com.example.kwu_graduation.domain.requirements.hakbun24.dto;

import java.util.List;

public record MscArea(
        String name,
        int minCredit,
        List<String> courses
) {}