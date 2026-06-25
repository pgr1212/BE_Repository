package com.example.kwu_graduation.domain.simulation.hakbun2526.dto;

/**
 * 졸업요건 점검 결과의 한 항목(예: 총학점, 주전공, 균형교양 ...).
 *
 * @param category 항목 분류(총이수학점, 전공, 교양, 다전공, 졸업작품 ...)
 * @param name     항목명(화면 표시용)
 * @param required 필요 학점/개수 (학점·개수가 의미 없는 항목은 0)
 * @param earned   취득 학점/개수
 * @param lack     부족분 = max(0, required - earned)
 * @param status   충족 상태
 * @param message  부가 설명(부족 사유, 사용자 확인 안내 등)
 */
public record CheckItem(
        String category,
        String name,
        int required,
        int earned,
        int lack,
        CheckStatus status,
        String message
) {

    /** 학점 충족 여부 항목(required/earned 비교)을 만든다. */
    public static CheckItem ofCredit(String category, String name, int required, int earned) {
        int lack = Math.max(0, required - earned);
        CheckStatus status = lack == 0 ? CheckStatus.SATISFIED : CheckStatus.INSUFFICIENT;
        String message = lack == 0
                ? "충족"
                : "%d학점 부족".formatted(lack);
        return new CheckItem(category, name, required, earned, lack, status, message);
    }

    /** 학점이 아닌 단순 통과/실패/확인필요 항목을 만든다. */
    public static CheckItem ofStatus(String category, String name, CheckStatus status, String message) {
        return new CheckItem(category, name, 0, 0, 0, status, message);
    }
}
