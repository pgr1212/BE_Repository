package com.example.kwu_graduation.domain.auth.dto;

public record KlasLoginRequest(
        String studentId,
        String password
) {
}