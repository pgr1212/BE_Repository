package com.example.kwu_graduation.domain.simulation.service;

import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckItem;
import com.example.kwu_graduation.domain.requirements.hakbun2526.dto.CheckStatus;
import com.example.kwu_graduation.domain.simulation.dto.RequirementGapResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequirementGapCalculatorTest {

    private final RequirementGapCalculator calculator = new RequirementGapCalculator(null);

    @Test
    @DisplayName("총학점만 부족하면 TOTAL_CREDIT gap만 만든다")
    void convertsOnlyTotalCreditShortage() {
        List<CheckItem> items = List.of(
                CheckItem.ofCredit("총이수학점", "졸업 총 이수학점", 133, 121),
                CheckItem.ofCredit("전공", "주전공(필수 포함)", 60, 60)
        );

        List<RequirementGapResponse> gaps = calculator.calculate(items);

        assertEquals(1, gaps.size());
        assertGap(gaps.getFirst(), "TOTAL_CREDIT", "졸업 총 이수학점", 12);
    }

    @Test
    @DisplayName("전공 학점만 부족하면 MAJOR_CREDIT gap만 만든다")
    void convertsOnlyMajorCreditShortage() {
        List<CheckItem> items = List.of(
                CheckItem.ofCredit("총이수학점", "졸업 총 이수학점", 133, 133),
                CheckItem.ofCredit("전공", "주전공(필수 포함)", 60, 57)
        );

        List<RequirementGapResponse> gaps = calculator.calculate(items);

        assertEquals(1, gaps.size());
        assertGap(gaps.getFirst(), "MAJOR_CREDIT", "주전공(필수 포함)", 3);
    }

    @Test
    @DisplayName("총학점과 전공 학점이 동시에 부족하면 두 gap을 모두 만든다")
    void convertsTotalAndMajorCreditShortageTogether() {
        List<CheckItem> items = List.of(
                CheckItem.ofCredit("총이수학점", "졸업 총 이수학점", 133, 121),
                CheckItem.ofCredit("전공", "주전공(필수 포함)", 60, 57)
        );

        List<RequirementGapResponse> gaps = calculator.calculate(items);

        assertEquals(2, gaps.size());
        assertGap(gaps.get(0), "TOTAL_CREDIT", "졸업 총 이수학점", 12);
        assertGap(gaps.get(1), "MAJOR_CREDIT", "주전공(필수 포함)", 3);
    }

    @Test
    @DisplayName("기존 졸업요건 점검 결과 중 자동 추천 가능한 부족분만 gap으로 변환한다")
    void convertsInsufficientCheckItemsToGaps() {
        List<CheckItem> items = List.of(
                CheckItem.ofCredit("총이수학점", "졸업 총 이수학점", 133, 121),
                CheckItem.ofCredit("전공", "주전공(필수 포함)", 60, 57),
                CheckItem.ofCredit("교양", "교양 총 이수학점", 30, 30),
                CheckItem.ofCredit("교양", "균형교양 이수학점", 18, 15)
        );

        List<RequirementGapResponse> gaps = calculator.calculate(items);

        assertEquals(3, gaps.size());
        assertGap(gaps.get(0), "TOTAL_CREDIT", "졸업 총 이수학점", 12);
        assertGap(gaps.get(1), "MAJOR_CREDIT", "주전공(필수 포함)", 3);
        assertGap(gaps.get(2), "REQUIRED_AREA", "균형교양 이수학점", 3);
    }

    @Test
    @DisplayName("필수교양 미이수 상태 항목은 기본 3학점 REQUIRED_COURSE gap으로 변환한다")
    void convertsRequiredLiberalCourseToRequiredCourseGap() {
        List<CheckItem> items = List.of(
                CheckItem.ofStatus("필수교양", "필수교양 - AI리터러시", CheckStatus.INSUFFICIENT, "미이수")
        );

        List<RequirementGapResponse> gaps = calculator.calculate(items);

        assertEquals(1, gaps.size());
        assertGap(gaps.getFirst(), "REQUIRED_COURSE", "필수교양 - AI리터러시", 3);
    }

    @Test
    @DisplayName("충족 항목과 사용자 확인 필요 항목은 gap 추천 대상에서 제외한다")
    void excludesSatisfiedAndNeedsInputItems() {
        List<CheckItem> items = List.of(
                CheckItem.ofCredit("총이수학점", "졸업 총 이수학점", 133, 133),
                CheckItem.ofStatus("다전공", "다전공 택1", CheckStatus.NEEDS_INPUT, "확인 필요"),
                new CheckItem("교양", "균형교양 영역 충족", 4, 3, 1, CheckStatus.INSUFFICIENT, "1개 영역 부족"),
                CheckItem.ofStatus("TOPCIT", "TOPCIT 응시", CheckStatus.INSUFFICIENT, "미응시")
        );

        List<RequirementGapResponse> gaps = calculator.calculate(items);

        assertEquals(0, gaps.size());
    }

    private void assertGap(RequirementGapResponse gap, String type, String name, int remaining) {
        assertEquals(type, gap.type());
        assertEquals(name, gap.name());
        assertEquals(remaining, gap.remaining());
        assertEquals(false, gap.satisfied());
    }
}
