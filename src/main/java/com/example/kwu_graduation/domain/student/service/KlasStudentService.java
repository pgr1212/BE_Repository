package com.example.kwu_graduation.domain.student.service;

import com.example.kwu_graduation.domain.student.client.KlasStudentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KlasStudentService {

    private final KlasStudentClient klasStudentClient;

    public String getStudentInfo(String cookie) {
        return klasStudentClient.getStudentInfo(cookie);
    }
}