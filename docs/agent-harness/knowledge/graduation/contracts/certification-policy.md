# 졸업 인증제 정책 통합 문서

> **경계**: 이 문서는 인증제의 **코드 집행 계약** (enum·정책 클래스·검사 흐름) 만 담는다.
> 인증 **값** (TOEIC 800, 고전 4영역 10권, "3개 중 2개" 등) 의 정본은
> `graduation-wiki cohort/<학번>/certification.md` (편람 인용 검수 완료) — 값 인용 시 태그 포인터로.

영어·코딩·고전독서 3종 인증제의 통합 정책. 대상 분류·면제·대체 과목·우선순위·응답 보정.

마지막 갱신: 2026-05-18

근거 PR: #254 (TSK-53 검사 구현), #269 (TSK-56-110 영어 대체과목), #277 (TSK-56-128 코딩 대체과목)
관련 코드: `domain/graduation/certification/*`, `CertificationChecker`, `CertificationPolicyConfig`

---

## 1. 인증제 3종 개요

| 인증 | enum 값 | 기준 데이터 | 대상 분류 enum | 면제 가능 | 대체 과목 |
|---|---|---|---|---|---|
| 영어 | `CERT_ENGLISH` | `english_cert_criteria` | `EnglishTargetType` | O | alt_curi_no 1개 |
| 코딩 | `CERT_CODING` | `coding_cert_criteria` | `CodingTargetType` | O | alt1, alt2 (전공자/비전공자 차이) |
| 고전독서 | `CERT_CLASSIC` | `classic_cert_criteria` | (사용자 분류 없음) | O (학번별) | (해당 없음 — 권수 기준) |

각 인증의 적용 여부는 `graduation_cert_rules` 시트 (`GraduationCertRule.graduationCertRuleType`) 로 학번별 결정.

---

## 2. 대상 분류 (`EnglishTargetType`, `CodingTargetType`)

학과별로 적용되는 인증 기준이 다름. `graduation_department_info` 시트의 `english_target_type`, `coding_target_type` 컬럼에 학과·학번별로 박제됨.

### 2.1 EnglishTargetType (영어 인증 대상 분류)

| 값 | 의미 |
|---|---|
| `NON_MAJOR` | 비전공 (일반 영어 인증 대상) |
| (전공별 값들) | (확인 필요 — enum 코드 미열람) |
| `EXEMPT` | **면제 — 영어 인증 불필요** |

### 2.2 CodingTargetType (코딩 인증 대상 분류)

| 값 | 의미 | alt1 | alt2 |
|---|---|---|---|
| `CODING_MAJOR` | 코딩 전공자 | 고급C프로그래밍실습 B0 이상 | (해당 없음) |
| `NON_MAJOR` | 비전공자 | 고급C 또는 K-MOOC | 둘 중 하나 충족 |
| `EXEMPT` | **면제 — 코딩 인증 불필요** | — | — |

(다른 값이 있을 수 있음 — 코드 enum 미열람)

### 2.3 정책 결정

- 대상 분류는 *학과·학번별 매핑 데이터* (graduation_department_info 시트) 에 기반
- 면제 학과·학번은 시트 적재 시점에 `EXEMPT` 명시
- 시트에 매핑 없으면 → `findByAdmissionYearAndDeptCd` 가 `DEPARTMENT_NOT_FOUND` 예외 (hard fail)

---

## 3. 대체 과목 정책 (영어·코딩)

### 3.1 영어 (`EnglishAltCoursePolicy`)

- 데이터: `english_cert_criteria.alt_curi_no` (1개 학수번호)
- 대표 과목 예: Intensive English
- 판정: 사용자 이수 과목 중 `alt_curi_no` 가 있고 학점 인정 (`isCreditEarned`) 됐으면 통과
- 학점 기준 없음 (수강 사실만 확인)

### 3.2 코딩 (`CodingAltCoursePolicy`)

- 데이터: `coding_cert_criteria` 의 `alt1_curi_no`, `alt1_min_grade`, `alt2_curi_no`, `alt2_min_grade`
- 대표 과목:
    - alt1: 고급C프로그래밍실습 (학수번호 - 시트 확인 필요)
    - alt2: K-MOOC:코딩과스토리텔링
- 등급 기준: `CodingAltCourseGradeRequirement` enum 으로 비교
    - A+ (4.5), A (4.0), A- (3.7) ... D0 (1.0), F (0), FA (0)
    - P (취득), NP (미취득) — P/NP 과목은 별도 처리
- 판정 (전공자 vs 비전공자 분기):
    - `CODING_MAJOR`: alt1 만 검사 → 학수번호 일치 AND grade >= alt1_min_grade
    - `NON_MAJOR`: alt1 OR alt2 → 둘 중 하나라도 충족

### 3.3 공통 인터페이스

```java
public interface GraduationCertificationAltCoursePolicy {
    boolean isSatisfiedByAltCourse(
        User user,
        GraduationDepartmentInfo userDept,
        List<CompletedCourse> earnedCourses
    );
}
```

- 구현체: `EnglishAltCoursePolicy`, `CodingAltCoursePolicy`
- 별도: `ClassicAltCoursePolicy` (`isSatisfiedByAltCourse(user)` 시그니처 다름 — 인증제 분류 다름)
- 빈 관리: `CertificationPolicyConfig` 에서 명시적 Bean 정의

### 3.4 대체 과목 우선순위 (EC-015 — 해결됨, PR #280)

- **현행 코드** (2026-07-12 대조): `CertificationChecker#applyAltCourse` 에 early return **없음** —
  시험 통과 여부와 무관하게 3종 대체 정책을 항상 검사하고, 충족 시 `passXxx()` + `reCalculate()`.
- **로그인 시점**: `GraduationCertResolver` 가 대체 과목 우선 적용 → 미충족 영역만 크롤링 (안정 신호 우선).
- **정책 의도 실현** (PR #277 결정 → PR #280 구현): 대체 과목 = 안정 신호 > 외부 시스템(휘발성).
- **결정 박제**: `../decisions/2026-02-24-alt-course-priority.md` (구현 완료 추기됨)

---

## 4. 면제 정책 (`EXEMPT`)

### 4.1 적용 시점

- `CertificationChecker#applyAltCourse` 의 각 정책 호출 시 `EXEMPT` 분기 처리:

```java
if (CodingTargetType.EXEMPT.equals(codingTargetType)) {
    return;  // 대체 과목 검사 안 함
}
```

### 4.2 응답 보정 정책 (PR #277 결정)

- **이전**: 면제 대상이면 응답에 `passed=true` 무조건 보정 (대휴칼 결과 무시)
- **현재** (PR #277 변경): 보정 로직 제거
    - `passed` = DB 값 그대로
    - **`isRequired` = false** 로 응답 (UI 카드 숨김 처리용)
- **이유**: 프론트가 `isRequired` 기준으로 카드 표시 결정. 백엔드는 단일 책임만

### 4.3 18-21학번 균필 부재와의 차이

- 18-21학번 균필 부재 (EC-017): 카테고리 자체가 결과에서 누락
- 면제 대상 인증 (EC-014): 카테고리 응답에 포함되되 `isRequired=false`
- *둘이 표현 방식이 다름* — UI 표시 정책 통일 필요 가능성

---

## 5. 영어·코딩 인증은 주전공 기준 (EC-013)

- `CertificationChecker#applyAltCourse` 에서 `GraduationDepartmentInfo` 조회 시 사용자 *주전공* (`user.getDeptCd()`) 만 사용
- 복수전공 학과의 인증 기준은 무시
- **정책 의도** (PR #254): 학사 정책상 인증은 주전공 따라감
- 복수전공의 인증 기준이 다른 경우 사용자 혼란 가능 — UI 표시 정책 합의 필요

---

## 6. 고전독서 인증 (`ClassicAltCoursePolicy`)

영어·코딩과 달리 *과목* 이 아니라 *권수* 기준.

### 6.1 기준 데이터

- 시트: `classic_cert_criteria`
- 컬럼: `total_required_count`, `required_count_western`, `required_count_eastern`, `required_count_eastern_and_western`, `required_count_science`
- 학번별로 1행

### 6.2 인증권수 의미 (EC-022)

- **인증권수 ≠ 기준 권수**, **인증권수 ≠ 이수 권수**
- 실제 의미: `인증권수 = min(필요 권수, 나의 이수 권수)`
- 통과한 사용자: 인증권수 == 기준 권수
- 미통과한 사용자: 인증권수 < 기준 권수

### 6.3 현행 처리 (PR #254 fix)

- 기준 권수는 대휴칼 파싱 X → `classic_cert_criteria` DB 에서 조회
- 이수 권수만 대휴칼 파싱
- 통과 여부는 학교 측이 표시한 값 사용 (계산 X)

### 6.4 코드

- `GraduationClassicsCertFetcher` — 대휴칼 페이지 파싱
- `ClassicsArea` — 영역 enum (WESTERN_HISTORY_THOUGHT 등)
- `ClassicAltCoursePolicy#isSatisfiedByAltCourse(user)` — *과목 기반 대체 없음*. 영어/코딩과 시그니처 다름

---

## 7. `CertificationChecker` 흐름

```
checkAndUpdate(userId, earnedCourses)  @Transactional
 ├─ applyAltCourse:
 │      ├─ User, GraduationDepartmentInfo, GraduationCheckCertResult 조회 (hard fail)
 │      ├─ englishAltCoursePolicy.isSatisfiedByAltCourse(user, userDept, earned) → certResult.passEnglish()
 │      ├─ codingAltCoursePolicy.isSatisfiedByAltCourse(user, userDept, earned) → certResult.passCoding()
 │      ├─ classicAltCoursePolicy.isSatisfiedByAltCourse(user) → certResult.passClassic()
 │      └─ if (isChanged) certResult.reCalculate()
 │
 └─ GraduationCheckCertResult 재조회 → CertResult.from() 반환
```

### 7.1 트랜잭션

- 클래스 기본: `@Transactional(readOnly=true)` (PR #277)
- `checkAndUpdate` 메서드: `@Transactional` (쓰기) — 외부 트랜잭션 (GraduationCheckService) 에 join

### 7.2 갱신 누락 방지

- `isChanged` 플래그로 변경 시에만 `reCalculate` 호출
- 단일 트랜잭션 안에서 영어·코딩·고전 모두 일괄 처리 → 부분 갱신 위험 없음 (PR #277 boyekim 코멘트 반영)

---

## 8. PATCH `/api/graduation/check/certifications/english`

- 사용자가 수동으로 영어 인증 통과 표시
- `GraduationCheckService#updateEnglishCertPass`:
    1. `GraduationCheckCertResult` 조회 (hard fail)
    2. `updateEnglish(isPassed)` 호출 (mutable 메서드)
    3. `reCalculate()` 호출 — 영어 변경분 반영
- **부분 재계산만** — 학점·다른 카테고리는 영향 없음
- 사용 시나리오: 대휴칼 인증 결과가 stale 한데 실제로는 통과한 경우 사용자가 수동 보정

---

## 9. 알려진 결함·개선 후보

### 분류 D (버그 수정)

- CF-001: 코딩 alt 학수번호 비교 시 Excel 파싱이 leading zero 손실 (`006844` → `6844`) 가능성 — `course.curiNo()` 정규화 필요 (chatgpt-codex 코멘트 PR #277)
- ~~CF-002: 대체 과목 우선순위 변경 (EC-015)~~ — 해결됨 (PR #280, §3.4)

### 분류 B (스키마 변경)

- ST-001: 면제 대상의 응답 표현 vs 18-21학번 부재의 응답 표현 통일 (EC-014, EC-017)

### 분류 C (리팩토링)

- RF-001: 영어/코딩/고전 정책의 시그니처 통일 (`ClassicAltCoursePolicy` 만 다름) — 인터페이스 일원화 검토

### 분류 A (정책 결정)

- PD-001: 복수전공 학생의 인증 기준 응답 정책 — 주전공 기준만 표시 vs 복수전공도 표시
- PD-002: `EXEMPT` enum 값 vs 18-21학번 부재의 사용자 표현 통일

---

## 10. 검수 상태

- [x] 인증 3종 개요: 코드 일치
- [x] 대체 과목 정책: 코드 + PR #269, #277 일치
- [x] 면제 응답 보정: 코드 + PR #277 일치
- [x] 주전공 기준: 코드 + PR #254 일치
- [x] 고전독서 인증권수 의미: PR #254 일치
- [x] CertificationChecker 흐름: 코드 일치 (PR #277 변경 반영)
- [ ] **EnglishTargetType / CodingTargetType 전체 enum 값**: 코드 미열람
- [ ] **alt1_curi_no, alt2_curi_no 실제 학수번호**: 시트 cell 확인 필요
- [ ] **18-21학번 면제 정책 표현 통일**: ADR 필요
- [ ] **PATCH english 외에 coding/classic 도 수동 보정 가능한지**: 코드 확인 필요
