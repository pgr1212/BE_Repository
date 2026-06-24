package com.example.kwu_graduation.domain.requirements.hakbun23.spec;

import java.util.List;

/**
 * 공학인증(공학프로그램) 졸업요건 정의.
 * 컴퓨터정보공학부·소프트웨어학부처럼 입학 시 공학프로그램에 배정되는 학과에만 존재하며,
 * 일반프로그램을 선택한 학생에게는 MSC·설계 요건이 면제된다.
 *
 * @param mscCredit             MSC(수학·기초과학·공학기초) 총 최소 이수학점 (컴정공 27, 소프트 18)
 * @param mscRequiredCourses    MSC 필수 과목(모두 이수해야 함)
 * @param mscAreas              MSC 하위 영역별 최소학점 요건(소프트: 수학6·기초과학3·공학기초6). 없으면 총학점만 판정.
 * @param mscPoolCourses        MSC 학점으로 인정되는 전체 과목 풀(총 취득학점 합산 대상, 필수 과목 포함)
 * @param requiredCourses       공학필수(공필) 과목 중 반드시 모두 이수해야 하는 과목
 * @param requiredCoursesOneOf  공학필수(공필) 과목 중 1개 이상 이수하면 되는 과목(예: 산학협력캡스톤설계1/2)
 */
public record EngineeringProgram(
        int mscCredit,
        List<String> mscRequiredCourses,
        List<MscArea> mscAreas,
        List<String> mscPoolCourses,
        List<String> requiredCourses,
        List<String> requiredCoursesOneOf
) {
    public EngineeringProgram {
        mscRequiredCourses = mscRequiredCourses == null ? List.of() : mscRequiredCourses;
        mscAreas = mscAreas == null ? List.of() : mscAreas;
        mscPoolCourses = mscPoolCourses == null ? List.of() : mscPoolCourses;
        requiredCourses = requiredCourses == null ? List.of() : requiredCourses;
        requiredCoursesOneOf = requiredCoursesOneOf == null ? List.of() : requiredCoursesOneOf;
    }
}
