# 졸업 시뮬레이션 API 구현 스펙

## 1. 목표

`/SimulationMain` 화면에서 사용할 졸업 시뮬레이션 API를 구현한다.

`/SimulationMain`에는 성적 입력 또는 성적 조회 UI가 없다. 따라서 프론트는 성적 JSON, 과목 목록, 학점 목록을 직접 보내지 않는다.

사용자가 학번, 학과, 현재 학년/학기, 일부 자기보고 값을 입력하면 백엔드는 KLAS 쿠키로 성적을 직접 조회한 뒤 다음 정보를 계산해서 반환한다.

- 현재 졸업 가능 여부
- 현재 취득 학점 요약
- 졸업요건별 충족/부족 상태
- 성적 데이터만으로 판단할 수 없어 사용자가 직접 확인해야 하는 항목

## 2. 구현 기준

현재 레포지토리에 있는 25/26학번 졸업요건 정보를 기준으로 구현한다.

```text
src/main/java/com/example/kwu_graduation/domain/simulation/hakbun2526
src/main/resources/requirements/hakbun2526
```

시뮬레이션 API에서 졸업요건 규칙을 새로 만들지 않는다. 기존 졸업요건 점검 로직을 재사용한다.

재사용 대상:

- `RequirementSpecLoader`: 학번/학과별 졸업요건 JSON 로드
- `RequirementChecker`: 졸업요건 항목별 충족 여부 판정
- `BalanceAreaCatalog`: 균형교양 과목명과 영역 매핑
- `GradeCalculator`: KLAS 성적 기준 학점 집계
- `KlasGradeService`: KLAS 성적 조회
- `KlasGradeParser`: KLAS 성적 JSON 문자열 파싱

## 3. API

### 3.1 KLAS 성적 기반 시뮬레이션

```http
POST /api/simulation
Klas-Cookie: {KLAS 로그인 쿠키}
Content-Type: application/json
```

백엔드는 `Klas-Cookie`를 사용해 KLAS 성적을 조회한다.

프론트 요청 본문에는 성적 관련 필드를 넣지 않는다.

요청 본문에 포함하지 않는 것:

- KLAS 원본 성적 JSON
- `semesters`
- 과목 목록
- 취득 학점 요약
- 성적 등급 목록

### 3.2 요청 예시

```json
{
  "admissionYear": 2025,
  "department": "software",
  "currentGradeYear": 4,
  "currentSemester": 1,
  "includeInProgressCourses": false,
  "multiMajorCompleted": null,
  "graduationProjectCompleted": null,
  "engineeringProgram": null,
  "topcitCompleted": null,
  "subMajor": null
}
```

### 3.3 요청 필드

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `admissionYear` | number | Y | 입학 학번. 초기 지원 범위는 `2025`, `2026` |
| `department` | string | Y | 학과 코드. `jeongyung`, `computer`, `software` |
| `currentGradeYear` | number | Y | 현재 학년. 1 이상 |
| `currentSemester` | number | Y | 현재 학기. `1` 또는 `2` |
| `includeInProgressCourses` | boolean | N | 현재 수강 중 과목을 이수 예정으로 포함할지. 기본값 `false` |
| `multiMajorCompleted` | boolean/null | N | 다전공 이수 또는 이수 예정 여부 |
| `graduationProjectCompleted` | boolean/null | N | 졸업논문/졸업작품/캡스톤 완료 여부 |
| `engineeringProgram` | boolean/null | N | 공학인증 학과용. `true` 공학프로그램, `false` 일반프로그램 |
| `topcitCompleted` | boolean/null | N | 소프트웨어학부 TOPCIT 응시 여부 |
| `subMajor` | string/null | N | 세부전공. 예: `소프트웨어전공`, `인공지능전공` |

## 3.4 프론트와 백엔드 역할 분리

`/SimulationMain` 기준 역할은 다음처럼 나눈다.

프론트가 하는 일:

- 학번 선택: 2025 또는 2026
- 학과 선택: 정보융합학부, 컴퓨터정보공학부, 소프트웨어학부
- 현재 학년/학기 입력
- 다전공, 졸업작품, 공학프로그램, TOPCIT, 세부전공 같은 자기보고 값 입력
- 시뮬레이션 API 호출
- 응답 결과 표시

백엔드가 하는 일:

- `Klas-Cookie`로 KLAS 성적 조회
- 성적 JSON 파싱
- 현재 취득 학점 계산
- 졸업요건 JSON 로드
- 졸업요건 충족 여부 계산
- 부족 조건 계산

즉, 성적 관련 구현은 `/SimulationMain` 화면이 아니라 백엔드 API 내부 구현 범위다.

## 4. 응답

### 4.1 응답 예시

```json
{
  "admissionYear": 2025,
  "department": "소프트웨어학부",
  "graduatableNow": false,
  "totalRequired": 133,
  "totalEarned": 121,
  "summary": {
    "totalCredit": 121,
    "majorCredit": 57,
    "cultureCredit": 31,
    "etcCredit": 33
  },
  "gaps": [
    {
      "type": "MAJOR_CREDIT",
      "name": "전공 학점",
      "required": 60,
      "completed": 57,
      "remaining": 3,
      "satisfied": false
    }
  ],
  "warnings": [
    "성적 데이터만으로 판단할 수 없는 항목은 직접 확인해야 합니다."
  ],
  "requirementItems": [
    {
      "category": "교양",
      "name": "균형교양 영역 충족",
      "required": 6,
      "earned": 5,
      "lack": 1,
      "status": "INSUFFICIENT",
      "message": "1개 영역 부족"
    }
  ]
}
```

### 4.2 응답 필드

| 필드 | 설명 |
| --- | --- |
| `admissionYear` | 입학 학번 |
| `department` | 학과 표시명 |
| `graduatableNow` | 현재 기준 모든 요건 충족 여부 |
| `totalRequired` | 졸업 총 필요 학점 |
| `totalEarned` | 현재 취득 총 학점 |
| `summary` | 총/전공/교양/기타 학점 요약 |
| `gaps` | 자동 계산 가능한 부족 조건 목록 |
| `warnings` | 수동 확인 필요 항목 등 안내 메시지 |
| `requirementItems` | 기존 졸업요건 점검 결과 원본에 가까운 상세 항목 |

## 5. 처리 흐름

`GraduationSimulationService`는 아래 순서로 동작한다.

```text
1. 요청 본문 검증
2. Klas-Cookie 헤더 검증
3. KlasGradeService로 KLAS 성적 JSON 조회
4. KlasGradeParser로 List<KlasSemesterGradeResponse> 변환
5. GradeCalculator 또는 GraduationCreditCalculator로 현재 학점 요약 계산
6. RequirementSpecLoader로 학번/학과별 졸업요건 로드
7. RequirementChecker로 졸업요건 항목별 충족 여부 계산
8. CheckItem 목록을 RequirementGapResponse 목록으로 변환
9. 수동 확인 필요 항목을 warnings에 반영
10. GraduationSimulationResponse 반환
```

## 6. Gap 변환 규칙

`RequirementChecker`는 `CheckItem` 목록을 반환한다. 시뮬레이션에서는 이 목록을 `RequirementGapResponse`로 변환한다.

기본 규칙:

```text
SATISFIED -> gap 생성 안 함
INSUFFICIENT -> 학점/과목 부족분을 gap으로 생성
NEEDS_INPUT -> gap 생성 없이 warnings 또는 requirementItems에 표시
```

타입 매핑:

| CheckItem 기준 | Gap type | gap 생성 |
| --- | --- | --- |
| `졸업 총 이수학점` | `TOTAL_CREDIT` | Y |
| `주전공(필수 포함)` | `MAJOR_CREDIT` | Y |
| `교양 총 이수학점` | `CULTURE_CREDIT` | Y |
| `균형교양 이수학점` | `REQUIRED_AREA` 또는 `BALANCE_CREDIT` | Y |
| `필수교양 - ...` | `REQUIRED_COURSE` | Y |
| `균형교양 영역 충족` | `MANUAL_CHECK` 또는 `BALANCE_AREA` | N |
| `다전공 택1` | `MANUAL_CHECK` | N |
| `졸업논문/졸업작품 택1` | `MANUAL_CHECK` | N |
| `졸업 프로그램 선택` | `MANUAL_CHECK` | N |
| `TOPCIT 응시` | `MANUAL_CHECK` | N |
| 학과 추가 요건 | `MANUAL_CHECK` | N |

초기 버전에서는 학점으로 환산 가능한 부족분을 `gaps`에 넣는다. 수동 확인 항목은 `gaps`에 넣지 않고 `warnings` 또는 `requirementItems`에서 따로 보여준다.

## 7. 검증 규칙

요청 검증:

- `Klas-Cookie` 헤더는 필수
- `admissionYear`는 `2025` 또는 `2026`
- `department`는 `jeongyung`, `computer`, `software`
- `currentGradeYear`는 1 이상
- `currentSemester`는 `1` 또는 `2`
- `includeInProgressCourses`가 없으면 `false`

오류 응답 기준:

- 요청값 오류: `400 BAD_REQUEST`
- KLAS 성적 조회 또는 파싱 오류: `502 BAD_GATEWAY`
- 졸업요건 JSON 없음: `404 NOT_FOUND` 또는 `400 BAD_REQUEST`
- 서버 내부 로딩 오류: `500 INTERNAL_SERVER_ERROR`

## 8. 구현 대상 파일

새로 만들 파일:

```text
src/main/java/com/example/kwu_graduation/domain/simulation/controller/GraduationSimulationController.java
src/main/java/com/example/kwu_graduation/domain/simulation/service/GraduationSimulationService.java
http/simulation-test.http
```

구현 또는 수정할 파일:

```text
src/main/java/com/example/kwu_graduation/domain/simulation/dto/GraduationSimulationRequest.java
src/main/java/com/example/kwu_graduation/domain/simulation/dto/GraduationSimulationResponse.java
src/main/java/com/example/kwu_graduation/domain/simulation/dto/GraduationSummaryResponse.java
src/main/java/com/example/kwu_graduation/domain/simulation/dto/RequirementGapResponse.java
```

기존 파일 재사용:

```text
src/main/java/com/example/kwu_graduation/domain/simulation/service/GraduationCreditCalculator.java
src/main/java/com/example/kwu_graduation/domain/simulation/service/RequirementGapCalculator.java
src/main/java/com/example/kwu_graduation/domain/simulation/parser/KlasGradeParser.java
```

## 9. 구현 전 확인 필요 사항

현재 pull된 코드에서 다음 정합성을 먼저 확인해야 한다.

```text
파일 경로:
src/main/java/com/example/kwu_graduation/domain/simulation/hakbun2526/...

package 선언:
com.example.kwu_graduation.domain.requirements.hakbun2526...
```

파일 경로는 `domain/simulation/hakbun2526`인데 package는 `domain.requirements.hakbun2526`로 선언되어 있다. Java 컴파일 자체는 package 선언 기준으로 동작하지만, 레포 구조와 import 가독성이 어긋난다.

시뮬레이션 API를 구현하기 전에 둘 중 하나로 정리하는 것이 좋다.

권장안:

```text
src/main/java/com/example/kwu_graduation/domain/requirements/hakbun2526/...
```

즉, 현재 package 선언에 맞춰 파일 경로를 옮긴다.

## 10. 구현 작업 순서

아래 순서대로 작업한다. 목표는 한 번에 전체를 만들지 않고, “컴파일 가능한 작은 단위”로 기능을 쌓는 것이다.

### 10.1 1단계: 현재 코드 정합성 확인

먼저 기존 졸업요건 코드와 현재 추가된 시뮬레이션 코드의 위치를 확인한다.

확인 대상:

```text
src/main/java/com/example/kwu_graduation/domain/simulation
src/main/java/com/example/kwu_graduation/domain/requirements
src/main/resources/simulation_hakbun
src/main/resources/requirements
```

확인해야 하는 것:

- 실제 파일 경로와 `package` 선언이 맞는지
- 이미 만들어진 DTO, model, parser, service가 있는지
- 졸업요건 JSON 파일명이 서비스에서 찾는 이름과 맞는지
- 기존 `RequirementSpecLoader`, `RequirementChecker`를 import 가능한지

이 단계가 필요한 이유:

현재 레포에는 이미 작성된 파일과 새로 만들 파일이 섞여 있을 수 있다. 구조를 먼저 확인하지 않으면 같은 역할의 클래스를 중복으로 만들거나, package mismatch 때문에 컴파일이 깨질 수 있다.

완료 기준:

- 사용할 패키지명을 `com.example.kwu_graduation.domain.simulation`으로 확정한다.
- 졸업요건 점검 로직은 새로 만들지 않고 기존 로직을 재사용하기로 확정한다.

### 10.2 2단계: 요청/응답 DTO 완성

먼저 API 입출력 모양을 고정한다.

대상 파일:

```text
GraduationSimulationRequest.java
GraduationSimulationResponse.java
GraduationSummaryResponse.java
RequirementGapResponse.java
```

이 단계에서 해야 할 일:

- `GraduationSimulationRequest`에 3.3 요청 필드가 모두 있는지 확인
- 프론트가 성적 데이터, 과목 목록, `semesters`를 보내지 않도록 요청 DTO에서 제외
- `GraduationSimulationResponse`가 4.2 응답 필드를 담을 수 있는지 확인
- `requirementItems`처럼 기존 졸업요건 점검 결과를 담을 필드 타입 결정

이 단계가 필요한 이유:

DTO는 프론트와 백엔드의 계약이다. DTO가 흔들리면 컨트롤러, 서비스, 프론트 연동이 모두 흔들린다. 계산 로직을 만들기 전에 요청/응답 구조를 먼저 고정해야 한다.

완료 기준:

- DTO만으로 요청 예시와 응답 예시를 표현할 수 있다.
- 사용자가 직접 성적/예정 과목을 넣는 필드는 없다.

### 10.3 3단계: 기존 졸업요건 점검 로직 재사용 가능하게 정리

시뮬레이션 API는 졸업요건 규칙을 새로 만들지 않는다. 기존 점검 로직을 호출해서 결과를 받아야 한다.

확인 대상:

```text
RequirementSpecLoader
RequirementChecker
BalanceAreaCatalog
```

이 단계에서 해야 할 일:

- 학번 `2025`, `2026`과 학과 `jeongyung`, `computer`, `software`가 기존 로더에서 조회되는지 확인
- 로더가 읽는 JSON 경로와 실제 리소스 경로가 일치하는지 확인
- `RequirementChecker`가 반환하는 결과 타입과 필드명을 확인
- `NEEDS_INPUT` 항목을 어떻게 응답에 담을지 결정

이 단계가 필요한 이유:

졸업요건 판정은 이 기능의 핵심 기준이다. 같은 요건을 시뮬레이션 API에서 다시 구현하면 기존 졸업요건 화면과 결과가 달라질 수 있다.

완료 기준:

- 시뮬레이션 서비스에서 기존 졸업요건 체크 결과를 받을 수 있다.
- `CheckItem` 또는 그에 해당하는 결과를 `requirementItems`에 실을 수 있다.

### 10.4 4단계: KLAS 성적 조회와 파싱 연결

`POST /api/simulation`은 프론트에서 성적 데이터를 받지 않는다. 백엔드가 `Klas-Cookie`로 직접 성적을 조회해야 한다.

사용 대상:

```text
KlasGradeService
KlasGradeParser
KlasSemesterGradeResponse
```

이 단계에서 해야 할 일:

- 컨트롤러 또는 서비스에서 `Klas-Cookie` 헤더를 필수로 받기
- `KlasGradeService.getSemesterGrades(cookie)` 호출
- 반환된 JSON 문자열을 `KlasGradeParser`로 `List<KlasSemesterGradeResponse>`로 변환
- 조회 실패와 파싱 실패를 `502 BAD_GATEWAY`로 분리

이 단계가 필요한 이유:

`/SimulationMain` 화면에는 성적 입력 UI가 없다. 따라서 시뮬레이션 API가 성적 조회까지 책임져야 프론트 요구사항과 맞는다.

완료 기준:

- 서비스 내부에서 KLAS 성적 목록을 Java 객체로 받을 수 있다.
- 요청 본문에 성적 관련 필드가 필요 없다.

### 10.5 5단계: 현재 학점 요약 계산

파싱된 성적 목록을 현재 학점 요약으로 바꾼다.

사용 대상:

```text
GradeCalculator
GraduationCreditCalculator
GraduationSummaryResponse
```

이 단계에서 해야 할 일:

- 기존 `GradeCalculator`로 신청/취득/삭제 학점 계산
- `includeInProgressCourses=false`이면 취득 학점만 반영
- `includeInProgressCourses=true`이면 취득 학점 + 현재 수강 중 학점 반영
- 총/전공/교양/기타 학점을 `summary`로 변환

이 단계가 필요한 이유:

졸업요건과 비교하려면 “현재 학생이 몇 학점을 인정받았는지”가 먼저 필요하다. 이 값이 틀리면 gap과 시뮬레이션 결과도 전부 틀린다.

완료 기준:

- `summary.totalCredit`, `summary.majorCredit`, `summary.cultureCredit`, `summary.etcCredit`를 만들 수 있다.

### 10.6 6단계: 졸업요건 체크 결과를 gap으로 변환

기존 `RequirementChecker` 결과를 시뮬레이션용 부족 조건으로 변환한다.

대상 파일:

```text
RequirementGapCalculator.java
RequirementGapResponse.java
```

이 단계에서 해야 할 일:

- `SATISFIED` 항목은 gap 생성하지 않기
- `INSUFFICIENT` 중 학점/과목 부족분으로 표시 가능한 항목만 gap 생성
- `NEEDS_INPUT` 또는 수동 확인 항목은 gap에서 제외
- 수동 확인 항목은 `warnings` 또는 `requirementItems`에 유지

이 단계가 필요한 이유:

졸업요건 점검 결과는 화면 표시용으로는 자세하지만, 프론트가 부족 조건을 요약해서 보여주려면 “전공 몇 학점 부족”, “교양 몇 학점 부족”처럼 단순화된 gap 목록이 필요하다.

완료 기준:

- 자동 계산 가능한 부족분만 `gaps`에 들어간다.
- 수동 확인 항목은 `gaps`에 들어가지 않고 `warnings` 또는 `requirementItems`에 남는다.

### 10.7 7단계: `GraduationSimulationService`에서 전체 흐름 조립

이제 앞 단계들을 하나의 유스케이스로 연결한다.

대상 파일:

```text
GraduationSimulationService.java
```

서비스 흐름:

```text
요청 검증
-> KLAS 쿠키 검증
-> KLAS 성적 조회
-> 성적 JSON 파싱
-> 현재 학점 요약 계산
-> 졸업요건 로드
-> 졸업요건 점검
-> gap 변환
-> warnings 생성
-> 응답 조립
```

이 단계가 필요한 이유:

각 계산기는 작은 문제만 해결한다. 실제 API 기능은 이 작은 조각들을 올바른 순서로 실행해야 완성된다.

완료 기준:

- 컨트롤러 없이도 서비스 메서드 하나로 `GraduationSimulationResponse`를 만들 수 있다.

### 10.8 8단계: 컨트롤러 연결

프론트가 호출할 API 입구를 만든다.

대상 파일:

```text
GraduationSimulationController.java
```

구현할 API:

```http
POST /api/simulation
```

이 단계에서 해야 할 일:

- `@RequestHeader("Klas-Cookie")`로 쿠키 받기
- `@RequestBody GraduationSimulationRequest`로 요청 받기
- `GraduationSimulationService` 호출
- `GraduationSimulationResponse` 반환

이 단계가 필요한 이유:

서비스만 있으면 내부 계산은 가능하지만 프론트가 호출할 수 없다. 컨트롤러가 있어야 HTTP API가 된다.

완료 기준:

- `/api/simulation` 엔드포인트가 Spring에 등록된다.

### 10.9 9단계: 보안 설정과 테스트 요청 파일 추가

API를 직접 호출해 볼 수 있게 환경을 정리한다.

대상 파일:

```text
SecurityConfig.java
http/simulation-test.http
```

이 단계에서 해야 할 일:

- 개발 중 `/api/simulation` 호출이 인증 설정에 막히지 않는지 확인
- 팀 정책상 허용 가능하면 `/api/simulation`을 permit 처리
- `http/simulation-test.http`에 예시 요청 추가

이 단계가 필요한 이유:

기능 구현이 맞아도 보안 설정 때문에 401이 나면 테스트가 막힌다. 또한 예시 요청 파일이 있으면 팀원이 같은 방식으로 API를 검증할 수 있다.

완료 기준:

- 테스트 요청을 복사하지 않고 파일에서 바로 실행할 수 있다.

### 10.10 10단계: 테스트 작성과 빌드 확인

마지막으로 계산 로직과 Spring 컨텍스트를 검증한다.

우선 테스트 대상:

```text
RequirementGapCalculatorTest
GraduationSimulationServiceTest
```

검증할 케이스:

- 총학점만 부족한 경우
- 전공 학점만 부족한 경우
- 전공과 총학점이 동시에 부족한 경우
- 수동 확인 항목은 gap에서 제외되는지
- 잘못된 요청값이면 400이 나는지

실행:

```bash
./gradlew test
```

이 단계가 필요한 이유:

졸업 시뮬레이션은 사용자에게 졸업 가능 여부를 알려주는 기능이라 계산 오류의 영향이 크다. 숫자 계산과 gap 변환은 테스트로 고정해야 한다.

완료 기준:

- `./gradlew test`가 통과한다.
- API 응답 예시와 실제 응답 구조가 일치한다.

## 11. 완료 기준

다음 조건을 만족하면 구현 완료로 본다.

- `POST /api/simulation` 호출 가능
- KLAS 쿠키로 성적 조회 가능
- 2025/2026 학번, 3개 학과 지원
- 기존 졸업요건 점검 결과가 응답에 포함됨
- 자동 계산 가능한 부족 학점이 `gaps`에 포함됨
- 수동 확인 항목이 `warnings` 또는 `requirementItems`에 표시됨
- `./gradlew test` 통과
