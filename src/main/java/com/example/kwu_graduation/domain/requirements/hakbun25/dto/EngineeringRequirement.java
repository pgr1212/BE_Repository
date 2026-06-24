package com.example.kwu_graduation.domain.requirements.hakbun25.dto;

import java.util.List;

/**
 * 공학인증(공학프로그램) 졸업요건. 공학인증 학과(컴퓨터정보공학부·소프트웨어학부)만 값이 있고,
 * 그 외 학과(정보융합학부)는 null 로 둔다.
 */
public record EngineeringRequirement(
        int mscCredit,                    // MSC 총 최소 이수학점
        List<String> mscRequiredCourses,  // MSC 필수과목
        List<MscArea> mscAreas,           // MSC 영역별 최소학점(영역 구분이 있는 학과만)
        List<String> mscPoolCourses,      // MSC 인정 과목 풀
        List<String> requiredCourses,     // 전공 최소설계 필수과목
        List<String> requiredCoursesOneOf // 위 외에 택1 이수해야 하는 과목
) {}
