package com.example.kwu_graduation.domain.scholarship.info.service;

import com.example.kwu_graduation.domain.scholarship.info.dto.ScholarshipResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 장학금 조회. 「장학금요약_버전.pdf」, 「광운대_장학금전체_버전.pdf」,
 * 「광운대학교 교내장학금.pdf」, 「광운대학교 교외장학금.pdf」(2025학년도 기준) 데이터를 그대로 담아 내려준다.
 *
 * <p>※ 장학제도는 매년 변경될 수 있으므로, 실제 서비스 적용 전 KLAS 공지사항 > 등록/장학에서
 * 최신 내용 재확인 필요. 아래 값은 2025학년도 공식 홈페이지 기준.
 *
 * <p>※ "가족장학금"은 원본 자료에 Grade A 표와 Grade B 표 양쪽에 모두 등장하는데,
 * "가족 2인 이상 재학 여부"가 KLAS로 자동 판별이 안 되는 항목이라 B로만 분류함
 * (화도/동해장학금을 A/B 중복에서 B로만 정리했던 것과 동일한 원칙).
 */
@Service
public class ScholarshipInfoService {

    public List<ScholarshipResponse> list(String grade) {
        List<ScholarshipResponse> all = allScholarships();
        if (grade == null || grade.isBlank()) {
            return all;
        }
        return all.stream()
                .filter(s -> s.grade().equalsIgnoreCase(grade))
                .toList();
    }

    private List<ScholarshipResponse> allScholarships() {
        return List.of(
                // ===== Grade A: KLAS 성적만으로 자동 판별 가능 =====
                new ScholarshipResponse(
                        "단과대학 수석장학금", "A",
                        15, 12, 3.0,
                        List.of("학과 자체 기준이 있는 경우 그 기준이 우선 적용됨"),
                        "등록금 전액", "성적우수", "중복지원 불가",
                        "입학연도마다 조건이 다를 수 있음. 소속 학과 기준 반드시 확인"),
                new ScholarshipResponse(
                        "참빛장학금", "A",
                        15, 12, 3.0,
                        List.of("학과 자체 기준이 있는 경우 그 기준이 우선 적용됨"),
                        "등록금 50%", "성적우수", "중복지원 불가",
                        "수석/참빛/비마는 선발기준 동일, 지원금액만 다름"),
                new ScholarshipResponse(
                        "비마장학금", "A",
                        15, 12, 3.0,
                        List.of("학과 자체 기준이 있는 경우 그 기준이 우선 적용됨"),
                        "등록금 25%", "성적우수", "중복지원 불가",
                        "수석/참빛/비마는 선발기준 동일, 지원금액만 다름"),
                new ScholarshipResponse(
                        "국가장학금 I유형/다자녀", "A",
                        12, null, 2.6, // 100점 만점 80점 = 4.5만점 기준 약 2.6
                        List.of(
                                "소득구간(9구간 이하)은 한국장학재단에서 별도 산정 - 실제로는 사용자 입력/연동 필요",
                                "기초/차상위 계층은 완화 기준(70점, 약 1.6/4.5) 적용"
                        ),
                        "소득구간별·자녀수별 차등 지급", "성적+소득", "등록금 범위 내 중복 가능",
                        "F학점 및 이수 후 포기과목도 포함하여 백분위 성적 산출. 한국장학재단 별도 신청 필요"),

                // ===== Grade B: 성적은 자동, 나머지는 사용자 입력 필요 =====
                new ScholarshipResponse(
                        "봉사장학금", "B",
                        15, 12, 2.0,
                        List.of("학생회·교내단체 활동 공로 인정 여부(사용자 입력)"),
                        "해당금액", "대가성", "가능",
                        "5학년(건축학과)은 4학년 기준과 동일 적용. 공로 인정 안 되면 학점만 충족한 상태로 안내"),
                new ScholarshipResponse(
                        "화도장학금", "B",
                        12, null, 2.0,
                        List.of("가정형편 곤란자 해당 여부(사용자 입력)"),
                        "등록금 50%", "가계곤란", "중복지원 정책 별도 확인",
                        "성적우수 장학금이 아니라 가계 곤란 학생 대상"),
                new ScholarshipResponse(
                        "동해장학금", "B",
                        12, null, 2.0,
                        List.of("가정형편 곤란자 해당 여부(사용자 입력)"),
                        "등록금 25%", "가계곤란", "중복지원 정책 별도 확인",
                        "화도장학금과 선발기준 동일, 지원금액만 다름"),
                new ScholarshipResponse(
                        "프론티어장학금", "B",
                        15, 12, 2.5,
                        List.of("학과 봉사활동 인정 여부(학과마다 기준 상이, 사용자 입력)"),
                        "등록금 25%", "기타", "중복지원 정책 별도 확인",
                        "\"학과 봉사활동 인정\" 기준은 소속 학과에 직접 확인 필요"),
                new ScholarshipResponse(
                        "가족장학금", "B",
                        15, null, 2.5,
                        List.of(
                                "직계 가족 2인 이상 재학 여부(사용자 입력)",
                                "신청학생 모두 '학부' 재학생(정규학기 재학, 휴학 제외)이어야 함"
                        ),
                        "2명: 등록금의 1/3, 3명 이상: 등록금 전액", "기타", "등록금 범위 내 중복 가능",
                        "학부-대학원, 학부-전과정 간 신청 불가"),
                new ScholarshipResponse(
                        "DB김준기재단 우수인재", "B",
                        null, null, null,
                        List.of("3학년 재학 여부", "평점 B0 이상", "소득구간 활용 여부 - 전부 수동 확인"),
                        "등록금 전액(졸업시까지)", "성적우수", "중복지원 불가",
                        "재단 자체 기준이 강해 사실상 수동 확인 비중이 높음"),
                new ScholarshipResponse(
                        "DB드림리더", "B",
                        null, null, null,
                        List.of("경영·경제·공과대 재학 여부", "평점 B0 이상", "소득 0~2구간"),
                        "생활비 180만원", "성적+소득", "가능",
                        "전공·소득구간 등 거의 전부 수동 확인 필요"),
                new ScholarshipResponse(
                        "KT그룹 희망나눔 창의혁신리더", "B",
                        null, null, 3.5, // 4.5만점 기준 환산
                        List.of("ICT 관련 전공 여부", "KT 리더십 프로그램 참여 가능 여부"),
                        "등록금 전액", "성적우수", "중복지원 불가",
                        "총평점 3.5/4.5 이상은 자동 체크 가능, 나머지는 수동"),
                new ScholarshipResponse(
                        "빛솔재장학금", "B",
                        null, null, null,
                        List.of("공공기숙사 입주 여부", "지방출신/장애/가계곤란(소득 70% 이하) 여부"),
                        "기숙사비 30%", "생활비", "가능",
                        "성적 조건 없음, 거주·소득 조건이 핵심"),

                // ===== Grade C: 추천/심사/재단 자체 선정 - 앱에서 판별 불가 =====
                new ScholarshipResponse(
                        "광운대 교수상조회", "C",
                        null, null, null,
                        List.of("학과장 추천 필요"),
                        "100만원", "성적우수", "중복지원 불가",
                        "추천 기반 선발 - 학과 사무실 문의 필요"),
                new ScholarshipResponse(
                        "광운대 재직동문회", "C",
                        null, null, null,
                        List.of("학과장 추천 필요"),
                        "100만원", "성적우수", "가능",
                        "추천 기반 선발 - 학과 사무실 문의 필요"),
                new ScholarshipResponse(
                        "광운대 총동문회", "C",
                        null, null, null,
                        List.of("동문회비 납부 여부", "소득 0~4구간 또는 성적우수 또는 학교위상 제고 사유"),
                        "해당금액", "기타", "범위 내 가능",
                        "동문회 자체 심사 기준 적용"),
                new ScholarshipResponse(
                        "국가우수장학생(이공계)", "C",
                        null, null, null,
                        List.of("자연과학·공학계열 3학년 이상", "대학 추천 필요"),
                        "등록금 전액", "성적우수", "중복지원 불가",
                        "대학 추천 기반 - 한국장학재단 선발"),
                new ScholarshipResponse(
                        "해성문화재단", "C",
                        null, null, null,
                        List.of("재단 자체 선정 기준 - 공개된 정량 기준 없음"),
                        "등록금 범위 내", "기타", "범위 내 가능",
                        "재단 홈페이지에서 별도 공고 확인 필요"),
                new ScholarshipResponse(
                        "형남진장학재단", "C",
                        null, null, null,
                        List.of("재단 자체 선정 기준 - 공개된 정량 기준 없음"),
                        "학기당 150만원(2학기)", "기타", "가능",
                        "재단 홈페이지에서 별도 공고 확인 필요"),
                new ScholarshipResponse(
                        "보훈장학금", "C",
                        null, null, null,
                        List.of("본인 또는 부모가 보훈대상자인지 여부"),
                        "전액 또는 일부", "기타", "중복지원 불가",
                        "신분 조건 기반 - 학생복지팀 문의 필요"),
                new ScholarshipResponse(
                        "새터민장학금", "C",
                        null, null, null,
                        List.of("북한이탈주민 본인 또는 자녀 여부"),
                        "등록금 일부", "기타", "중복지원 불가",
                        "신분 조건 기반 - 학생복지팀 문의 필요")
        );
    }
}