package com.example.kwu_graduation.domain.requirements.hakbun24.engine;

import com.example.kwu_graduation.domain.grade.dto.CreditSummaryResponse;
import com.example.kwu_graduation.domain.grade.dto.KlasSubjectGradeResponse;
import com.example.kwu_graduation.domain.simulation.hakbun24.dto.CheckStatus;
import com.example.kwu_graduation.domain.simulation.hakbun24.dto.RequirementCheckRequest;
import com.example.kwu_graduation.domain.simulation.hakbun24.dto.RequirementCheckResponse;
import com.example.kwu_graduation.domain.simulation.hakbun24.engine.BalanceAreaCatalog;
import com.example.kwu_graduation.domain.simulation.hakbun24.engine.RequirementChecker;
import com.example.kwu_graduation.domain.simulation.hakbun24.spec.Department;
import com.example.kwu_graduation.domain.simulation.hakbun24.spec.GraduationRequirement;
import com.example.kwu_graduation.domain.simulation.hakbun24.spec.RequirementSpecLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 2024학번 모듈이 실제 요건 JSON을 읽어 판정까지 동작하는지 확인하는 스모크 테스트.
 * (KLAS 성적 조회만 제외하고, 스펙 로딩 → 균형교양 자동분류 → 항목 판정 전 과정을 검증한다.)
 */
class RequirementCheckSmokeTest {

    private final RequirementSpecLoader loader = new RequirementSpecLoader();
    private final RequirementChecker checker = new RequirementChecker(new BalanceAreaCatalog());

    /** 취득(완료)한 교양 3학점 과목 */
    private KlasSubjectGradeResponse culture(String name) {
        return new KlasSubjectGradeResponse(name, "교양", 3, "A+", null, null, null,
                "Y", "1", "N", null, 2024, "Y", "Y");
    }

    /** 취득(완료)한 전공/MSC 과목 */
    private KlasSubjectGradeResponse major(String name) {
        return new KlasSubjectGradeResponse(name, "전공", 3, "A+", null, null, null,
                "Y", "1", "N", null, 2024, "Y", "Y");
    }

    private CreditSummaryResponse credits(int total, int major, int culture) {
        return new CreditSummaryResponse(0, 0, 0, 0, total, major, culture, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /** 균형교양 영역에 걸친 10과목 30학점 */
    private List<KlasSubjectGradeResponse> balance30() {
        List<KlasSubjectGradeResponse> list = new ArrayList<>();
        for (String n : List.of("발표와설득전략", "한국문학읽기", "문화비평연습", "C프로그래밍", "생활속의과학",
                "자연과학사", "과학철학의이해", "현대사회와윤리", "법과생활", "생활속의경제")) {
            list.add(culture(n));
        }
        return list;
    }

    private CheckStatus statusOf(RequirementCheckResponse res, String name) {
        return res.items().stream().filter(i -> i.name().equals(name)).findFirst().orElseThrow().status();
    }

    @Test
    @DisplayName("2024 정융 - 총학점·전공 60·균형교양 30학점이 충족으로 판정된다")
    void jeongyung() {
        GraduationRequirement spec = loader.load(2024, Department.JEONGYUNG);
        List<KlasSubjectGradeResponse> subjects = new ArrayList<>(balance30());
        subjects.add(culture("졸업작품"));

        RequirementCheckResponse res = checker.check(spec, credits(133, 60, 31), subjects,
                new RequirementCheckRequest(null, null, null, null, null, null));

        assertEquals(CheckStatus.SATISFIED, statusOf(res, "졸업 총 이수학점"));
        assertEquals(CheckStatus.SATISFIED, statusOf(res, "주전공(필수 포함)"));
        assertEquals(CheckStatus.SATISFIED, statusOf(res, "균형교양 이수학점"));
        assertEquals(CheckStatus.SATISFIED, statusOf(res, "졸업논문/졸업작품 택1"));
    }

    @Test
    @DisplayName("2024 소프트 - TOPCIT·세부전공·공학프로그램 분기가 동작한다")
    void software() {
        GraduationRequirement spec = loader.load(2024, Department.SOFTWARE);
        List<KlasSubjectGradeResponse> subjects = new ArrayList<>(balance30());
        subjects.add(major("인공지능"));
        subjects.add(major("컴퓨터비전"));
        subjects.add(major("기계학습"));
        subjects.add(major("졸업작품"));

        RequirementCheckResponse res = checker.check(spec, credits(133, 60, 31), subjects,
                new RequirementCheckRequest(null, null, null, false, true, "인공지능전공"));

        assertEquals(CheckStatus.SATISFIED, statusOf(res, "TOPCIT 응시"));
        assertEquals(CheckStatus.SATISFIED, statusOf(res, "세부전공필수 - 인공지능전공"));
        assertEquals(CheckStatus.SATISFIED, statusOf(res, "졸업 프로그램")); // 일반프로그램 선택
    }
}
