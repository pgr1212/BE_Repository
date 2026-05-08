package com.example.kwu_graduation.domain.auth.service;

import com.example.kwu_graduation.domain.auth.client.KlasClient;
import com.example.kwu_graduation.domain.auth.dto.KlasLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KlasAuthService {

    private final KlasClient klasClient;

    public KlasLoginResponse login(String studentId, String password) {
        String cookie = klasClient.login(studentId, password);

        return new KlasLoginResponse(true, cookie);
    }
}