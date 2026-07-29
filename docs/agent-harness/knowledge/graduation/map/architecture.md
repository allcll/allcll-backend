# 졸업요건 검사 아키텍처

졸업요건 검사 피쳐의 데이터 흐름과 책임 분리. 학기 무관 자산.

마지막 갱신: 2026-05-18 (PR #254/255/269/277/278 반영)

이 파일은 *전체 그림*만 박제한다. 단계별 *정책 계약*은 다음 파일들 참조:

- **../contracts/data-loading-policy.md** — 시트 → DB 적재 계약
- **../contracts/data-usage-policy.md** — DB → 검사 결과 계약
- **../contracts/certification-policy.md** — 인증제 3종 통합 정책 (영어·코딩·고전독서)
- **../edge-cases.md** — 학번별 보정·전공 예외 등 22개 EC 카탈로그
- **../glossary.md** — 용어·enum 사전

---

## 큰 그림

```
[수강편람 PDF]
       │
       │  (사람이 옮김, 학기 시작 시 1회)
       ▼
[Google Sheets: 졸업요건검사 기준 데이터]
       │  ─ 13개 탭 (시트1 스크래치 제외, course_replacements 미적재 1개 포함)
       │
       │  (어드민 sync 호출, POST /api/admin/graduation/sync)
       │   → AdminGraduationSyncService.syncGraduationRules  @Transactional
       │
       ▼  ─ fetch (Google Sheets API) → validate → deleteAll + saveAll (12회 반복)
       │
[MySQL: 12개 테이블]
       │
       ├── 기준 조회 경로 (GET /api/graduation/categories)
       │     NonMajorCategoryResolver + MajorCategoryResolver + BalanceRequiredResolver
       │     → GraduationCategoryResponse (검사 X, 단순 조회)
       │
       └── 검사 경로 (POST /api/graduation/check)
             ├─ [성적표 엑셀] → GradeExcelParser → CompletedCourse[]
             ├─ GraduationChecker.calculate
             │      ├─ resolveCreditCriteria (DB read)
             │      ├─ CategoryCreditCalculator.calculateCategoryResults
             │      │      ├─ 일반 카테고리 (AcademicBasicPolicy 추가 검증)
             │      │      ├─ 균형교양 (CategoryCalculator 자체 분기)
             │      │      └─ TOTAL_COMPLETION
             │      └─ CertificationChecker.checkAndUpdate (영어·코딩·고전)
             ├─ GraduationCheckPersistenceService.saveCheckResult
             └─ GraduationCheckResponseMapper (응답 단계 보정: 전필→전선, 면제 isRequired)
                   ↓
              CheckResult 응답
```

**주의**: 기준 조회 경로와 검사 경로가 *균형교양* 데이터를 *다른 키로* 조회 — ../contracts/data-usage-policy.md §6 참조.

---

## 시트 13개 인벤토리 (구글 시트: 졸업요건검사 기준 데이터)

| # | 탭 키 | Validator | 엔티티 | Sync 메서드 | 비고 |
|---|---|---|---|---|---|
| 1 | `credit_criteria` | CreditCriteriaSheetValidator | CreditCriterion | syncCreditCriteria | 기본 학점 기준 |
| 2 | `double_credit_criteria` | DoubleCreditCriteriaSheetValidator | DoubleCreditCriterion | syncDoubleCreditCriteria | 복수전공 학과 페어 예외 (EC-003) |
| 3 | `required_courses` | RequiredCoursesSheetValidator | RequiredCourse | syncRequiredCourses | 학과별 필수 과목 (courseKey override 패턴 — EC-010) |
| 4 | `course_equivalences` | CourseEquivalencesSheetValidator | CourseEquivalence | syncCourseEquivalences | 동일 과목 인정 매핑 (AcademicBasicPolicy 가 사용) |
| 5 | `balance_required_rules` | BalanceRequiredRulesSheetValidator | BalanceRequiredRule | syncBalanceRequiredRule | 균형교양 학과별 규칙 (ALL_DEPT 표기 다름 — EC-020) |
| 6 | `balance_required_course_area_map` | BalanceRequiredCourseAreaMapSheetValidator | BalanceRequiredCourseAreaMap | syncBalanceRequiredCourseAreaMap | 과목 → 영역 매핑 |
| 7 | `balance_required_area_exclusions` | BalanceRequiredAreaExclusionsSheetValidator | BalanceRequiredAreaExclusion | syncBalanceRequiredAreaExclusion | 학과군별 제외 영역 (여러 개 가능 — EC-019) |
| 8 | `graduation_cert_rules` | GraduationCertRulesSheetValidator | GraduationCertRule | syncGraduationCertRule | 인증제 적용 규칙 (학번별) |
| 9 | `english_cert_criteria` | EnglishCertCriteriaSheetValidator | EnglishCertCriterion | syncEnglishCertCriteria | 영어 인증 + alt_curi_no |
| 10 | `coding_cert_criteria` | CodingCertCriteriaSheetValidator | CodingCertCriterion | syncCodingCertCriteria | 코딩 인증 + alt1/alt2 (EC-016) |
| 11 | `classic_cert_criteria` | ClassicCertCriteriaSheetValidator | ClassicCertCriterion | syncClassicCertCriteria | 고전 인증 영역별 필수 권수 (EC-022) |
| 12 | `graduation_department_info` | GraduationDepartmentInfoSheetValidator | GraduationDepartmentInfo | syncGraduationDepartmentInfo | 학과 메타 (인증 대상 분류 포함) |
| — | `course_replacements` | (확인 필요) | (CourseReplacement 가 있지만 sync 메서드 없음) | **없음** | **EC-011** — 다른 적재 경로 추정 |
| — | `시트1` | — | — | — | 빈 스크래치 탭, 안전하게 무시됨 |

---

## 단계별 책임

### 단계 1: 정책 적재

자세한 계약: **../contracts/data-loading-policy.md** 참조

요약:
- 트리거: 어드민이 명시적으로 `POST /api/admin/graduation/sync` 호출
- 패턴: 12개 탭 *fetch → validate → deleteAll → saveAll*
- 트랜잭션: 단일 `@Transactional`
- 검증 한계: 형식만 (의미 정합성은 `graduation-data-validation` skill 영역)

### 단계 2: 사용자 입력 (성적표 엑셀)

- `GradeExcelParser` → `CompletedCourseDto[]`
- `CompletedCoursePersistenceService.saveAllCompletedCourse` → `CompletedCourse[]`
- `CategoryType.fromRaw(raw, admissionYear)` 으로 별칭 매핑 + 학번 보정 (EC-001, EC-002)
- `CompletedCourse.isCreditEarned` 으로 F/FA/NP 학점 필터링
- 외부 의존: 학교 SJPT 엑셀 형식

### 단계 3: 기준 조회 (`/api/graduation/categories`)

- 검사 X, *어떤 카테고리·과목·학점이 필요한지* 조회
- `NonMajorCategoryResolver`: 교양 영역 (MajorType=ALL) + 균형교양 (BalanceRequiredResolver 호출)
- `MajorCategoryResolver`: 전공 영역 (SINGLE/DOUBLE 분기)
- 출력: `GraduationCategoryResponse[]`

### 단계 4: 검사 (`GraduationChecker.calculate`)

자세한 계약: **../contracts/data-usage-policy.md** §3 참조

- 사용자 정보 (User) + earnedCourses → CheckResult
- 단일 트랜잭션 안에서 DB read + 계산 + 결과 저장
- 분기: `MajorType.SINGLE` vs `DOUBLE` — DOUBLE 은 `buildDoubleMajorCriteria` 의 4단계

### 단계 5: 결과 응답

- 결과 영속화: `GraduationCheck` (사용자당 1행, 덮어쓰기, *원본 값 저장*)
- 응답 단계 보정: `GraduationCheckResponseMapper`
    - 전필 초과 → 전선 이관 (EC-012)
    - 인증 면제 → `isRequired=false` (EC-014)
- 조회: `GET /api/graduation/check` — 저장된 결과 + 응답 보정
- 부분 갱신: `PATCH /api/graduation/check/certifications/english`

---

## 핵심 컴포넌트 책임 매트릭스

### Sync (적재) 경로

| 컴포넌트 | 책임 | 트랜잭션 |
|---|---|---|
| `AdminGraduationApi` | sync 트리거·인증 검증 | (Spring 기본) |
| `AdminGraduationSyncService` | 12개 탭 fetch+validate+적재 | `@Transactional` (전체) |
| `GraduationSheetFetcher` | Google Sheets API 호출 | (트랜잭션 무관) |
| `GraduationSheetTable` | 시트 row → key·value 추출 | (POJO) |
| `*SheetValidator` | 시트별 형식 검증 (12종) | (스레드 안전) |
| `GraduationSheetValidatorRegistry` | tabKey → validator dispatch | (싱글톤) |

### 기준 조회 경로 (`/api/graduation/categories`)

| 컴포넌트 | 책임 | 트랜잭션 |
|---|---|---|
| `GraduationCategoryApi` | API 진입점 (PR #255) | (Spring 기본) |
| `GraduationCategoryService` | 오케스트레이션 (SINGLE/DOUBLE 분기 *X* — Resolver 가 처리) | (read only) |
| `NonMajorCategoryResolver` | 교양 영역 기준 조회 (courseKey override 패턴 — EC-010) | (read only) |
| `MajorCategoryResolver` | 전공 영역 기준 조회 (SINGLE/DOUBLE 분기) | (read only) |
| `BalanceRequiredResolver` | 균형배분 기준 조회 (NonMajor 가 호출) | (read only) |
| `DoubleCreditCriterionResolver` | 복수전공 학과 페어 → 예외 기준 3단계 fallback | (read only) |
| `RequiredCourseResolver` | DEPRECATED 과목 → CourseReplacement 치환 (EC-011) | (read only) |

### 검사 경로 (`/api/graduation/check`)

| 컴포넌트 | 책임 | 트랜잭션 |
|---|---|---|
| `GraduationCheckApi` | 사용자 검사 트리거·결과 조회 | (Spring 기본) |
| `GraduationCheckService` | 엑셀 파싱·저장·검사·결과 저장 오케스트레이션 | `@Transactional(readOnly)` 기본 + 쓰기 메서드 `@Transactional` |
| `GradeExcelParser` | 엑셀 → `CompletedCourseDto[]` | (트랜잭션 무관) |
| `CompletedCoursePersistenceService` | CompletedCourse 적재 | `@Transactional` |
| `GraduationChecker` | 카테고리·총학점·인증 오케스트레이션 | (외부에 의존) |
| `CategoryCreditCalculator` | 이수구분별 학점 합산 + 균형교양 검사 | (Component, 메서드별 위임) |
| `AcademicBasicPolicy` | 기필 과목의 학과 인정 여부 추가 검증 (EC-021) | (read only) |
| `CertificationChecker` | 인증제 3종 검사 + 대체 과목 자동 적용 | `@Transactional(readOnly)` 클래스 + `@Transactional` 메서드 |
| `*AltCoursePolicy` (3종) | 대체 과목 통과 판정 | (`CertificationPolicyConfig` Bean) |
| `GraduationCheckResponseMapper` | 응답 단계 보정 (전필→전선, 면제 isRequired) | (트랜잭션 무관) |
| `GraduationCheckPersistenceService` | 검사 결과 저장 (사용자당 1행) | `@Transactional` |

---

## 핵심 패턴 1: courseKey 와일드카드 override (교양 지정과목)

`NonMajorCategoryResolver#loadRequiredCourses` 의 핵심 로직 — 교양 지정과목 데이터 적재의 *시트 컨벤션* 을 코드가 어떻게 해석하는지.

```
시트 적재 컨벤션:
- dept_cd="0" (와일드카드): 모든 학과 공통 기본값
- dept_cd=학과코드 (override): 특정 학과 정책 (required=true|false)

조회·머지 로직:
1. (ALL_DEPT, 사용자 학과코드) 두 학과 조건으로 한꺼번에 fetch
2. courseKey = categoryType + "|" + curiNm 키로 LinkedHashMap 누적
3. 와일드카드 행: putIfAbsent (없을 때만 채움)
4. 학과 행: put (무조건 override, required=true/false 모두)
5. 최종: required=true 만 필터링 후 categoryType별 그룹화
```

자세히: ../edge-cases.md EC-010

이 패턴이 의미하는 것:
- 공통 + 학과별 면제 패턴 (A): 공통=true / 학과=false → 면제 적용
- 학과별 적용 패턴 (B): 공통 없음 / 학과=true 만 있음
- 같은 키의 우선순위: **학과 정책 > 와일드카드**

---

## 핵심 패턴 2: SINGLE vs DOUBLE 분기

### `MajorCategoryResolver.resolve` (기준 조회 경로)

```
if (SINGLE):
    return resolveSingleType(year, deptCd)
return resolveDoubleType(year, primaryDeptCd, secondaryDeptCd, user)
```

### `GraduationChecker.resolveCreditCriteria` (검사 경로)

```
if (MajorType.DOUBLE.equals(user.majorType)):
    return buildDoubleMajorCriteria(user, year, deptNm)
return creditCriterionRepository.findByAdmissionYearAndDeptNmAndMajorTypeIn(
    year, deptNm, List.of(ALL, SINGLE)
)
```

**두 분기가 비슷한 형태로 두 군데에 존재** — 리팩토링 후보 (../contracts/data-usage-policy.md RU-002)

### DOUBLE 분기의 4단계 (EC-003, EC-005, EC-006)

```
buildDoubleMajorCriteria(user, year, deptNm):
 1. nonMajorCriteria = CreditCriterion(year, deptNm, ALL)     # 교양 기준
 2. defaultDoubleCriteria = CreditCriterion(year, *, DOUBLE)   # deptNm 필터 없음 (EC-006)
 3. exceptionCriteria = DoubleCreditCriterion(year, primaryDeptCd, secondaryDeptCd)
 4. applyExceptionCriteria:
      - default 행 순회: 예외 키와 매칭되면 → 예외로 override
      - 남은 예외 행 (key 매칭 못 한 것) → 새로 추가 (EC-005)
```

### `DoubleCreditCriterionResolver.resolve` 의 3단계 fallback (EC-003)

1. (primaryDeptCd, secondaryDeptCd) 양쪽 매칭 (가장 구체)
2. (primaryDeptCd, ALL_DEPT="0")
3. (ALL_DEPT="0", secondaryDeptCd)
4. 셋 다 실패 시 빈 리스트 → `MajorCategoryResolver` fallback

---

## 핵심 패턴 3: 균형교양 *두 경로* (헷갈리기 쉬움)

**중요**: 두 경로가 *서로 다른 ALL_DEPT 표기* 사용. ../contracts/data-usage-policy.md §6 참조.

| 경로 | 호출자 | ALL_DEPT 표기 | 제외 영역 수 |
|---|---|---|---|
| BalanceRequiredResolver | NonMajorCategoryResolver (기준 조회) | `dept_cd="0"` | 1개 (findByXxx) |
| CategoryCreditCalculator#findBalanceRequiredRule | GraduationChecker (검사) | `dept_nm="ALL"` | 여러 개 (findAllByXxx, EC-019) |

분류 B (스키마 통일) 또는 분류 A (정책 결정) 후보.

---

## 핵심 패턴 4: 인증제 3종 정책 구조

자세히: **../contracts/certification-policy.md** 참조.

```
CertificationPolicyConfig (Spring Bean 정의)
 ├─ englishAltCoursePolicy  → GraduationCertificationAltCoursePolicy 구현체
 ├─ codingAltCoursePolicy   → GraduationCertificationAltCoursePolicy 구현체
 └─ classicAltCoursePolicy  → ClassicAltCoursePolicy (시그니처 다름)

CertificationChecker.checkAndUpdate(userId, earnedCourses)
 ├─ User + GraduationDepartmentInfo (주전공 기준 — EC-013) 조회 — hard fail
 ├─ englishAltCoursePolicy.isSatisfiedByAltCourse(...) → certResult.passEnglish()
 ├─ codingAltCoursePolicy.isSatisfiedByAltCourse(...) → certResult.passCoding()
 ├─ classicAltCoursePolicy.isSatisfiedByAltCourse(user) → certResult.passClassic()
 └─ if (isChanged) certResult.reCalculate()
```

면제 처리: 각 정책의 `EXEMPT` 분기에서 검사 skip. 응답은 `isRequired=false` (EC-014).
우선순위: 로그인 시 대체 과목 우선 적용, 검사 시 재보정 (EC-015 해결됨 — PR #280).

---

## 응답 단계 보정 (`GraduationCheckResponseMapper`)

DB 저장값과 응답값이 다를 수 있음. *졸업 가능 여부* 의 사용자 표시 정확성을 위한 보정.

### 전필 → 전선 이관 (EC-012)

- MAJOR_REQUIRED 카테고리 학점 초과 시 MAJOR_ELECTIVE 로 이관
- DB 에는 원본 저장, 응답에서만 보정
- MajorScope (PRIMARY/SECONDARY) 별로 그룹핑하여 처리

### 인증 면제 응답 (EC-014)

- `EXEMPT` 사용자는 `isRequired=false` (UI 카드 숨김)
- `passed` 는 DB 값 그대로 (이전: 무조건 true → 제거)

### 학과·학번 기반 동적 조회 (PR #269 fix)

- 이전: 응답에 `EnglishTargetType.NON_MAJOR` 고정값 (버그)
- 현재: GraduationDepartmentInfo 동적 조회

---

## 별도 축: 운영 도메인

CLAUDE.md 의 5단계 여석 파이프라인은 graduation 도메인과 **무관**. 헷갈리지 말 것.

- 여석/SSE/스케줄러: `support/sse`, `support/scheduler`, `domain/seat`
- graduation: 위와 별도 흐름. 사용자 요청 트리거 응답 (실시간 SSE 없음)

---

## Cross-Reference Index

| 토픽 | 주 파일 | 보조 파일 |
|---|---|---|
| 시트 13개 인벤토리 | architecture.md (본 파일) | ../contracts/data-loading-policy.md §8, graduation-wiki `sheets/<날짜>/` 스냅샷 |
| 정책 **값** (학점·영역·인증 기준) | **graduation-wiki `cohort/`** (별도 private 레포 — 태그 포인터로 인용) | ../glossary.md, ../contracts/certification-policy.md |
| 적재 트리거·트랜잭션·검증 | ../contracts/data-loading-policy.md | architecture.md §단계1 |
| 검사 호출·신뢰 가정·재계산 | ../contracts/data-usage-policy.md | architecture.md §단계3·4·5 |
| 인증제 (영어·코딩·고전) 통합 정책 | ../contracts/certification-policy.md | ../contracts/data-usage-policy.md §5, ../edge-cases.md EC-013~016, EC-022 |
| 복수전공 예외 분기 | ../edge-cases.md EC-003~006 | architecture.md (패턴 2), ../contracts/data-usage-policy.md §3 |
| 균형교양 두 경로 | architecture.md (패턴 3) | ../contracts/data-usage-policy.md §6, ../edge-cases.md EC-017~020 |
| courseKey 와일드카드 override | ../edge-cases.md EC-010 | architecture.md (패턴 1) |
| MAJOR_BASIC 학번 보정 | ../edge-cases.md EC-001 | ../glossary.md, ../decisions/2026-02-21 |
| CategoryType 별칭 매핑 | ../edge-cases.md EC-002 | ../glossary.md |
| ALL_DEPT 매직 값 (표기 불일치) | ../edge-cases.md EC-004, EC-020 | architecture.md (패턴 3), ../contracts/data-usage-policy.md §6 |
| 전필→전선 이관 | ../edge-cases.md EC-012 | architecture.md (응답 보정), ../contracts/data-usage-policy.md §10 |
| 인증 면제 응답 | ../edge-cases.md EC-014 | ../contracts/certification-policy.md §4 |
| DEPRECATED 과목 매핑 | ../edge-cases.md EC-011 | architecture.md (RequiredCourseResolver), ../contracts/data-usage-policy.md §11.4 |
| AcademicBasicPolicy 추가 검증 | ../edge-cases.md EC-021 | ../contracts/data-usage-policy.md §4.2 |
| 균형교양 만족 조건 (학점+영역) | ../edge-cases.md EC-018 | ../contracts/data-usage-policy.md §4.3 |
| 18-21학번 균필 부재 | ../edge-cases.md EC-017 | ../contracts/certification-policy.md §4.3 |
| 고전독서 인증권수 의미 | ../edge-cases.md EC-022 | ../contracts/certification-policy.md §6.2 |
| enabled=FALSE 처리 | ../edge-cases.md EC-007 | ../contracts/data-loading-policy.md |

---

## 검수 상태

- [x] 큰 그림 다이어그램: 코드 일치 (기준 조회 경로 + 검사 경로 분리)
- [x] 시트 13개 인벤토리: 코드 + 시트 탭 목록 일치
- [x] 핵심 컴포넌트 책임 매트릭스: 코드 + PR 일치 (3개 경로별 분리)
- [x] courseKey 와일드카드 override 패턴: 코드 일치 (EC-010)
- [x] SINGLE/DOUBLE 분기 패턴: 코드 일치
- [x] 균형교양 두 경로 차이: 코드 일치 (EC-019, EC-020 보강)
- [x] 인증제 정책 구조: ../contracts/certification-policy.md 와 일치
- [x] 응답 보정 패턴: 코드 + PR #254, #269, #277 일치
- [ ] **`course_replacements` 탭의 sync 경로**: 다른 적재 경로 추정 — 확인 필요
- [ ] **`CompletedCoursePersistenceService`, `GraduationCheckPersistenceService` 트랜잭션**: 코드 미열람 (`@Transactional` 추정)
