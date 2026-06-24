package com.example.kwu_graduation.domain.scholarship.info.controller;

import com.example.kwu_graduation.domain.scholarship.info.dto.ScholarshipResponse;
import com.example.kwu_graduation.domain.scholarship.info.service.ScholarshipInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 장학금 조회 API(조회 전용, 23/24/25/26학번 졸업요건 조회와 동일한 패턴).
 * 로그인·성적 없이 "어떤 장학금이 있고 조건이 뭔지"를 그대로 내려준다.
 *
 * GET /api/klas/scholarships             전체
 * GET /api/klas/scholarships?grade=A     등급별 필터(A/B/C)
 */
@RestController("scholarshipInfoController")
@RequestMapping("/api/klas/scholarships")
@RequiredArgsConstructor
public class ScholarshipInfoController {

    private final ScholarshipInfoService scholarshipInfoService;

    @GetMapping
    public List<ScholarshipResponse> list(
            @RequestParam(required = false) String grade
    ) {
        return scholarshipInfoService.list(grade);
    }
}