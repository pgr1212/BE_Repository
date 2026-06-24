package com.example.kwu_graduation.domain.requirements.hakbun24.spec;

import java.util.List;

/**
 * MSC(수학·기초과학·공학기초) 하위 영역별 최소 이수학점 요건.
 * 소프트웨어학부처럼 영역별 최소학점(수학 6, 기초과학 3 등)이 정해진 경우에만 사용한다.
 * 영역 구분이 없는 학과(컴정공)는 이 목록을 비워 두고 총 학점({@code mscCredit})만으로 판정한다.
 *
 * @param name      영역명(수학·기초과학·공학기초)
 * @param minCredit 해당 영역 최소 이수학점
 * @param courses   해당 영역으로 인정되는 과목 목록
 */
public record MscArea(
        String name,
        int minCredit,
        List<String> courses
) {
    public MscArea {
        courses = courses == null ? List.of() : courses;
    }
}
