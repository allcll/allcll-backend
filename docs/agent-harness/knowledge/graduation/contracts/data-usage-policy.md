# 졸업요건 데이터 활용 정책

DB 에 적재된 기준 데이터가 사용자 검사 결과로 변환되는 lifecycle 계약.
**이 파일은 `GraduationCheckService`·`GraduationChecker`·`CategoryCreditCalculator`·`CertificationChecker`·`BalanceRequiredResolver`·`NonMajorCategoryResolver`·`MajorCategoryResolver` 의 동작 사양**이다.

마지막 갱신: 2026-05-18 (PR #254, #255, #269, #277, #278 반영)
관련 파일: `GraduationCheckApi`, `GraduationCheckService`, `GraduationChecker`, `CategoryCreditCalculator`, `NonMajorCategoryResolver`, `MajorCategoryResolver`, `DoubleCreditCriterionResolver`, `BalanceRequiredResolver`, `CertificationChecker`, `RequiredCourseResolver`, `AcademicBasicPolicy`, `GraduationCheckResponseMapper`

---

## 1. 호출 시점 (Who/When 데이터를 활용하는가)

### 1.1 사용자 API 엔드포인트 (`GraduationCheckApi`)

| 메서드 | 경로 | 책임 | 트랜잭션 |
|---|---|---|---|
| `POST` | `/api/graduation/check` | 성적표 엑셀 업로드 → 검사 → 결과 저장 | `@Transactional` (쓰기) |
| `GET` | `/api/graduation/check` | 저장된 마지막 검사 결과 조회 — **재계산 없음** | `@Transactional(readOnly)` (기본) |
| `GET` | `/api/graduation/courses` | 사용자의 이수 과목 목록 조회 | `@Transactional(readOnly)` |
| `PATCH` | `/api/graduation/check/certifications/english` | 영어 인증 수동 통과 표시 + 재계산 | `@Transactional` (쓰기) |

### 1.2 어드민 API (`AdminGraduationApi`)

- `GET /api/admin/graduation/{studentId}` — 학생 단건 조회 (저장된 결과 기반, 재계산 없음)

### 1.3 기준 데이터 조회 API (`GraduationCategoryApi` — PR #255)

- `GET /api/graduation/categories` — 사용자 기준 이수구분별 졸업요건 *기준 데이터* 조회. 검사가 아닌 *어떤 카테고리가 있고 어떤 과목·학점이 필요한지* 응답
- 호출 단위: `GraduationCategoryService` → `NonMajorCategoryResolver` + `MajorCategoryResolver`

### 1.4 정책적 의미

- 검사 *계산* 트리거는 **`POST /api/graduation/check` 한 곳뿐**
- 검사 결과 GET 은 저장된 값 그대로 반환 — DB sync 직후 사용자가 재검사하지 않으면 *stale* 결과 반환
- 기준 데이터 GET 은 매 호출마다 DB read (캐싱 없음)
- 운영자 sync 호출 후 모든 사용자 검사를 무효화하거나 재계산하는 메커니즘 없음

---

## 2. 검사 파이프라인 (`POST /api/graduation/check`)

`GraduationCheckService#checkGraduationRequirements` 의 4단계:

```
1. validateExcelFile        — 파일 null·비어있음·확장자(.xlsx) 검증
2. gradeExcelParser.parse   — 엑셀 → CompletedCourseDto 리스트
3. completedCoursePersistenceService.saveAllCompletedCourse
                            — DB 저장 + CompletedCourse 반환
4. graduationChecker.calculate(userId, savedCourses)
                            — CheckResult 산출
5. graduationCheckPersistenceService.saveCheckResult
                            — CheckResult 영속화
```

### 2.1 트랜잭션 경계

- 전체가 단일 `@Transactional` (외부)
- `CertificationChecker.checkAndUpdate` 도 자체 `@Transactional` — REQUIRED 기본이라 외부 트랜잭션에 join
- 4단계 어디서든 실패 시 전체 롤백 → CompletedCourse 저장도 안 됨
- 외부 의존: 엑셀 파일 I/O 만. Google Sheets·외부 API 호출 없음 (DB read only)

### 2.2 캐싱·중복 호출

- 캐싱 없음 — 매 호출마다 DB 읽기 + 계산
- 같은 사용자가 같은 엑셀 재업로드하면 처음부터 다시 계산 + 결과 덮어쓰기
- `GraduationCheck` 의 PK 는 userId — 사용자당 최신 결과 1개만 보존

---

## 3. `GraduationChecker.calculate` 의 내부 단계

```
calculate(userId, savedCourses):
 ├─ filter earnedCourses (CompletedCourse.isEarned)
 │
 ├─ resolveCreditCriteria(userId)              # 사용자 학번·전공으로 기준 조회
 │      ├─ User 조회 — USER_NOT_FOUND
 │      ├─ MajorType.DOUBLE 이면 buildDoubleMajorCriteria
 │      │      ├─ ALL (교양) — CreditCriterion
 │      │      ├─ DOUBLE 기본 — CreditCriterion
 │      │      ├─ 예외 — DoubleCreditCriterion (학과 페어, EC-003)
 │      │      └─ applyExceptionCriteria — override + leftover 추가 (EC-005)
 │      └─ SINGLE 이면 단순 조회
 │
 ├─ categoryCalculator.calculateCategoryResults  # 카테고리별 학점 계산 (§4 참조)
 ├─ summarizeTotalCredits                        # 총 학점 + 잔여
 ├─ certificationChecker.checkAndUpdate          # 인증제 3종 검사 + 대체 과목 (§5, certification-policy.md)
 ├─ canGraduate                                  # 모든 카테고리 satisfied AND certResult.isSatisfied
 │
 └─ CheckResult 반환
```

### 3.1 신뢰 가정

각 단계는 다음을 *가정* 하고 동작:

- **DB 에 들어있는 기준은 옳다** — sync 단계에서 형식 검증을 통과했으니까. 의미상 정합성은 `graduation-data-validation` skill 의 책임
- **사용자 정보는 일관** — User.admissionYear, deptCd, majorType, doubleDeptCd 가 정확
- **CompletedCourse 의 categoryType 은 raw 별칭 매핑 + 학번 보정 완료** — `CategoryType.fromRaw` 가 별칭 처리 + MAJOR_BASIC 학번 보정 (EC-001) 적용

### 3.2 가정 위반 시 동작

- DB 에 기준 행 없음 → 빈 리스트 반환 → 카테고리 결과에서 *해당 카테고리 자체가 안 나타남* (false negative)
    - 예: `MajorType.DOUBLE` 학생인데 `(year, DOUBLE)` 행이 시트에 없으면 → 결과에 전공 카테고리 누락
- 사용자 정보 누락 (User.doubleDeptCd=null) → `DoubleCreditCriterionResolver.resolve` 가 빈 리스트 → fallback 진입
- CompletedCourse 의 raw category 가 어느 별칭과도 매칭 안 됨 → `AllcllErrorCode.CATEGORY_TYPE_NOT_FOUND` 예외 → 전체 검사 실패

---

## 4. 카테고리별 학점 계산 (`CategoryCreditCalculator`)

PR #254 + 코드 확정 사항 박제. 일부 함정은 EC-018·EC-019·EC-020·EC-021 참조.

### 4.1 흐름

```
calculateCategoryResults(userId, earnedCourses, creditCriteria)
 ├─ User, GraduationDepartmentInfo (주전공 기준) 조회 — hard fail
 └─ calculateCategories(earnedCourses, primaryUserDept, creditCriteria)
        ├─ 1단계: 일반 카테고리 (BALANCE_REQUIRED, TOTAL_COMPLETION 제외) 학점 계산
        ├─ 2단계: 균형교양 처리 (addBalanceRequiredIfNeeded)
        └─ 3단계: TOTAL_COMPLETION 카테고리 추가 (학점은 외부에서 채움 — earnedCredits=0)
```

### 4.2 카테고리 학점 계산 (`calculateCategoryCredits`)

이수 과목 필터링 3중:

```java
.filter(course -> course.getCategoryType() == criterion.getCategoryType())  // 카테고리 일치
.filter(course -> matchesMajorScope(course, criterion.getMajorScope()))      // MajorScope 일치
.filter(course -> academicBasicPolicy.isRecentMajorAcademicBasic(course, criterion))  // 기필 추가 검증
.mapToDouble(CompletedCourse::getCredits).sum()
```

**MajorScope 매칭 (`matchesMajorScope`)**:
- `course.majorScope == null` → `criterion.majorScope == PRIMARY` 일 때만 매칭
- 즉 *MajorScope 정보 없는 과목은 자동으로 PRIMARY 로 간주*
- 함정: SECONDARY 기준에 매칭되어야 할 과목이 majorScope=null 이면 PRIMARY 로만 합산됨

**AcademicBasicPolicy 추가 검증** (EC-021):
- ACADEMIC_BASIC 카테고리 과목에 대해서만 적용
- 학과·학번의 RequiredCourse (ACADEMIC_BASIC) 목록 + CourseEquivalence (동일 과목 그룹) 으로 *실제 학과 인정 과목인지* 검증
- 비-ACADEMIC_BASIC 은 early return true → 영향 없음

### 4.3 균형교양 처리 (`addBalanceRequiredIfNeeded`)

- 룰 조회 (`findBalanceRequiredRule`):
    1. `(admissionYear, user.deptNm)` 매칭 시도
    2. fallback: `(admissionYear, "ALL")` — **문자열 "ALL"** (다른 시트의 ALL_DEPT="0" 과 다름, EC-020)
    3. 둘 다 없으면 null → early return (카테고리 결과에 BALANCE_REQUIRED 자체가 안 나타남, EC-017)
- 룰 있으면 `rule.required` 검사:
    - false → early return (균필 면제 학과)
    - true → `calculateBalanceRequired` 호출
- 만족 조건 (EC-018): `creditsSatisfied && areasSatisfied`
    - `creditsSatisfied = earnedCredits >= rule.requiredCredits`
    - `areasSatisfied = completedAreas.size() >= rule.requiredAreasCnt`
- 제외 영역 (EC-019): `findAllByAdmissionYearAndDeptGroup` — **여러 개 가능** (이전 박제 1개 제한 오류 수정)
- completedAreas 추출:
    - 사용자의 BALANCE_REQUIRED 이수 과목 중 `selectedArea` 필드를 `BalanceRequiredArea.fromSelectedArea` 로 변환
    - 제외 영역에 속한 것은 카운트에서 제외 (단, *학점은 합산됨* — 확인 필요)

### 4.4 정책적 의미

- 균형교양은 *학점 vs 영역 수* 두 축으로 만족 판정. 학점만 채워도 영역 수 부족하면 미충족
- 균필 부재 학과·학번 (18-21학번, 체육학과 등) 은 결과에 카테고리 자체가 없음 — UI 표시 정책 필요
- ACADEMIC_BASIC 의 학과 지정과목 검증 (`AcademicBasicPolicy`) 으로 학과별 인정 차이 반영

---

## 5. 인증제 검사 (`CertificationChecker.checkAndUpdate`)

상세 내용은 **[certification-policy.md](certification-policy.md)** 참조. 여기서는 요약만.

### 5.1 흐름 요약

1. `applyAltCourse` — 영어·코딩·고전 3종 대체 과목 정책 적용 (자동 통과 처리)
2. `findByUserId` 로 결과 재조회 → `CertResult.from()` 반환

### 5.2 핵심 정책 (PR #269, #277)

- **대체 과목 정책** (인증 종류별 다름):
    - 영어: 단일 alt_curi_no, 학점 인정 여부만
    - 코딩: 전공자 alt1, 비전공자 alt1 OR alt2 (등급 비교)
    - 고전독서: 과목 기반 대체 없음 (권수 기준)
- **면제 처리**: `EXEMPT` 분기에서 검사 skip. 응답은 `isRequired=false` (EC-014)
- **주전공 기준 적용** (EC-013): 복수전공의 인증 기준은 무시
- **대체 과목 우선순위** (EC-015 — **해결됨**, PR #280): 로그인 시 대체 과목 우선 적용 → 미충족 영역만 크롤링.
  검사 시점 `applyAltCourse` 는 시험 통과 여부와 무관하게 대체 정책을 재검사해 pass 만 추가 설정 (2026-07-12 코드 대조)

### 5.3 사전 조건

- `GraduationCheckCertResult.findByUserId(userId)` 가 *이미 존재*해야 함
- 없으면 `GRADUATION_CERT_NOT_FOUND` 예외 — **로그인 시 `AuthFacade` → `GraduationCertService.createOrUpdate` 가 생성** (2026-07-12 확인). 로그인 없이 검사가 호출되는 경로가 생기면 이 예외 발생

### 5.4 도메인 객체 mutability

- `GraduationCheckCertResult` 의 `passEnglish()`, `passCoding()`, `passClassic()`, `reCalculate()` 변이 메서드
- JPA dirty checking 으로 update 반영
- 단일 트랜잭션 안에서 영어·코딩·고전 일괄 처리 (PR #277 변경 — 부분 갱신 위험 제거)

---

## 6. 균형교양 검사 (BalanceRequiredResolver vs CategoryCreditCalculator)

**중요**: 균형교양은 *두 경로* 로 다뤄짐 — 헷갈리기 쉬움.

### 6.1 BalanceRequiredResolver (기준 데이터 조회 경로)

- 호출자: `NonMajorCategoryResolver#resolve` (PR #255 `/api/graduation/categories` 의 일부)
- 책임: 사용자에게 *어떤 균형교양 기준이 적용되는지* 응답 (검사 X)
- 흐름:
    1. `findExcludedRuleByAdmissionYearAndDeptCd(year, deptCd)` — 학과 제외 규칙
    2. fallback: `findRequiredRuleByAdmissionYearAndDeptCd(year, "0")` — **dept_cd="0"** (다른 ALL_DEPT 들과 같음)
    3. 둘 다 없으면 Optional.empty
- 제외 영역: 1개 (`findByAdmissionYearAndDeptGroup` 의 단일 조회)
- 반환: `GraduationCategoryResponse` (학점·영역 수 + 가능 과목 목록)

### 6.2 CategoryCreditCalculator#findBalanceRequiredRule (실제 검사 경로)

- 호출자: `GraduationChecker.calculate` (PR #254 `/api/graduation/check` 의 일부)
- 책임: 사용자 이수 과목 학점으로 균형교양 만족 여부 판정
- 흐름:
    1. `(admissionYear, user.deptNm)` 매칭
    2. fallback: `(admissionYear, "ALL")` — **dept_nm="ALL"** (BalanceResolver 와 다름!)
    3. 둘 다 없으면 null → early return
- 제외 영역: **여러 개 가능** (`findAllByAdmissionYearAndDeptGroup`)

### 6.3 두 경로의 차이 (검수 필요)

| 항목 | BalanceRequiredResolver | CategoryCreditCalculator |
|---|---|---|
| ALL_DEPT 표기 | `dept_cd="0"` | `dept_nm="ALL"` |
| 제외 영역 수 | 1개 (findByXxx) | 여러 개 (findAllByXxx) |
| 호출 경로 | 기준 조회 API | 검사 API |

**우려**:
- ALL_DEPT 표기 불일치 → 시트에 두 표기 다 있어야 작동 가능 (확인 필요)
- 제외 영역 수 불일치 → 기준 조회와 실제 검사 결과 어긋날 수 있음
- 분류 B (스키마 변경) 또는 분류 A (정책 결정) 후보

---

## 7. Missing Data 처리 패턴

### 7.1 Hard Fail (예외 → 전체 검사 실패)

| 누락 데이터 | 예외 코드 | 트리거 위치 |
|---|---|---|
| User | USER_NOT_FOUND | GraduationChecker, CertificationChecker, CategoryCreditCalculator |
| GraduationDepartmentInfo | DEPARTMENT_NOT_FOUND | CertificationChecker, CategoryCreditCalculator |
| GraduationCheckCertResult | GRADUATION_CERT_NOT_FOUND | CertificationChecker |
| CategoryType (raw 매칭 실패) | CATEGORY_TYPE_NOT_FOUND | CategoryType.fromRaw |

### 7.2 Soft Fail (빈 결과 반환)

| 누락 데이터 | 결과 |
|---|---|
| CreditCriterion (기준 행 없음) | 빈 카테고리 리스트 — *해당 카테고리가 결과에 안 나옴* |
| DoubleCreditCriterion (예외 페어 없음) | fallback (credit_criteria 의 DOUBLE+ALL_DEPT) |
| BalanceRequiredRule (학과+ALL 모두 없음) | null → BALANCE_REQUIRED 카테고리 누락 (EC-017) |
| 학과 RequiredCourse | 빈 목록 (영향: AcademicBasicPolicy false negative — EC-021) |
| BalanceRequiredAreaExclusion | 빈 set → 제외 영역 없이 모두 카운트 |

### 7.3 함정

- **Soft fail 은 검사 결과가 *조용히 잘못 나옴***. UI 사용자가 "왜 내 전공 카테고리가 안 나오지?" 라는 식으로 발견. 무음 장애 패턴
- Validator·loading 시 키 유일성·존재성 검증 누락이 이 무음 장애 원인
- **개선 후보**: soft fail 케이스를 로그·메트릭으로 가시화

---

## 8. 읽기 일관성 (Read Consistency)

### 8.1 단일 트랜잭션 안에서

- `checkGraduationRequirements` 의 전체 흐름이 단일 트랜잭션
- CompletedCourse 저장 → 같은 트랜잭션에서 calculate 가 read — 자기 자신의 쓰기 봄 (consistency 보장)
- DB 기준 데이터 (CreditCriterion 등) 은 calculate 중에 여러 번 read — 같은 트랜잭션 안이라 일관

### 8.2 sync vs check 동시 발생

- sync 호출 중에 사용자 check 가 시작되면:
    - MySQL REPEATABLE READ 기본 격리 수준에 의존
    - 사용자 트랜잭션은 시작 시점의 스냅샷을 봄
- 검사 중에 sync 가 시작되면:
    - 사용자 트랜잭션 commit 까지 sync 의 변경은 invisible
    - 사용자 트랜잭션이 길면 sync 가 락 대기

### 8.3 영속화된 결과의 읽기 일관성

- `GET /api/graduation/check` 는 단순 SELECT — 다른 트랜잭션과 무관
- 사용자가 검사 후 PATCH 로 영어 인증 변경 → 즉시 GET 에서 반영

---

## 9. 결과 저장·재계산

### 9.1 결과 저장

- `GraduationCheckPersistenceService.saveCheckResult(userId, checkResult)` — 사용자당 1행
- DB 에는 **보정 전 원본 값 저장** (전필 초과 학점 포함 — EC-012)
- 다음 검사 호출 시 덮어쓰기
- 히스토리 보존 없음

### 9.2 부분 재계산 (PATCH)

- `updateEnglishCertPass` 는 cert result 만 update → reCalculate
- 카테고리별 학점은 재계산 안 함

---

## 10. 응답 보정 정책 (`GraduationCheckResponseMapper`)

DB 저장값과 응답값이 다른 경우. 사용자에게 보이는 *졸업 가능 여부* 의 정확성을 위해 보정.

### 10.1 전필 초과 → 전선 이관 (EC-012)

- `adjustMajorCategories`: MajorScope (PRIMARY/SECONDARY) 별로 그룹핑 후
    - `overflow = max(0, MAJOR_REQUIRED.earned - MAJOR_REQUIRED.required)`
    - 응답 MAJOR_REQUIRED.earned = `min(earned, required)` (cap)
    - 응답 MAJOR_ELECTIVE.earned = `original earned + overflow` (이관)
- 함정: **DB 직접 확인 시 응답과 다른 값** — 어드민 도구 사용 시 혼란 가능
- 정책 의도: 학사 정책상 전필 초과분이 전선으로 인정. 사용자에게 정확한 졸업 가능 여부 표시

### 10.2 인증 면제 응답 (EC-014, PR #277)

- `EnglishTargetType.EXEMPT` 또는 `CodingTargetType.EXEMPT` 인 사용자
- 응답: `isRequired=false` (UI 카드 숨김), `passed` 는 DB 값 그대로 (이전: 무조건 true 보정 → 제거)
- 정책 의도: 프론트가 `isRequired` 로 표시·숨김 결정

### 10.3 인증 대상 타입 동적 조회 (PR #269 fix)

- 이전: 응답에서 `EnglishTargetType.NON_MAJOR` 고정값 사용 (버그)
- 현재: `GraduationDepartmentInfo` 에서 사용자 기반 동적 조회
- 검수 필요: 다른 응답 보정 로직도 유사 패턴 없는지

---

## 11. 카테고리 보정·매핑 정책

### 11.1 CategoryType 별칭 매핑 (EC-002)

- 성적표 raw "교필" → `CategoryType.COMMON_REQUIRED` 등
- 별칭 표는 ../glossary.md 참조

### 11.2 MAJOR_BASIC ↔ ACADEMIC_BASIC 학번 보정 (EC-001)

- `CategoryType.fromRaw(raw, admissionYear)` 가 학번 < 2024 면 "전기" → ACADEMIC_BASIC 변환
- 이전 별도 `MajorBasicPolicy` 클래스 (PR #278) 가 있었으나 fromRaw 로 통합·제거됨
- 18-19학번 "전공기초교양" 레거시 명 → 성적표에 "기교" 로 찍히므로 별도 처리 불필요

### 11.3 ACADEMIC_BASIC 의 학과 인정 검증 (EC-021)

- `AcademicBasicPolicy#isRecentMajorAcademicBasic` 으로 카테고리 외 추가 검증
- 학과 RequiredCourse 목록과 매칭 안 되면 학점 합산 제외
- CourseEquivalence 매핑으로 2차 검증

### 11.4 DEPRECATED 과목 매핑 (EC-011)

- `RequiredCourse.curiNo = "DEPRECATED"` 인 행 → `RequiredCourseResolver` 가 CourseReplacement 로 치환
- NPE 방어 (PR #255): 매핑 누락 시 원본 그대로 + 로그

---

## 12. 활용 패턴 정리

| 시나리오 | 정책 |
|---|---|
| 학기 시작, 시트·DB sync 됨 | 사용자는 명시적으로 POST 재호출해야 최신 기준 반영 |
| 같은 엑셀 두 번 업로드 | 새 결과로 덮어씀, 이전 결과 손실 |
| 영어 인증 통과 표시 | PATCH 한 번이면 됨 (전체 재검사 불필요) |
| 학과 변경 | User 갱신 후 사용자가 다시 POST 해야 결과 변경 |
| sync 중에 검사 | 트랜잭션 격리로 보호, 다만 결과 stale 가능 |
| sync 후 자동 무효화 | **없음** — 무음 장애 가능. 개선 후보 |
| 18-21학번 검사 | 균필 카테고리 자체가 결과에 없음 (EC-017) |
| 면제 학과 인증 | 응답 isRequired=false (EC-014) |
| 복수전공 학생의 인증 | 주전공 기준만 적용 (EC-013) |
| 전필 초과 학점 | DB 원본 + 응답 시 전선 이관 보정 (EC-012) |

---

## 13. 알려진 결함·개선 후보

### 분류 D (버그 수정 후보)

- DU-001: 균형교양 검사의 `findBalanceRequiredRule` 와 `BalanceRequiredResolver` 가 *다른 ALL_DEPT 표기* 사용 (EC-020). 시트 적재 시 두 표기 다 박제 필요한지 확인
- DU-002: ~~생성 시점·트리거 확인 필요~~ → 확인됨: `AuthFacade` 로그인 플로우가 생성 (§5.3). 로그인 우회 검사 경로 신설 시에만 재점검
- DU-003: 코딩 alt 학수번호 비교 시 Excel 파싱이 leading zero 손실 (EC-016)
- DU-004: `matchesMajorScope` 가 course.majorScope=null 인 경우 PRIMARY 로 자동 간주 → SECONDARY 카테고리 누락 가능
- DU-005: ~~BalanceRequiredArea enum 명칭 불일치 ("자연과학" ≠ 편람 "자연과과학") → 이수영역 카운트 누락~~ (EC-023, QA B1 19건 — **2026-07-12 해결**: enum name 편람 표기로 수정)

### 분류 B (스키마·구조 변경 후보)

- SU-001: 검사 결과 히스토리 보존 미적용
- SU-002: sync 후 사용자 결과 자동 무효화·재계산 메커니즘 없음
- SU-003: ALL_DEPT 표기 통일 (`"0"` vs `"ALL"`)
- SU-004: 균형교양 제외 영역 조회 통일 (BalanceResolver 1개 vs CategoryCalculator 여러 개)
- SU-005: 18-21학번 균필 부재 vs 면제 학과의 응답 표현 통일 (EC-014, EC-017)
- SU-006: **exclusions 의 dept_group(단과대학) 단위 스키마 → 창의소프트학부 특례 표현 불가 (EC-024, QA 2-6)**

### 분류 C (리팩토링 후보)

- RU-001: ALL_DEPT 상수 4군데 중복 (EC-004)
- RU-002: SINGLE/DOUBLE 분기 중복 (GraduationChecker.resolveCreditCriteria, MajorCategoryResolver.resolve)
- RU-003: 전공·비전공 정책을 Policy 묶음 클래스로 정리 (PR #278 boyekim 제안 — calculator 의 stream filter 단순화)

### 분류 A (정책 결정 필요)

- PU-001: sync 후 stale 결과 정책 (자동 무효화? 사용자 알림? 무시?)
- PU-002: 같은 사용자 동시 POST 호출 시 정책
- PU-003: Soft fail 케이스의 가시화 정책 (로그·메트릭·UI)
- ~~PU-004: 대체 과목 우선순위 (EC-015)~~ — 해결됨 (PR #280, 로그인 시 대체 우선. §5.2)
- PU-005: 18-21학번 균필 vs 면제 학과 응답 표현 통일 정책

---

## 14. 검수 상태

- 2026-07-12: §5(EC-015 현행화)·§5.3(cert result 생성처 확인)·§13(DU-005·SU-006 추가) 코드 대조 갱신

- [x] 호출 시점 5종 (POST/GET/PATCH check + categories + admin): 코드 일치
- [x] 검사 파이프라인 4단계: 코드 일치
- [x] 단일 `@Transactional` + 중첩 `@Transactional` join: 코드 일치
- [x] `CertificationChecker.applyAltCourse` 흐름: 코드 + PR #269/#277 일치
- [x] `BalanceRequiredResolver` vs `CategoryCreditCalculator` 균형교양 분기: 코드 확정
- [x] CategoryCreditCalculator 내부 (1·2·3단계, AcademicBasicPolicy 추가 검증): 코드 일치
- [x] Hard fail / Soft fail 패턴: 코드 일치
- [x] 응답 보정 (전필→전선, 인증 면제 isRequired): PR #254/#277 일치
- [x] 카테고리 매핑·보정 정책: PR #278 + 코드 일치
- [ ] **`GraduationCheckCertResult` 의 첫 생성 시점**: 코드 확인 필요
- [ ] **결과 저장의 upsert 방식**: `GraduationCheckPersistenceService` 미열람
- [ ] **`EnglishTargetType`, `CodingTargetType` 전체 enum 값**: 코드 미열람
- [ ] **sync 후 무효화 정책**: 운영 결정 필요
- [ ] **18-21학번 vs 면제 학과 응답 통일**: 정책 결정 필요
