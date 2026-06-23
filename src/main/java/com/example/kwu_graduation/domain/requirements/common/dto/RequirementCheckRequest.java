package com.example.kwu_graduation.domain.requirements.common.dto;

/**
 * 성적(KLAS) 데이터만으로는 자동 판정이 불가능한 항목에 대한 사용자 자기보고 값.
 * 모든 값은 선택(nullable)이며, 값이 없으면 해당 항목은 NEEDS_INPUT 으로 응답한다.
 *
 * @param multiMajorCompleted        다전공(복수/부/심화/연계/마이크로/학생설계융합) 이수(예정) 여부
 * @param graduationProjectCompleted 졸업논문 또는 졸업작품(캡스톤) 완료 여부
 * @param balanceAreasCompleted      (더 이상 사용하지 않음) 균형교양 이수 영역 수는 이제 성적의
 *                                   과목명을 균형교양 영역 카탈로그와 대조하여 자동 판정한다.
 *                                   기존 API 호환을 위해 필드만 유지하며 판정에는 사용하지 않는다.
 */
public record RequirementCheckRequest(
        Boolean multiMajorCompleted,
        Boolean graduationProjectCompleted,
        Integer balanceAreasCompleted
) {
    public static RequirementCheckRequest empty() {
        return new RequirementCheckRequest(null, null, null);
    }
}
