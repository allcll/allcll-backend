---
name: graduation-backend
description: domain/graduation/**, support/sheet/**, admin/graduation/** 코드 작업(신규 피쳐·버그 수정·리팩토링) 시 발동. 사람이 수강편람 읽고 임의 규칙으로 적재한 데이터라, 그 규칙을 코드가 빠뜨리지 않게 강제한다. resolver 계층 경유 의무, 적재 규칙·데이터 출처 선점검, knowledge 선행 점검, baseline 테스트를 게이팅한다.
---

# 졸업요건 백엔드 코드 다리

이 도메인의 기준 데이터는 **사람이 수강편람을 보고 임의 규칙으로 구글시트에 적재**한 것이다. 그래서 코드가 그 규칙을 모르면 *졸업 가능 여부를 오판*한다. 이 skill 은 그 누락을 막는 게이트다.

## 발동 조건

다음 경로를 손대는 작업이면 발동:
- `src/main/java/kr/allcll/backend/domain/graduation/**`
- `src/main/java/kr/allcll/backend/support/sheet/**`
- `src/main/java/kr/allcll/backend/admin/graduation/**`

## 핵심 전제 — 코드 짜기 전 반드시 읽을 3종

| 무엇 | 어디 | 왜 |
|---|---|---|
| **resolver 계층 규칙** | `docs/agent-harness/docs/agent-harness/knowledge/graduation/conventions/resolver-layering.md` | service 에서 repository 직접 호출 금지. 조회는 resolver 경유 |
| **적재 규칙** | `docs/agent-harness/docs/agent-harness/knowledge/graduation/conventions/loading-rules.md` | ALL_DEPT·계약학과 보정·rename·세대 차이 등 사람 적재 규칙 |
| **데이터 의존 지도** | `docs/agent-harness/docs/agent-harness/knowledge/graduation/map/data-dependency-map.md` | 전공 과목=크롤러, 교양=시트 등 출처 구분 |

추가 참조: `docs/agent-harness/docs/agent-harness/knowledge/graduation/map/architecture.md`(파이프라인 + 시트 13개 인벤토리), `docs/agent-harness/docs/agent-harness/knowledge/graduation/contracts/data-loading-policy.md`(시트↔엔티티↔validator 매핑 + 적재 메커니즘), `docs/agent-harness/docs/agent-harness/knowledge/graduation/contracts/data-usage-policy.md`(검사 계약), `docs/agent-harness/docs/agent-harness/knowledge/graduation/edge-cases.md`(코드 분기 24), `docs/agent-harness/docs/agent-harness/knowledge/graduation/glossary.md`(enum), docs/agent-harness/docs/agent-harness/knowledge/graduation/contracts/certification-policy.md(인증 3종), `docs/agent-harness/docs/agent-harness/knowledge/graduation/decisions/`(정책 결정).

> 시트 *값·검증*은 별도 위키(graduation-llm-wiki: cohort 값 + grad-wiki-audit)가 담당. 시트 *스키마 정본*은 `*SheetValidator.java` + 엔티티 코드.

## 절대 규칙

1. **resolver 경유.** 졸업요건 기준 조회는 resolver 를 거친다. service 에서 repository 직접 호출 금지. 새 조회 규칙은 resolver 에 추가(service if 분기 금지). 검사 경로(`GraduationChecker`·`CategoryCreditCalculator`)는 아직 repository 직접 호출 = **리팩토링 대상**이니 새로 늘리지 말 것.
2. **전공 과목은 시트 아님.** 전공필수/선택 과목 목록은 Subject(크롤러). 교양·학문기초만 `required_courses` 시트.
3. **적재 규칙 준수.** ALL_DEPT fallback·계약학과 보정·dept_nm 직매칭·세대 차이를 빠뜨리지 말 것.
4. **baseline 테스트.** `GraduationChecker`·`CategoryCreditCalculator`·`*Resolver` 동작 변경은 **실 데이터 fixture** 로 전후 동일성 확인 (mock 우회 금지). 재현 테스트는 실제 학번·학과 fixture.
5. **정책 해석 변경은 AI 단독 금지** (아래 분류 A) — 위키(정책 정본)·사람 결정 게이트.

## Step 1: 작업 분류 (가장 먼저, notes 박제)

| 분류 | 예 | 처리 |
|---|---|---|
| **A. 정책 해석 변경** | "코딩인증 면제 학번 확대", "균형 영역 학점 변경" | **AI 단독 금지.** 중단 → 위키(graduation-llm-wiki) 정책 확인 + 사람 결정(decisions/ ADR) |
| **B. 시트 스키마 변경** | "credit_criteria 컬럼 추가", "validator 강화" | 동반 수정: 시트(사람) + `*SheetValidator` + 엔티티. (스키마 정본 = validator+엔티티 코드, 값·검증은 위키 audit) |
| **C. 리팩토링 (동작 보존)** | "buildDoubleMajorCriteria 가독성", "검사 경로 resolver 통일" | `refactoring` skill + baseline 동일성 테스트 1개 이상 |
| **D. 버그 수정·작은 기능** | "DOUBLE SECONDARY 누락 NPE" | `bug-fix` skill + 실 fixture 재현 테스트 + edge-cases 박제 확인 |

## Step 2: Knowledge 선행 점검 (의무)

작업 범위 관련 knowledge 가 최신인지 확인 후 notes 박제:

| 작업 범위 | 점검 대상 |
|---|---|
| 조회/검사 쿼리 | `resolver-layering.md` (resolver 경유 여부) |
| 학점·카테고리 로직 | `loading-rules.md` + `CreditCriterion`/`*SheetValidator` 코드 |
| 과목 조회 | `data-dependency-map.md` (시트 vs 크롤러) |
| 균형교양 | `docs/agent-harness/docs/agent-harness/knowledge/graduation/conventions/loading-rules.md` §균형 + `BalanceRequired*` 엔티티 |
| 인증제 | `docs/agent-harness/docs/agent-harness/knowledge/graduation/contracts/certification-policy.md` + `*CertCriterion` 엔티티 |
| 학번 보정·전공 예외 | `edge-cases.md` |

```
Knowledge 점검:
- 대상: docs/agent-harness/docs/agent-harness/knowledge/graduation/conventions/resolver-layering.md, loading-rules.md
- 코드 일치: OK / 불일치(항목)
- 누락: 없음 / 있음
```

## Step 3: 구현 + 검증

- 분류 A → 중단·게이트. B → 동반 수정 3종. C → refactoring + baseline. D → bug-fix + 재현 테스트.
- 변경 후: §절대규칙 4 baseline 테스트 실행.

## 자가 보고 (필수 — 빈 항목도 "해당 없음" 명시)

1. **작업 분류** (A/B/C/D) + 근거
2. **Knowledge 선행 점검 결과**
3. **resolver 준수** (repository 직접 호출 안 늘렸나 / 새 규칙 resolver에 넣었나)
4. **변경 파일 + 라인 수** (CLAUDE.md 변경 범위 가이드 점검)
5. **검증 결과** (baseline 비교 / 재현 테스트)
6. **분리한 인접 문제** / **Knowledge 갱신 필요 사항**
