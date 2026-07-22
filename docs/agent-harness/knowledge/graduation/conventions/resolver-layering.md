# Resolver 계층 규칙 (service → resolver → repository)

졸업요건 조회·검사 코드를 짤 때 **반드시 따라야 하는 계층 규칙**. 신규 피쳐·버그 수정 시 이 문서를 먼저 본다.

마지막 갱신: 2026-06-23 (코드 기준: `domain/graduation/**`)
관련 결정: `../decisions/2026-02-16-resolver-separation.md`

---

## 1. 왜 이 계층이 필요한가 (배경)

졸업요건 기준 데이터는 **사람이 수강편람을 읽고 임의 규칙으로 구글시트에 적재**한 것이다. 그래서 "그냥 repository 로 조회"하면 안 되는 **숨은 규칙**이 데이터에 깔려 있다:

- `ALL_DEPT = "0"` 매직값 fallback (학과 무관 공통)
- 복수전공 학과 페어 3단계 fallback
- 균형교양은 `dept_nm = "ALL"` 단일 행 fallback
- 계약학과 학점 보정, dept_nm 직매칭(rename 주의), enabled 플래그…

개발자가 repository 를 직접 호출하면 **이 규칙들을 빠뜨리기 쉽다**(= 졸업 가능 여부 오판). 그래서 규칙을 캡슐화한 **resolver 계층**을 두고, **service 는 repository 를 직접 쓰지 않고 resolver 를 거친다.**

---

## 2. 절대 규칙

```
service  →  resolver  →  repository
(조회 규칙은 resolver 안에만. service 에서 repository 직접 호출 금지)
```

- **졸업요건 기준을 조회하는 모든 쿼리는 resolver 를 경유**한다.
- **새 조회 규칙(fallback·예외·보정)이 생기면 resolver 에 추가**한다 (service 에 if 분기 흩뿌리지 않는다).
- repository 는 *단순 조회 메서드*만 노출. 규칙(우선순위·fallback)은 repository 가 아니라 resolver 책임.

---

## 3. 현재 적용 상태 (정직하게 — 통일은 진행 중)

| 경로 | 진입 | resolver 경유? | 비고 |
|---|---|---|---|
| **기준 조회** `/api/graduation/categories` | `GraduationCategoryService` | resolver 경유 (O) | 모범 사례 |
| **검사** `/api/graduation/check` | `GraduationChecker`, `CategoryCreditCalculator` | repository 직접 (X) | **리팩토링 대상** |

→ 즉 규칙은 정해졌고 조회 경로는 따르지만, **검사 경로는 아직 repository 를 직접 호출**한다:
- `GraduationChecker#resolveCreditCriteria` 가 `creditCriterionRepository`·`doubleCreditCriterionRepository` 직접 호출 + SINGLE/DOUBLE 분기·예외 매핑을 service 안에서 수행.
- `CategoryCreditCalculator` 가 `balanceRequiredRuleRepository`·`graduationDepartmentInfoRepository`·`balanceRequiredAreaExclusionRepository` 직접 호출.

**이 SINGLE/DOUBLE 분기·균형교양 fallback 이 조회 경로 resolver 와 중복**이다 (ADR RU-002). 검사 경로를 resolver 로 통일하는 게 목표.

> 신규/수정 작업 시: 검사 경로를 건드리면 **repository 직접 호출을 resolver 로 빼는 방향**으로. 새로 repository 직접 호출을 늘리지 말 것.

---

## 4. Resolver 카탈로그 (각자 무슨 규칙을 캡슐화하나)

| Resolver | 책임 (캡슐화한 규칙) | 핵심 메서드 | 관련 |
|---|---|---|---|
| `MajorCategoryResolver` | 전공 카테고리 조회. SINGLE/DOUBLE 분기. **전공 과목은 시트가 아니라 Subject(크롤러)에서** (`loadMajorSubjects`) | `resolve`, `resolveSingleType`, `resolveDoubleType` | EC-010, ../map/data-dependency-map.md |
| `NonMajorCategoryResolver` | 교양 카테고리 조회. 와일드카드(ALL_DEPT) + 학과 override 머지 | `loadRequiredCourses`(courseKey override) | EC-010 |
| `BalanceRequiredResolver` | 균형교양 기준 조회 (학과별 룰 + 영역 제외) | — | EC-019, EC-020 |
| `DoubleCreditCriterionResolver` | 복수전공 학과 페어 **3단계 fallback** | `resolve` (pair → primary+ALL → ALL+secondary) | EC-003, EC-004 |
| `RequiredCourseResolver` | DEPRECATED 과목 → 현행 과목 치환, group_code 매칭 | `findRequiredCourseNames`, `findRequiredCourseInGroup` | EC-011 |
| `AcademicBasicPolicy` (policy) | 기필(ACADEMIC_BASIC) 과목의 학과 인정 추가검증 | `isRecentMajorAcademicBasic` | EC-021 |
| `GraduationCertResolver` (check/cert) | 로그인 시 인증 판정 (대체과목 우선) | — | EC-015 |

> `*Policy` 도 넓게 보면 resolver 류 — "규칙을 캡슐화해 service 가 직접 판단 안 하게" 하는 같은 의도.

### DoubleCreditCriterionResolver 3단계 (대표 예시 — 절대 빠뜨리면 안 되는 fallback)

```
1. (primaryDeptCd, secondaryDeptCd) 정확 매칭
2. (primaryDeptCd, ALL_DEPT="0")
3. (ALL_DEPT="0", secondaryDeptCd)
→ 셋 다 실패 시 빈 리스트 → 상위에서 credit_criteria(DOUBLE, ALL_DEPT) 로 fallback
```

repository 직접 호출은 이 우선순위를 모른 채 1번만 보고 끝낼 위험이 크다. → 반드시 resolver 경유.

---

## 5. 신규 작업 체크리스트

- [ ] 졸업요건 기준을 조회하나? → **resolver 를 거치는가?** (service 에서 repository 직접 호출 금지)
- [ ] 새 조회 규칙(fallback·예외·보정)을 추가하나? → **resolver 안에** 넣었는가? (service if 분기 금지)
- [ ] 검사 경로를 건드리나? → repository 직접 호출을 **resolver 로 빼는 방향**인가? (늘리지 말 것)
- [ ] 전공 과목 목록이 필요하나? → 시트 아님. **Subject(크롤러)** 경유 (../map/data-dependency-map.md)

---

## 검수 상태

- [x] resolver 분리 결정: ../decisions/2026-02-16 일치
- [x] 조회 경로 resolver 경유 / 검사 경로 repository 직접: 코드(`GraduationChecker`, `CategoryCreditCalculator`, `GraduationCategoryService`) 일치
- [ ] 검사 경로 resolver 통일 리팩토링: **미완 (목표)** — RU-002
