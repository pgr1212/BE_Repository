package com.example.kwu_graduation.domain.auth.controller;

import com.example.kwu_graduation.domain.auth.dto.KlasLoginRequest;
import com.example.kwu_graduation.domain.auth.dto.KlasLoginResponse;
import com.example.kwu_graduation.domain.auth.service.KlasAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/klas/auth")
@RequiredArgsConstructor
public class KlasAuthController {

    private final KlasAuthService klasAuthService;

    @PostMapping("/login")
    public KlasLoginResponse login(@RequestBody KlasLoginRequest request) {
        return klasAuthService.login(request.studentId(), request.password());
    }
}
