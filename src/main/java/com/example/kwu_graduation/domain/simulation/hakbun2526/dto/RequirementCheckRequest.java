package com.example.kwu_graduation.domain.requirements.hakbun2526.dto;

/**
 * 성적(KLAS) 데이터만으로는 자동 판정이 불가능한 항목에 대한 사용자 자기보고 값.
 * 모든 값은 선택(nullable)이며, 값이 없으면 해당 항목은 NEEDS_INPUT 으로 응답한다.
 *
 * @param multiMajorCompleted        다전공(복수/부/심화/연계/마이크로/학생설계융합) 이수(예정) 여부
 * @param graduationProjectCompleted 졸업논문 또는 졸업작품(캡스톤) 완료 여부
 * @param balanceAreasCompleted      (더 이상 사용하지 않음) 균형교양 이수 영역 수는 이제 성적의
 *                                   과목명을 균형교양 영역 카탈로그와 대조하여 자동 판정한다.
 *                                   기존 API 호환을 위해 필드만 유지하며 판정에는 사용하지 않는다.
 * @param engineeringProgram         공학인증 학과(컴정공·소프트) 한정. true=공학프로그램, false=일반프로그램,
 *                                   null=미선택(NEEDS_INPUT). 공학프로그램일 때만 MSC·공필 요건을 판정한다.
 * @param topcitCompleted            TOPCIT 응시 완료 여부(소프트웨어학부). null=미확인(NEEDS_INPUT).
 * @param subMajor                   세부전공(트랙)명. 예) 소프트웨어학부 "소프트웨어전공" / "인공지능전공".
 *                                   null=미선택(NEEDS_INPUT).
 */
public record RequirementCheckRequest(
        Boolean multiMajorCompleted,
        Boolean graduationProjectCompleted,
        Integer balanceAreasCompleted,
        Boolean engineeringProgram,
        Boolean topcitCompleted,
        String subMajor
) {
    public static RequirementCheckRequest empty() {
        return new RequirementCheckRequest(null, null, null, null, null, null);
    }
}
