# 졸업요건 Edge Case 카탈로그

코드 분기·매직 값·학번 보정으로 표현된 특수 케이스의 단일 소스. 발견 시마다 EC-XXX 누적.

EC 번호는 한 번 부여되면 폐기되어도 재사용 금지. 폐기된 케이스는 `상태: 폐기` 한 줄 추가.

마지막 갱신: 2026-06-23 (EC-001 24학번 origin 확정 + EC-009 dept_nm 변경 시트 운영 패턴 데이터 10건 박제 (7건 자동 탐지 + 3건 cohort intro 추가) + 부수 발견 2건 — graduation-wiki@v2026-1 의 reports/2026-06-23-sheet-drift.md + tools/department_drift.py + cohort/<학번>/department.md)

---

## EC-001: MAJOR_BASIC 학번 보정 (양방향 정규화)

- **조건**: 성적표 raw 카테고리 `[전기]` (MAJOR_BASIC alias) 가 학번 < 2024 사용자의 행에 등장
- **현행 처리**: `CategoryType.fromRaw(raw, admissionYear)` 가 학번 < 2024 면 `ACADEMIC_BASIC` 로 변환
- **이력**:
    - 18~19학번: 학문기초교양의 레거시 명 = "전공기초교양" → 성적표에 `[기교]` 로 찍힘
    - 20~23학번 단일 전공: 성적표에 `[기필]` 로 찍힘
    - 20~23학번 복수 전공: 성적표에 `[전기]` 섞여 표기 (PR #278 발견)
- **정책 출처** (24학번 신설 origin 확정):
    - 1차 출처 — 개편 시행 안내: `graduation-wiki@v2026-1 changelog/2026-1.md` §1-다 "학문기초교양필수·전공기초 개편" (수강편람 2026 p.4 §1-다 — 2024-1부터 시행, 일반물리/화학/생물/공업수학 학문기초→전공기초 변경, 기초통계학·기초천문학 등 전공기초 신설)
    - 1차 출처 — 24학번 표에 컬럼 신규 등장: `graduation-wiki@v2026-1 cohort/2024/credit.md` §3 단일전공 표 (수강편람 2026 p.42, p.45 — 2024학번 표 Schema A 의 `전공기초(A)` 컬럼이 비-0 값으로 등장)
    - 1차 출처 — 23학번 대조 (직전 cohort 표에 컬럼 없음): `graduation-wiki@v2026-1 cohort/2023/credit.md` §3 단일전공 표 (수강편람 2026 p.46, p.50 — Schema B 는 `전공학점계(A+B)` 통합 열, 전공기초 별도 컬럼 부재)
    - 보조: `graduation-wiki@v2026-1 cohort/2026/credit.md` §CategoryType매핑 (수강편람 2026 p.37 — 2026학번 정식 적용 재확인)
- **코드 흔적**: 현행 `CategoryType#normalizeMajorBasic` (학번<2024 면 `ACADEMIC_BASIC` 변환). 폐기 `MajorBasicPolicy` (decisions/2026-02-21)
- **검수 상태**: ✓ **확정** — 24학번 origin 페이지 = 수강편람 2026 p.4 §1-다 (개편 안내) + p.42/p.45 (24학번 표 Schema A). 23학번 표(p.50)와의 스키마 차이가 데이터로 boundary 증명. 코드 분기(학번<2024) 와 일치.

## EC-002: CategoryType 별칭 매핑

- **조건**: 시트·성적표 엑셀의 raw 문자열 → enum 변환
- **별칭**: COMMON_REQUIRED (교필, 공필), MAJOR_REQUIRED (전필, 복필), MAJOR_ELECTIVE (전선, 복선), GENERAL_ELECTIVE (교선, 교선1, 교선2), ACADEMIC_BASIC (기교, 기필), MAJOR_BASIC (전기)
- **함정**: 복필/복선이 MAJOR_REQUIRED/MAJOR_ELECTIVE 로 흡수 — 어느 전공의 필수인지는 categoryType만 봐서는 불명
- **검수 상태**: 코드 일치

## EC-003: DoubleCreditCriterion 3단계 fallback

- **조건**: `MajorType.DOUBLE` 학생의 학과 페어 예외 기준 조회
- **처리**: `DoubleCreditCriterionResolver.resolve` 3단계
    1. (primaryDeptCd, secondaryDeptCd) 양쪽 매칭
    2. (primaryDeptCd, ALL_DEPT="0")
    3. (ALL_DEPT="0", secondaryDeptCd)
    4. 모두 실패 시 → `MajorCategoryResolver` fallback
- **정책 의도**: 학과 조합 예외 (건축학과, 법학과 등) 우선
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-004: ALL_DEPT 매직 값 (`"0"` 또는 `"ALL"`)

- **두 가지 표현**:
    - 대부분 시트: `dept_cd = "0"`
    - **balance_required_rules**: `dept_nm = "ALL"`
- **코드 위치 (4개 중복)**: `MajorCategoryResolver`, `NonMajorCategoryResolver`, `DoubleCreditCriterionResolver`, `BalanceRequiredResolver`
- **함정**: balance_required_rules 만 표기가 다름 (분류 B 후보)
- **검수 상태**: 코드 일치, 통일 미정

## EC-005: applyExceptionCriteria 의 leftover 처리

- **조건**: `MajorType.DOUBLE` 학생의 학점 기준 결정 시
- **처리**: 기본 행 순회하며 예외 키로 override + **남은 예외 행은 새로 추가** (drop 안 함)
- **정책 의도**: override + 추가 — 예외 테이블이 두 역할 겸함
- **함정**: override 의도였는데 default 에 같은 키 없으면 추가됨
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-006: DOUBLE 전공 조회 시 deptNm 무관

- **조건**: `MajorType.DOUBLE` 학생의 기본 복수전공 기준 조회
- **처리**: `findByAdmissionYearAndMajorType(year, DOUBLE)` — deptNm/deptCd 필터 없음
- **정책 의도**: 복수전공자는 학과 무관 공통 기준 (dept_cd="0") 적용
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-007: enabled=FALSE 행 처리

- **상태**: PersistenceService 에서 필터링 없이 적재, Repository 조회 시도 필터 없음 → 검사 결과에 포함됨
- **개선 후보 (분류 D)**: 적재·조회 시 enabled=TRUE 필터
- **검수 상태**: 코드 일치, 운영 의도 확인 필요

## EC-008: admission_year vs admission_year_short 정합

- **조건**: 모든 시트의 두 컬럼
- **함정**: validator 정합 검증 안 함. 사람 적재 시 한쪽만 수정하면 어긋남
- **검수 상태**: 코드 일치, validator 보강 후보

## EC-009: dept_nm 변경 시 마이그레이션 — 시트 운영 = 새 dept_cd 분리 발급

- **조건**: 학과명 표기 변경 (학사 개편으로 rename 또는 단어 추가/축소/명칭 표준화)
- **코드 함정**: `CreditCriterionRepository.findByAdmissionYearAndDeptNmAndMajorTypeIn` 등 `dept_nm` 정확 매칭 의존. 직접 매칭 실패 시 빈 결과 → ALL_DEPT fallback(EC-004) 호출.
- **시트 운영 패턴 (확정, 2026-06-23 데이터 검증)**:
    - 학과명 변경 시 시트 운영자가 **새 `dept_cd` 를 별도 발급** (구 코드 재사용 X).
    - `graduation_department_info` + `credit_criteria` 양쪽에 `(학번, 구이름, 구cd)` 행과 `(학번, 신이름, 신cd)` 행을 **분리 적재**.
    - 결과: `(admission_year, dept_nm)` 기반 lookup 은 **안전** — 각 학번이 자기 시기의 이름으로 매칭됨.
    - **한계**: `dept_cd` 단독으로 학과 시간축 추적 불가 (논리적으로 동일한 학과의 rename 이력이 dept_cd 만으로는 끊김 — 별도 매핑 테이블 부재).
- **데이터 증거 (graduation-wiki@v2026-1 reports/2026-06-23-sheet-drift.md §B)**:

  | 학번 경계 | 변경 | 구 dept_cd | 신 dept_cd | 시트 적재 |
  |---|---|---|---|---|
  | 2019→2020 | 원자력공학과 → 양자원자력공학과 | 2785 | 2789 | 양쪽 OK |
  | 2021→2022 | 건축공학전공 → 건축공학과 | 2779 | 2720 | 양쪽 OK |
  | 2021→2022 | 건축학전공 → 건축학과 | 2780 | 2739 | 양쪽 OK |
  | 2022→2023 | 기계공학전공 → 기계공학과 | 2723 | 2725 | 양쪽 OK |
  | 2023→2024 | 데이터사이언스학과 → 인공지능데이터사이언스학과 | 3225 | 3516 | 양쪽 OK |
  | 2024→2025 | 소프트웨어학과 → 콘텐츠소프트웨어학과 | 3220/3515 | 3523 | 양쪽 OK |
  | 2025→2026 | 국방시스템공학과 → 국방AI융합시스템공학과 | 2784 | 2796 | 양쪽 OK |
  | 2022→2023 | 항공시스템공학과 → 항공시스템공학전공 | 2787 | 2793 | 양쪽 OK |
  | 2024→2025 | 법학전공 → 법학과 | 2052 | 2053 | 양쪽 OK |
  | 2025→2026 | 글로벌조리학과 → 조리서비스경영학과 | 3037 | 3038 | 양쪽 OK |

- **부수 발견 (2026-06-23 cohort intro 정독 + 시트 대조)**:
    - **법학부 (wiki) ≠ 법학전공 (시트)**: wiki 의 18~23 cohort intro (수강편람 2022-2 p.59, 2026 p.46/p.51/p.56/p.61/p.66) 가 "법학부" 로 표기하지만 시트 `graduation_department_info` 는 18~24학번 전부 `법학전공` (dept_cd=2052) 으로 통일 적재. 시트 운영자의 명칭 정리로 추정 — 학생 조회는 dept_nm 직매칭 기반이라 wiki 와 시트 매핑이 끊김 가능성. 별도 drift 후보.
    - **23학번 항공 명칭 미세 차이**: wiki 의 23 cohort intro (수강편람 2026 p.46 §1 졸업기준 row) 는 "항공시스템공학과" 표기, 시트는 23~26학번 "항공시스템공학전공" (dept_cd=2793) 으로 통일. cohort intro 졸업기준 row 와 단일전공 표 (p.50) 사이의 표기 미세 차이 — 시트 운영자가 단일전공 표 기준 적재.
- **출처**:
    - 1차 출처 (시트 적재 실측): `graduation-wiki@v2026-1 reports/2026-06-23-sheet-drift.md` §B (Excel `졸업요건검사 기준 데이터.xlsx` graduation_department_info + credit_criteria 대조)
    - 1차 출처 (자동 탐지 7건): `graduation-wiki@v2026-1 tools/department_drift.py` (PDF 9개 cohort 표 timeline 비교 → 7건 진성 rename)
    - 1차 출처 (추가 3건 — 24~26 cohort intro): `graduation-wiki@v2026-1 cohort/{2018~2025}/department.md` (cohort intro 정독으로 법학·항공·조리 변천 추가 발견)
    - 보조: 각 변경의 PDF 인용은 `cohort/<학번>/credit.md` + `cohort/<학번>/department.md`
- **코드 검토**:
    - 현행 `dept_nm` 직매칭 Repository 는 위 운영 패턴 하에서 안전 (학번별로 자기 시기의 이름 매칭).
    - **dept_cd 기반 학과 추적 신규 기능 추가 시** (예: "이 학생의 학과 변경 이력", "코드별 cohort 통계") → 별도 매핑 테이블 필요. 현재 미구비.
    - 향후 학사 학과 신규 변경 발생 시 동일 패턴 (새 dept_cd 발급 + 양쪽 적재) 준수해야 함.
- **검수 상태**: ✓ **확정** (2026-06-23 — 10건 데이터 박제) — 시트 운영 패턴 = 새 dept_cd 분리 발급. 현행 코드 lookup 안전. 마이그레이션 절차 = "wiki 의 cohort rename 탐지 → 시트 양쪽 행 적재 확인" 으로 닫힘. 부수 발견 2건 (법학부 통일, 23 항공 미세) 은 별도 drift 후보 — `reports/2026-06-23-sheet-drift.md` 후속 점검 항목.

## EC-010: courseKey 와일드카드 override 패턴 (교양 지정과목)

- **조건**: 교양 지정과목 (RequiredCourse) 로딩 시
- **처리** (`NonMajorCategoryResolver#loadRequiredCourses`):
    - 시트 적재: dept_cd="0" 와일드카드 행 + dept_cd=학과코드 학과별 행 공존
    - 머지: `courseKey = KeyUtils.generate(categoryType, curiNm)` 키로 LinkedHashMap 누적
        - 와일드카드: `putIfAbsent` (기본값으로만)
        - 학과 행: `put` (무조건 override)
    - 최종: required=true 만 필터링
- **정책 의도**:
    - A 패턴: 공통 + 학과별 면제
    - B 패턴: 학과별 적용 (공통 없음)
- **함정**: courseKey 가 curiNm 기반 — 학수번호 변경 시 같은 과목 인식 X → group_code (EC-011) 가 해결
- **검수 상태**: 코드 일치

## EC-011: DEPRECATED 과목 처리 — `course_replacements` 폐기, `course_equivalences + group_code` 로 마이그레이션

- **상태**: **구조 변경** (TSK-71, PR #310, 2026-03-30 머지)
- **이전 (폐기)**:
    - `RequiredCourse.curiNo = "DEPRECATED"` 행
    - `RequiredCourseResolver` 가 `CourseReplacement` 로 치환
    - 학번별 대체과목 관리 — 커버리지 낮음, 운영 리소스 큼
- **현행**:
    - `course_equivalences` 시트 + `group_code` 컬럼 도입
    - `required_courses` 에도 `group_code` 컬럼 추가
    - 모든 학번/학과 공통, 과목 자체 기준 통합 관리
    - 동일과목 ⊃ 대체과목 (동일과목이 상위 개념)
    - 인정 흐름: 사용자 이수 과목 → `course_equivalences` 에서 `group_code` 조회 → `required_courses` 의 같은 `group_code` 매칭 → 인정
- **최신 과목 판단**: `group_code` 동일 AND `required_courses.curi_no` 와 일치 (deprecated 안 됨) = 최신
- **정책 의도** (PR #310 확정):
    - 학번 단위 → 과목 자체 기준 관리로 커버리지 확대
    - 학사정보시스템 동일과목 CSV 기반 적재 → 운영 리소스 감소
    - 동일과목/대체과목 통합 — 동일과목 카테고리가 대체과목 카테고리 포함 (실제 동일과목이 더 넓음)
- **시트 영향**:
    - `course_replacements` 시트 폐기 (또는 미적재 유지)
    - `course_equivalences` 시트가 모든 동일/대체과목 관리
- **함정**:
    - 적재 순서 의존 없음 (PR #310 논의 참조 — 학사정보시스템 순서가 변경 이력대로 정렬 안 됨)
    - 일대다 대체는 같은 group_code 에 여러 행으로 자연스럽게 표현
- **결정 박제**: 없음 (decisions/2026-03-30-course-equivalences-group-code.md 는 미작성 — 근거는 PR #310. 2026-07-12 검수)
- **검수 상태**: 코드 일치 (PR #310), 정책 의도 확정

## EC-012: 전공 학점 재배분 (전필 → 전선 → 교양)

- **상태**: **확장** (TSK-70, PR #306, 2026-03-20 머지). 이전엔 전필→전선만 있었음
- **조건**: 사용자가 카테고리 requiredCredits 를 초과 이수
- **처리** (`GraduationCheckResponseMapper#adjustMajorCategories` 의 *체인 재배분*):
    - 1단계: `MAJOR_REQUIRED` 초과분 → `MAJOR_ELECTIVE` 로 이관
    - 2단계 (PR #306 추가): `MAJOR_ELECTIVE` 초과분 → `GENERAL` (교양) 으로 이관
- **재배분 대상 카테고리**: MAJOR_REQUIRED, MAJOR_ELECTIVE, GENERAL (3개)
- **fallback 카테고리 처리** (PR #306):
    - `findCategory` 가 카테고리 없으면 null 반환
    - 이월 학점 계산용으로 `createEmptyGraduationCategory()` 임시 객체 생성 (requiredCredits=0)
    - **실제 이월 학점이 생긴 경우에만** 결과 DTO 에 추가 (없으면 응답에서 제외)
- **함정**:
    - DB 에는 *원본* 학점 저장. 응답에서만 보정
    - fallback 카테고리 requiredCredits=0 — *실제 기준 0이라는 게 아니라 별도 조회 안 함*. CreditCriterion 기준학점 정확 비교는 미구현 (PR #306 boyekim 코멘트)
    - MajorScope (PRIMARY/SECONDARY) 별로 그룹핑 후 처리
- **정책 의도**: 학사 정책상 전필 초과분이 전선으로, 전선 초과분이 교양으로 인정
- **결정 박제**: 없음 (decisions/2026-03-20-major-overflow-to-general.md 는 미작성 — 근거는 PR #300. 2026-07-12 검수)
- **검수 상태**: 코드 일치 (PR #306), 정책 의도 확정. **CreditCriterion 기준학점 정확 비교는 미구현** (확장 후보)

## EC-013: 영어·코딩 인증은 주전공 기준

- **조건**: 복수전공 학생의 영어·코딩 인증 기준 결정
- **처리**: `GraduationDepartmentInfo` 를 주전공 (User.deptCd) 기준으로만 조회
- **정책 의도** (PR #254): 학사 정책상 인증은 주전공 따라감
- **코드**: `CertificationChecker#applyAltCourse`, `GraduationCertResolver` (로그인 시) 모두 같은 패턴
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-014: 면제 대상의 응답 보정 정책

- **조건**: `EnglishTargetType.EXEMPT` 또는 `CodingTargetType.EXEMPT`
- **처리** (PR #277):
    - `isRequired=false` 로 응답 (UI 카드 숨김)
    - `passed` 값은 DB 값 그대로
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-015: 영어·코딩 대체 과목 우선순위 — 해결됨 (TSK-56-133)

- **상태**: **해결됨** (PR #280, 2026-03-01 머지)
- **이전 (현행 X)**: 검사 시점 시험 통과 우선, 재로그인 시 외부 결과로 덮어쓰기 위험
- **현행**:
    - **로그인 시 대체 과목 우선 적용** → 미충족 영역만 선택적 크롤링 → DB 업데이트
    - 졸업요건검사 시에도 대체 과목 보정 한 번 더 (첫 사용 시나리오)
    - `GraduationCertResolver` 가 로그인 시 인증 판정 책임
- **정책 의도** (PR #280): 대체 과목 = 안정 신호. 외부 시스템 = 휘발성. 안정 신호 우선
- **결정 박제**: decisions/2026-02-24-alt-course-priority.md (변경 결정. 구현 = PR #280 — 별도 decision 파일은 없음)
- **연관**: `GraduationCert*Fetcher` 분리 구조, EC-013 (주전공 기준). ※ 구 문서가 참조하던 EC-024(Fetcher 분리)·EC-026(인증 시점 이동)은 미작성 번호였음 — 제거. 현재의 EC-024 는 별개 주제(exclusions 스키마)로 새로 부여됨 (2026-07-12 검수)
- **검수 상태**: 코드 일치 (PR #280), 정책 의도 확정

## EC-016: 코딩 대체 과목 — 전공자/비전공자별 차이

- **처리** (PR #277, `CodingAltCoursePolicy`):
    - `CODING_MAJOR`: alt1 (고급C) B0 이상만
    - `NON_MAJOR`: alt1 또는 alt2 (K-MOOC) 둘 중 하나
    - `EXEMPT`: skip
- **등급 비교**: `CodingAltCourseGradeRequirement` enum (A+~F, FA=0, P=취득)
- **함정**: Excel 파싱 시 leading zero 손실 가능성. P/NP 는 `CompletedCourse#isEarned` 사전 필터링
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-017: 18-21학번 균필 부재

- **조건**: 학번 < 2022 사용자
- **처리**: `balance_required_rules` 해당 학번 행 없음 → `findBalanceRequiredRule` null → early return → 카테고리 누락
- **함정**: 카테고리 누락이 정상인지 오류인지 응답만 봐서는 구별 불가 — UI 표시 정책 필요
- **검수 상태**: 코드 일치, UI 정책 합의 필요

## EC-018: 균형교양 만족 조건 = 학점 + 영역 수

- **처리** (`CategoryCreditCalculator#calculateBalanceRequired`):
    - `creditsSatisfied && areasSatisfied` 둘 다
    - completedAreas: `selectedArea` → `BalanceRequiredArea.fromSelectedArea` 변환, 제외 영역은 카운트에서 제외
- **함정**: selectedArea null 이면 카운트 누락 가능. 제외 영역 학점은 *합산됨* (영역 수만 제외)
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-019: 균형교양 제외 영역은 학과당 여러 개 가능

- **처리**: `findAllByAdmissionYearAndDeptGroup` (List 반환)
- **검수 상태**: 코드 일치

## EC-020: balance_required_rules 의 ALL_DEPT 표기 불일치

- **처리** (`CategoryCreditCalculator#findBalanceRequiredRule`):
    - 1차 (year, deptNm) 매칭
    - 2차 fallback (year, "ALL") — **문자열 "ALL"**
- **다른 시트와 차이**: 다른 시트는 dept_cd="0", balance_required_rules 만 dept_nm="ALL"
- **검수 상태**: 코드 일치, 통일 미정

## EC-021: AcademicBasicPolicy 의 추가 검증 (기필 과목 인정 여부)

- **처리** (`AcademicBasicPolicy#isRecentMajorAcademicBasic`):
    - ACADEMIC_BASIC 카테고리에만 적용
    - 학과 RequiredCourse 매칭 → 없으면 CourseEquivalence (group_code) 매칭
- **PR #310 영향**: group_code 기반으로 커버리지 확대
- **함정**: 비-ACADEMIC_BASIC 은 early return true. 학과 지정과목 시트 빈약하면 false negative
- **검수 상태**: 코드 일치 (PR #310 반영), 운영 영향 검증 필요

## EC-022: 고전독서 인증권수 의미

- **함정** (PR #254): 인증권수 = `min(필요 권수, 이수 권수)` — *기준 권수 아님*
- **현행 처리** (PR #254 fix → PR #280 보강):
    - 기준 권수는 classic_cert_criteria DB 에서 조회
    - 이수 권수만 대휴칼 파싱
    - PR #280: `ClassicsResult` record + `withFallbackCounts` 로 fallback 캡슐화 (크롤링 실패 시 DB 저장값 fallback)
- **코드**: `GraduationClassicsCertFetcher`, `ClassicsArea`, `ClassicsResult`, `ClassicsCounts`
- **검수 상태**: 코드 일치, 정책 의도 확정

## EC-023: BalanceRequiredArea enum 명칭 ≠ 편람 영역명 — QA B1 원인 (해결 2026-07-12)

- **코드**: `BalanceRequiredArea.NATURE_SCIENCE("자연과학")` ← 편람·위키 정본은 **"자연과과학"** (수강편람 2026-1 p.52 §4)
- **경로**: 성적 엑셀 `selectedArea`("자연과과학") → `CategoryCreditCalculator:184` → `fromSelectedArea()` 완전일치 실패 → **null 반환** → 이수영역 카운트 누락
- **증상**: "6/6학점인데 이수영역 1/2" (QA B1, 신고 19건 — 전 조합이 자연과과학 과목 포함)
- **주의**: `balance_required_course_area_map` 시트는 enum 코드로 적재라 무관 — 시트 아닌 코드 버그 (wiki reports/audits/2026-07-12-balance-drift.md 로 확정)
- **해결**: enum name "자연과학" → "자연과과학" (B1 배치, 2026-07-12). 유사 불일치 전수 점검 완료 — QA DB `completed_courses.selected_area` 실측상 균형 4영역 명칭은 전부 편람 표기와 일치, 잔여 이형("사상과역사"·"자연과과학기술"·"사회와문화" 7행)은 18~21학번 교양선택 era 영역명으로 B2 소관.
- **회귀 방지**: `BalanceRequiredAreaTest` (영역명 매핑 단위 테스트) + qa-cases B1 배치 15건 리플레이
- **검수 상태**: 해결 — 스냅샷 전수 diff PASS (1201명 중 87명 기대 변경, 예상 밖 0)

## EC-024: balance_required_area_exclusions 는 단과대학(dept_group) 단위 — 학부 특례 표현 불가 (미해결)

- **편람 규칙**: 창의소프트학부(디자인이노베이션·만화애니메이션텍)는 "문화와예술" 영역 제외 (수강편람 2026-1 p.52 각주 — 22~26 전 cohort 동일)
- **스키마 한계**: exclusions 시트·엔티티가 `dept_group`(단과대학 enum) 키 → 학부/학과 단위 예외 행 불가
- **증상**: 만화애니 학생에게 소속 대학 규칙(자연과과학 제외)이 적용되고 문화와예술은 인정 (QA 2-6, 22012096)
- **처리 방향**: dept_cd 단위 예외 지원 (분류 B — 스키마 확장 + validator + 시트 동반 수정)
- **검수 상태**: 미해결 (wiki reports/audits/2026-07-12-balance-drift.md §3)

