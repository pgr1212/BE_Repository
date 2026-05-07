package com.example.kwu_graduation.domain.auth.dto;

public record KlasLoginResponse(
        boolean success,
        String cookie
) {
}
