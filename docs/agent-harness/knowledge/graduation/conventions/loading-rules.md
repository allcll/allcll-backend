# 적재 규칙 (수강편람 → 시트 적재 컨벤션)

졸업요건 기준 데이터는 원래 **사람이 수강편람을 보고 손으로 구글시트에 적재**하며 정한 *암묵적 규칙*들이다. 2026-08부터 시트는 wiki 정본(cohort/·mappings/)에서 **자동 생성·반영되는 파생물**이고, 이 규칙들은 생성기·검증기에 코드화되어 있다. 코드가 이 규칙을 알고 있어야 조회/검사가 맞는다 — 신규 피쳐·버그 수정·시트 검증 시 필독. **시트 변경은 사람이 직접 하지 않는다** — 유일한 경로는 graduation-wiki `grad-sheet-change` 파이프라인(정본 수정 → matrix 재생성 → PR 리뷰 → CI apply).

마지막 갱신: 2026-08-13 (시트 자동화 파이프라인 전환 반영 · 규칙 본문은 2026-08-05 13탭 전수 역추출 기준)
값의 정본: graduation-llm-wiki(cohort/) · 코드 분기: ../edge-cases.md
**전수 감사·발견 오류 목록**: graduation-wiki `reports/audits/2026-08-05-sheet-loading-rules-profile.md`
**기계 검증**: graduation-wiki `tools/loading_rules_check.py` — 아래 규칙 26종을 스냅샷에 자동 assert (신규 위반 시 exit 1). 시트 수정·새 스냅샷 후 재실행. `--matrix` 모드는 생성 정답지를 예외 목록 없이 검증(위반 0 필수).

---

## 1. 매직값·sentinel 사전

| sentinel | 의미 | 적용 범위 | 코드/EC |
|---|---|---|---|
| `dept_cd="0"` **그리고** `dept_nm="ALL"` | 학과 무관 공통 기준. **쌍으로 함께** 적재해야 함 — 판정 경로는 dept_nm, 표시 경로는 dept_cd를 읽으므로 한쪽만 채우면 한쪽 경로에서만 잡힌다 (검증 A3: 현재 전 탭 정합) | credit·double·required·balance_rules | EC-004, EC-020 |
| `curi_no="DEPRECATED"` | 폐강/코드 미부여 자리표시. group_code는 유지 → 동일과목 매칭 생존 | required_courses (853행) | EC-011 |
| `gtelp_speaking_level=0` | 편람 표의 "-" (기준 없음). validator가 정수 강제라 빈칸 불가 | english 18-22학번 | |
| `required_credits=0` + `enabled=FALSE` | "해당 카테고리 없음" **명시 행** (행 삭제 아님). 검증 C1: `credits==0 ⟺ FALSE` 위반 0 | credit_criteria (322행) | EC-007 |
| 행 부재 | 제도 미적용 (코딩인증 18-22, balance 3탭 18-21) — credit과 반대 관례 | 인증·balance 탭 | |
| 건축 복수전공 전필 `99` | 매직값 아님 — 편람 p.99 복수전공 규정의 실값 (단일전공 93과 **별개 규칙**, 2026-08-05 확정) | double | |

## 2. major_type / major_scope 적재 컨벤션 (검증 C4·C5)

- **이원화 규칙**: 교양·졸업 카테고리(COMMON_REQUIRED/GENERAL_ELECTIVE/ACADEMIC_BASIC/MAJOR_BASIC/TOTAL_COMPLETION) → `major_type=ALL`, 전공(MAJOR_REQUIRED/MAJOR_ELECTIVE) → `SINGLE`. **MAJOR_BASIC은 전공기초인데 ALL로 적재 (유일 예외).**
- credit 탭 major_scope는 전부 PRIMARY. SECONDARY는 DOUBLE 블록에만.
- DOUBLE 기본행 = 학번당 4행 (dept="0"/ALL, PRIMARY/SECONDARY × 전필15/전선24 = 합39).

## 3. 학과명(dept_nm) 직매칭 규칙 — rename 주의

- **판정 경로는 dept_nm, 표시 경로는 dept_cd 직매칭** (탭별 상세: 감사 리포트 §12 컬럼 소비 사전). department_info는 유저생성=dept_nm / 인증판정=dept_cd로 **경로가 갈림**.
- 학과명 rename 시 **새 dept_cd 발급 + 신구 행 분리 적재** (EC-009). 2024 소융대→AI융합대는 rename이 아닌 소속 이동인데 7개 학과 dept_cd 일괄 재발급 → 학번 경계 넘는 dept_cd 조인은 반드시 (year, dept_cd) 복합키.
- `(admission_year, dept_nm)`은 **유니크해야 함** (검증 F1) — 중복 시 단건 Optional 조회가 NonUniqueResultException → 500 (유저 가입 경로 포함).
- curi_nm도 사실상 매칭 키 (학문기초 판정이 성적표 과목명과 **문자열 완전일치**) — 표기 하나 틀리면 학점 누락.

## 4. 계약학과 (국방·항공)

- **단일전공 보정**: 공통교양필수 **-1** / 전공기초 **+1** (일반 13/13 → 12/14). 시트에 일반값으로 적재되면 drift — 2026-08-05 감사에서 6건 재발 확인 (24·25 항공/국방 공필 13→12, 26 전기 13→14).
- **복수전공 수치의 정본은 편람 p.99~100** (단일전공 표 아님): 항공 주전공 (전필38, 전선11) / 국방AI 2015~ (전필39, 전선6) / 건축 (전필99, 전선24) / 법학 2024~ (전필21, 전선18).
- **복수전공 신청불가 학과** (편람 p.98, 7종: 호텔외식 계열 3 + 국방AI로봇융합·사이버국방·국방AI융합시스템·항공시스템) → double 탭에 `[X→타전공]` **단방향만** 적재가 정상 (검증 D2). 법학·건축만 양방향 8행.

## 5. 학번 세대(Schema)별 적재 차이 (검증 C3·B3·B4·E1~E3)

| 세대 | 학번 | 교양 자율 | MAJOR_BASIC | 인증 era |
|---|---|---|---|---|
| D | 18~19 | 중핵필수선택 21 (GENERAL_ELECTIVE) | 없음 | 영어 700/800, BOTH_REQUIRED, 코딩 없음 |
| C | 20~21 | 교양선택 21 (GENERAL_ELECTIVE) | 없음 | 〃 |
| B | 22~23 | 균형교양 2영역 6 (balance 탭) | **없음 (행 없어야 정상)** | 22까지 〃 / **23부터 영어 800/900·TWO_OF_THREE·코딩 신설** |
| A | 24~26 | 균형교양 3영역 9 | 15 등 (정식) | 23 era 계속 |

- 세대 경계는 **편람 열 이름이 아니라 학번**: 2021 편람 열명이 '균형교양필수'여도 GENERAL_ELECTIVE로 적재.
- 인증 탭은 era 값 1개를 **학번마다 행 복제** (전 학번 동일값이어도 학번별 행 강제 — classic).
- 인증 **면제는 criteria 탭이 아니라 department_info의 english/coding_target_type=EXEMPT**가 담당. coding EXEMPT ⟺ 학번≤2022 (era 복제).
- 구TEPS 값(766/637)의 1차 근거는 편람이 아니라 **학교 웹사이트** — wiki `source/2026-08-05-english-cert-website/note.md`.

## 6. 과목·동일과목 적재 규칙

- **전공필수/전공선택 과목 목록은 시트가 아니다** → Subject(크롤러). `required_courses`는 교양 지정과목·학문기초·전공기초용.
- required_courses 인코딩: 지정 범위 **과반 → `dept_cd="0"` ALL행 + required=FALSE override / 소수 → 학과별 TRUE 열거**.
- **course_equivalences의 원천은 학사정보시스템 CSV** (편람·cohort 아님, EC-011). group_code 2대역: 일반(0~2xxx) = 같은 과목의 코드 변천 통합, **9000번대 = 다른 과목 간 대체 인정 (수동 발급)**. 한 과목의 이중 소속 37건은 의도 — 코드 조회는 List 합집합이라 양쪽 그룹 전부 인정(과잉 인정 방향, 예외 없음).
- **택N 규칙 (미구현 버그)**: required_courses의 `note='1'/'2'` = same_course_code 그룹에서 "N과목 택해 이수" (편람 원문 확정, note≠빈값 ⟺ 그룹 존재 — 검증 R1). **코드는 note를 읽지 않고 그룹 전체 이수를 요구** → false negative. same_course_code 체계: `<첫등장학번>-<계열약어(CH/NA/LI)>-<번호>`, 24/25학번 코드 교차(swap) 있음 — (year, dept_cd) AND 조건 필수.
- course_replacements 탭은 **dead** (validator·엔티티·sync 전부 제거, 설정 키만 잔존) — 입력해도 미반영.

## 7. 탭 원천 3분류 (시트 자동화 설계 기준)

| 분류 | 탭 | matrix 생성 원천 |
|---|---|---|
| cohort/편람 파생 | credit, double, balance 3탭, 인증 4탭, department_info | cohort md + 편람 PDF 표 직접 추출 (지정과목 학과×과목 매트릭스는 md에 없음) + 학수번호는 학사시스템 조인 |
| 외부 CSV 파생 | course_equivalences | 학사정보시스템 동일과목 CSV |
| dead | course_replacements | 없음 (sync 미대상) |

## 8. 기타 컬럼 규칙

- `admission_year_short` = year%100 (검증 A1). **코드 전체에서 미사용** — 순수 사람용 컬럼.
- boolean 컬럼은 반드시 대문자 `TRUE`/`FALSE` (검증 A2) — 코드가 `"TRUE"` 완전일치만 true, `"true"/"Y"/"1"`은 **조용히 false**.
- enum 컬럼은 상수명만 (`CategoryType.fromRaw`의 한글 alias·학번 정규화는 성적표 경로 전용, 시트 경로 미적용).
- `note` 의미가 탭별로 다름: credit=카테고리 한글약어 1:1(검증 C6) / double=페어 설명문 / balance=면제·영역 설명 / **required=택N 숫자(§6)**. note는 required 외 전부 코드 미사용.
- `enabled`는 판정 엔진 미필터 (EC-007) — 현재는 0학점 관례 덕에 무해, FALSE+비0 학점 행은 판정에 그대로 산입되므로 금지.
- **시트를 고쳐도 판정이 안 바뀌는 곳 2탭**: classic_cert_criteria (전체 미사용 — 권수는 `ClassicsArea` enum 하드코딩), balance_required_course_area_map (판정은 성적표 selectedArea 신뢰, 표시 전용).
- 균형 면제의 정본 출처는 balance.md가 아니라 **cohort credit.md §3 균형열=0** → balance_rules에 required=FALSE 명시행으로 적재. exclusions는 (학번, dept_group) 단위 — 그 학번 department_info에 실존하는 dept_group만 의미 있음 (dead row 금지, 검증 X3).

---

## 검수 상태

- [x] 2026-08-05 **13탭 전수 역추출 + 편람 원문 대조 + 기계 검증 통과** (당시 24규칙, 신규 위반 0, 알려진 오류 12건은 wiki 감사 리포트 §10 수정 목록과 1:1 일치)
- [x] 2026-08-13 **시트 자동화 파이프라인 전환** — 검증 26종으로 확장(`--matrix` 모드), 시트 반영 경로는 wiki `grad-sheet-change`(matrix → PR → CI apply)로 일원화
- [x] ALL_DEPT·sentinel 쌍·페어 fallback·계약학과 보정·rename·세대 차이: 코드+데이터+편람 3자 일치
- [ ] 택N(note) 판정 로직 구현 — 미구현 확정 (§6), 백엔드 이슈화 필요
- [ ] enabled=FALSE 필터 (EC-007) — 운영 의도 확인 후 적재·조회 필터 여부 결정
