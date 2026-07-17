# 졸업요건 도메인 용어 사전

graduation 도메인의 모든 작업에서 참조할 단일 용어 사전. **코드 enum 과 1:1 일치**해야 한다.

이 파일은 학기 무관 자산이다. 정책 텍스트가 바뀌어도 enum·자료구조가 안 바뀌면 갱신 불필요.

마지막 갱신: 2026-05-18

---

## 전공 관련

### MajorType (전공 이수 형태)

코드: `kr.allcll.backend.domain.graduation.MajorType`

| 값 | 의미 | 비고 |
|---|---|---|
| `SINGLE` | 단일 전공 | 사용자가 주전공 하나만 가짐 |
| `DOUBLE` | 복수 전공 | 사용자가 주전공 + 복수전공 |
| `ALL` | 모든 전공 범위 | 교양 등 전공 무관 기준에 사용 |

- DB 컬럼: `credit_criteria.major_type`, `double_credit_criteria.major_type` 등
- 사용자가 `User.majorType` 으로 어느 형태인지 보유

### MajorScope (기준 적용 전공 범위)

코드: `kr.allcll.backend.domain.graduation.MajorScope`

| 값 | 의미 |
|---|---|
| `PRIMARY` | 주전공 기준 |
| `SECONDARY` | 복수전공 기준 |

- DB 컬럼: `credit_criteria.major_scope`, `double_credit_criteria.major_scope`
- `DoubleCreditCriterion` 한 행이 PRIMARY/SECONDARY 어느 쪽 기준인지 명시

### MajorType vs MajorScope — 헷갈리기 쉬움

- **MajorType**: *학생이 어떤 전공 이수 형태인가* (단일/복수)
- **MajorScope**: *이 기준 행이 주전공용인가 복수전공용인가*
- 예: `MajorType=DOUBLE` 학생의 학점 계산에는 `MajorScope=PRIMARY` 행 + `MajorScope=SECONDARY` 행이 모두 필요

---

## 이수구분 (CategoryType)

코드: `kr.allcll.backend.domain.graduation.credit.CategoryType`

| 코드값 | 한글 의미 | 시트 별칭 (aliases) |
|---|---|---|
| `COMMON_REQUIRED` | 공통교양필수 | 교필, 공필 |
| `BALANCE_REQUIRED` | 균형교양 | 균필 |
| `ACADEMIC_BASIC` | 학문기초교양 | 기교, 기필 |
| `GENERAL_ELECTIVE` | 교양선택 | 교선, 교선1, 교선2 |
| `GENERAL` | 교양 | 교양 |
| `MAJOR_REQUIRED` | 전공필수 | 전필, 복필 |
| `MAJOR_ELECTIVE` | 전공선택 | 전선, 복선 |
| `MAJOR_BASIC` | 전공기초 | 전기 |
| `TOTAL_COMPLETION` | 전체 이수 | (별칭 없음, 합계용) |

별칭은 시트·성적표 엑셀의 raw 문자열 → enum 변환 시 사용 (`CategoryType.fromRaw`).

**중요 보정 규칙**: `MAJOR_BASIC` 은 학번 < 2024 인 경우 `ACADEMIC_BASIC` 로 변환됨.
EC-001 참조.

### 카테고리 그룹

- **전공 카테고리** (`isMajorCategory()`): `MAJOR_REQUIRED`, `MAJOR_ELECTIVE`
- **비전공 카테고리** (`isNonMajorCategory()`): 위 외 전부
- **재배치 대상** (`isReallocateTarget()`): `MAJOR_REQUIRED`, `MAJOR_ELECTIVE`, `GENERAL`

---

## 균형교양 영역 (BalanceRequiredArea)

코드: `kr.allcll.backend.domain.graduation.balance.BalanceRequiredArea`

| 코드값 | name (코드 내 문자열) |
|---|---|
| `HISTORY_THOUGHT` | 역사와사상 |
| `NATURE_SCIENCE` | 자연과과학 |
| `ECONOMY_SOCIETY` | 경제와사회 |
| `CULTURE_ARTS` | 문화와예술 |
| `CONVERGENCE_AND_CREATIVITY` | 융합과창의 |

`fromSelectedArea` 는 공백 제거 후 **완전일치** 비교 — 불일치 시 **null 반환** (예외 아님 → 조용히 영역 미집계).

> **EC-023 (해결 2026-07-12)**: 과거 `NATURE_SCIENCE("자연과학")` 이 편람·위키 정본 "자연과과학"
> (수강편람 2026-1 p.52 §4) 과 달라 QA B1 (이수영역 1/2) 의 원인이었음 — enum name 을 편람 표기로 수정.
> 이 표는 "코드가 지금 이렇다" 는 기록이지 이 값이 맞다는 뜻이 아님 — 정본은 wiki cohort/*/balance.md.

---

## 인증제 (GraduationCertType)

코드: `kr.allcll.backend.domain.graduation.certification.GraduationCertType`

| 코드값 | 의미 |
|---|---|
| `CERT_ENGLISH` | 영어 인증 |
| `CERT_CLASSIC` | 고전 인증 |
| `CERT_CODING` | 코딩 인증 |

각 인증제마다 별도의 기준 엔티티·대체 과목 정책 클래스 존재:

- 영어: `EnglishCertCriterion`, `EnglishAltCoursePolicy`
- 고전: `ClassicCertCriterion`, `ClassicAltCoursePolicy`, `ClassicsArea`
- 코딩: `CodingCertCriterion`, `CodingAltCoursePolicy`

---

## 시트·DB 적재 관련

### 학과 코드

- `deptCd`: 학과 코드 (e.g. `CSE`, `ECO`)
- `deptNm`: 학과명 (e.g. `컴퓨터공학과`)
- **`ALL_DEPT = "0"`**: 학과 무관 매직 값. `MajorCategoryResolver.ALL_DEPT`, `DoubleCreditCriterionResolver.ALL_DEPT` 양쪽에서 정의 (코드 중복 — 통합 검토 대상)

### admission_year vs admission_year_short

- `admission_year`: 4자리 입학년도 (e.g. 2024)
- `admission_year_short`: 2자리 (e.g. 24)
- DB 양쪽 다 저장. 시트에도 양쪽 컬럼 존재 (중복 — 사람 가독성 목적)

---

## 검사 결과 관련

### CheckResult

코드: `kr.allcll.backend.domain.graduation.check.result.dto.CheckResult`

졸업 가능 여부 + 카테고리별 결과 + 인증제 결과의 최상위 응답 DTO.

### GraduationCategory

이수구분 단위 결과. 요구 학점 vs 이수 학점 + 부족 학점.

### CertResult

인증제 3종 (영어·고전·코딩) 결과.

---

## 외부 도메인 용어

CLAUDE.md 에서 가져옴 (참조용):

- **여석**: 남은 수강 가능 자리 수 (이 도메인과 무관, seat 도메인)
- **관담인원**: 관심 과목으로 등록한 학생 수
- **핀 (Pin)**: 사용자 지정 관심 과목
- **관심과목 (basket)**: 사용자 담은 과목 목록

졸업요건 도메인 자체에는 위 용어 안 쓰임. 다른 도메인과 헷갈리지 않게 박제.

---

## 검수 상태

- [x] MajorType/MajorScope: 코드 일치 확인
- [x] CategoryType: 코드 일치 + aliases 박제
- [x] BalanceRequiredArea: 5개 값 코드 일치
- [x] GraduationCertType: 3개 값 코드 일치
- [ ] **학번별 보정 규칙 (MAJOR_BASIC 2024년 기준)**: 정책 출처 확인 필요 (사람 검수)
- [ ] **CategoryType aliases**: 시트에 실제 어느 별칭이 쓰이는지 운영 확인 필요

---

## 추가 박제 (PR #254/255/269/277/278 반영, 2026-05-18)

### courseKey

코드: `KeyUtils.generate(categoryType, curiNm)` (=`categoryType + "|" + curiNm`)

- 교양 지정과목 (`RequiredCourse`) 의 와일드카드 override 매핑에 사용
- `NonMajorCategoryResolver#loadRequiredCourses` 의 `LinkedHashMap<String, RequiredCourse>` 키
- 같은 카테고리·같은 과목명 = 같은 키 (학과 무관)
- 와일드카드 (ALL_DEPT) 행과 학과별 행이 같은 키로 머지됨
- 자세히: edge-cases.md EC-010

**함정**: 학수번호 변경 시 같은 과목으로 인식 안 됨. CourseEquivalence 와 별개 경로.

### EnglishTargetType (영어 인증 대상 분류)

코드: `kr.allcll.backend.domain.graduation.certification.EnglishTargetType`

| 값 | 의미 |
|---|---|
| `NON_MAJOR` | 비전공 (일반 영어 인증 대상) |
| (전공별 값들) | 코드 미열람 — 추후 확인 필요 |
| `EXEMPT` | **면제 — 영어 인증 불필요** |

DB 컬럼: `graduation_department_info.english_target_type` (학과·학번별)

자세히: contracts/certification-policy.md §2.1

### CodingTargetType (코딩 인증 대상 분류)

코드: `kr.allcll.backend.domain.graduation.certification.CodingTargetType`

| 값 | 의미 | 대체 과목 |
|---|---|---|
| `CODING_MAJOR` | 코딩 전공자 | alt1 (고급C) B0 이상만 |
| `NON_MAJOR` | 비전공자 | alt1 또는 alt2 (K-MOOC) |
| `EXEMPT` | **면제** | — |

DB 컬럼: `graduation_department_info.coding_target_type`

자세히: contracts/certification-policy.md §2.2

### CodingAltCourseGradeRequirement (코딩 대체 과목 등급 enum)

코드: `kr.allcll.backend.domain.graduation.certification.CodingAltCourseGradeRequirement` (또는 `GradeThreshold`)

| 등급 | 평점 |
|---|---|
| A+ | 4.5 |
| A | 4.0 |
| A- | 3.7 |
| B+ | 3.3 |
| B0 | 3.0 |
| B- | 2.7 |
| C+ | 2.3 |
| C0 | 2.0 |
| C- | 1.7 |
| D+ | 1.3 |
| D0 | 1.0 |
| F | 0 |
| FA | 0 |
| P | (취득) |
| NP | (미취득) |

P/NP 과목은 별도 처리. `isCreditEarned` 가 F/FA/NP 사전 필터링.

자세히: contracts/certification-policy.md §3.2, edge-cases.md EC-016

### DEPRECATED 과목

- 정의: `RequiredCourse.curiNo = "DEPRECATED"` 인 행. 과목명 변경·폐지로 학수번호 매칭 불가
- 처리: `RequiredCourseResolver` 가 `CourseReplacementRepository.findByLegacyCuriNm` 로 현재 과목 매핑 조회 → 치환
- NPE 방어 (PR #255): 매핑 누락 시 원본 그대로 + 경고 로그
- 자세히: edge-cases.md EC-011

### 인증권수 vs 이수권수 (고전독서 표 컬럼)

대휴칼 고전독서 페이지 표의 두 컬럼이 헷갈리기 쉬움:

| 컬럼 | 실제 의미 |
|---|---|
| 이수권수 | 사용자가 이수한 권수 (실제 데이터) |
| 인증권수 | **min(필요 권수, 이수 권수)** — *기준 권수 아님* |

PR #254 발견. 자세히: edge-cases.md EC-022, contracts/certification-policy.md §6.2

기준 권수는 `classic_cert_criteria` DB 에서 조회 (대휴칼 파싱 X).

### Intensive English (영어 대체 과목)

- 영어 인증 대체 과목의 대표 예시
- `english_cert_criteria.alt_curi_no` 에 학수번호 박제 (학번·학과별 1개)
- 학점 인정 (`isCreditEarned`) 됐으면 통과로 처리
- 등급 기준 없음

### 고급C프로그래밍실습 (코딩 대체 과목 alt1)

- 코딩 인증 1순위 대체 과목
- `coding_cert_criteria.alt1_curi_no` 에 학수번호, `alt1_min_grade` 에 최소 등급 (`B0`) 박제
- 전공자·비전공자 모두 인정 (단, 전공자는 alt1 만, 비전공자는 alt2 도 추가)

### K-MOOC:코딩과스토리텔링 (코딩 대체 과목 alt2)

- 비전공자 전용 코딩 인증 대체 과목 (P/NP 평가)
- `coding_cert_criteria.alt2_curi_no` 에 학수번호, `alt2_min_grade` 에 `P` 박제
- 전공자 (`CodingTargetType.CODING_MAJOR`) 는 인정 안 됨

### MajorBasicPolicy (폐기)

- PR #278 에서 도입됐다가 이후 리팩토링으로 제거
- 역할이 `CategoryType.fromRaw(raw, admissionYear)` 의 `normalizeMajorBasic` 으로 이전됨
- 결정 박제: decisions/2026-02-21-major-basic-normalization-location.md
- 코드에서 grep 해도 더 이상 안 보임 — 검수 통과

### MajorCategoryResolver vs CategoryCreditCalculator (이름 혼동 주의)

- `MajorCategoryResolver`: **기준 조회 경로** (`/api/graduation/categories`). 전공 카테고리 *기준* 응답
- `CategoryCreditCalculator`: **검사 경로** (`/api/graduation/check`). 모든 카테고리 *학점 합산*
- 이름이 비슷하나 호출 경로가 완전히 다름. contracts/data-usage-policy.md §3 + map/architecture.md 책임 매트릭스 참조

### 균형교양 ALL_DEPT 표기 (두 가지)

- 일반 시트: `dept_cd="0"` (숫자 코드 0)
- **`balance_required_rules` 만 다름**: `dept_nm="ALL"` (문자열 ALL)
- `CategoryCreditCalculator#findBalanceRequiredRule` fallback 에서 `"ALL"` 사용
- 자세히: edge-cases.md EC-020

---

## 검수 상태 (갱신)

- [x] MajorType/MajorScope: 코드 일치
- [x] CategoryType (9개 + aliases): 코드 일치
- [x] BalanceRequiredArea: 5개 값 코드 일치
- [x] GraduationCertType: 3개 값 코드 일치
- [x] courseKey 정의: PR #255 일치
- [x] EnglishTargetType / CodingTargetType (EXEMPT 포함): PR #277 일치 (전체 enum 값은 미열람)
- [x] DEPRECATED 과목 처리: PR #255 일치
- [x] 인증권수/이수권수 의미: PR #254 일치
- [x] MajorBasicPolicy 폐기 박제: 코드 + decisions ADR 일치
- [x] 균형교양 ALL_DEPT 두 표기: 코드 일치 (EC-020)
- [ ] **학번별 보정 규칙 (MAJOR_BASIC 2024년 기준)**: 정책 출처 1차 (수강편람 페이지) 확인 필요
- [ ] **CategoryType aliases**: 시트에 실제 어느 별칭이 쓰이는지 운영 확인 필요
- [ ] **EnglishTargetType / CodingTargetType 전체 enum 값**: 코드 미열람
- [ ] **alt1/alt2 학수번호 실제값**: 시트 cell 확인 필요
