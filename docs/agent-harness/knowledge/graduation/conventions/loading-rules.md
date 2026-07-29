# 적재 규칙 (사람이 수강편람 읽고 시트에 적재한 우리만의 규칙)

졸업요건 기준 데이터는 **사람이 수강편람을 보고 손으로 구글시트에 적재**한다. 그 과정에서 정한 *암묵적 규칙*들 — 코드가 이 규칙을 알고 있어야 조회/검사가 맞는다. 신규 피쳐·버그 수정·시트 검증 시 필독.

마지막 갱신: 2026-06-23 (시트 스냅샷 `raw/sheets/2026-06-23` + 코드 대조)
값의 정본: graduation-llm-wiki(cohort/) · 코드 분기: ../edge-cases.md

---

## 1. 매직값·fallback 규칙

| 규칙 | 내용 | 코드/EC |
|---|---|---|
| **ALL_DEPT = `"0"`** | 학과 무관 공통 기준. 대부분 시트의 `dept_cd="0"`. resolver fallback 트리거 | EC-004, resolver-layering.md |
| **균형교양만 `dept_nm="ALL"` (문자열)** | balance_required_rules 만 표기 다름. `credit_criteria` 에는 BALANCE_REQUIRED 행 없음(별도 시트) | EC-020, `CategoryCreditCalculator#findBalanceRequiredRule` |
| **복수전공 페어 fallback** | (primary,secondary) → (primary,ALL) → (ALL,secondary) → credit_criteria(DOUBLE,ALL) | EC-003, `DoubleCreditCriterionResolver` |

## 2. major_type / major_scope 적재 컨벤션

- `major_type`: `ALL`(교양 공통 카테고리) / `SINGLE`(단일전공) / `DOUBLE`(복수전공). **ALL 행도 dept_nm 별로 존재**(학과마다 한 줄).
- `major_scope`: `PRIMARY`(주전공) / `SECONDARY`(복수전공). **복수전공자는 PRIMARY+SECONDARY 두 행**이 함께 있어야 함.
- 공통/학문기초/전공기초/전체이수 카테고리는 보통 `major_type=ALL, major_scope=PRIMARY` 로 적재(시트 스냅샷 확인).

## 3. 학과명(dept_nm) 직매칭 규칙 — rename 주의

- 조회는 `(admission_year, dept_nm)` **직매칭**. `dept_cd` 로 안 한다.
- 학과명이 바뀌면(rename) **구·신 학과명 양쪽 행을 모두 적재**해야 lookup 이 안 끊긴다. (dept_cd 가 바뀌어도 dept_nm 기준)
- 예: 원자력공학과→양자원자력공학과, 국방시스템→국방AI융합시스템 등 (graduation-llm-wiki `department-rename-timeline`)
- 위험: 시트에 한쪽만 적재 시 해당 학번이 빈 결과 → ALL_DEPT fallback (오판 가능).

## 4. 계약학과 학점 보정

- 국방·항공 계약학과: 공통교양필수 **-1** / 전공기초 **+1** (일반 13/13 → 12/14).
- 시트에 일반 기본값으로 적재되면 **drift**(audit 2026-06-23 에서 5건 발견). 적재 시 반드시 보정값.
- 근거: graduation-wiki `reports/audits/2026-06-23-sheet-drift.md` §A (계약학과 5건) + `cohort/*/credit.md` 국방·항공 행 (공통 12 확인). (구 entities/contradictions 참조는 2026-07-12 위키 정리로 폐기됨)

## 5. 학번 세대(Schema)별 적재 차이

| 세대 | 학번 | 교양 자율 | MAJOR_BASIC |
|---|---|---|---|
| D | 18~19 | 중핵필수선택 21 (GENERAL_ELECTIVE) | 없음 |
| C | 20~21 | 교양선택(1영역) 21 (GENERAL_ELECTIVE) | 없음 |
| B | 22~23 | 균형교양 2영역 6 (BALANCE_REQUIRED) | **없음 (행 없어야 정상)** |
| A | 24~26 | 균형교양 3영역 9 | **15 등 (정식)** |

- `MAJOR_BASIC` 은 24학번부터. 코드가 `admissionYear<2024` 면 `ACADEMIC_BASIC` 으로 정규화(`CategoryType.fromRaw`). → 23학번 이하 MAJOR_BASIC 행 적재 금지.
- graduation-llm-wiki `credit-schema-generations`, `general-elective-era`, `category-major-basic`.

## 6. 과목·동일과목 적재 규칙

- **전공필수/전공선택 과목 목록은 시트가 아니다** → Subject(크롤러). `required_courses` 시트는 교양 지정과목·학문기초용. (../map/data-dependency-map.md)
- 동일/대체 과목: `course_equivalences` 시트의 `group_code`/`same_course_code` 로 인정. `RequiredCourseResolver`·`AcademicBasicPolicy` 가 사용 (EC-011, EC-021).
- DEPRECATED 과목: 현행 과목으로 치환.

## 7. 기타 컬럼 규칙

- `enabled` (TRUE/FALSE): FALSE 는 검사 제외 의도. (현재 코드 필터 여부 EC-007 — 확인 대상)
- `admission_year` vs `admission_year_short`: 양쪽 정합(`year % 100 == short`). 한쪽만 고치면 어긋남 (EC-008).
- 와일드카드 override(교양 지정과목): `dept_cd="0"` 기본 + 학과 행 override (required=true/false). `NonMajorCategoryResolver#loadRequiredCourses` courseKey (EC-010).
- `TOTAL_COMPLETION` ≠ 카테고리 합. credit 표는 필수성 카테고리만 담고, 나머지는 교양선택·자유선택 몫.

---

## 검수 상태

- [x] ALL_DEPT·균형 ALL·페어 fallback: 코드 + 시트 스냅샷 일치
- [x] 계약학과 보정·rename·세대 차이: audit 2026-06-23 + graduation-llm-wiki 일치
- [ ] enabled=FALSE 검사 필터 여부: 코드 확인 필요 (EC-007)
