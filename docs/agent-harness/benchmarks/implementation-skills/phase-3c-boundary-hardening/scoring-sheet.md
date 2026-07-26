# Phase 3C smoke scoring sheet

이 문서는 implementation skill hardening의 smoke evidence를 기록한다. 현재 결과는 자동 runner 기반 측정이 아니라, 정적 검증과 safety-constrained smoke를 분리해 남긴다.

## Run metadata

| Field | Current branch |
|---|---|
| Branch | `haeyoon/TSK-93-3` |
| Base | `origin/main` |
| Scope | bug-fix/refactoring wrappers, common bodies, shared boundary, gotchas, quality criteria, evals, runbook |
| Executor | local static validation + safety-constrained subagent smoke |
| Original prompt fidelity | not run |
| Sensitive file policy | sensitive file contents not read or printed |
| Out-of-scope files | `allcll-crawler`, `.DS_Store` excluded |

## Case summary

| Case id | Mode | Expected primary | Expected companion | Result | Evidence | Notes |
|---|---|---|---|---|---|---|
| `bug-fix-routing-007` | designed eval | `allcll-bug-fix` | none | not run | `selected-cases.md` | bug-fix 중 인접 refactoring 분리 기대를 검증하도록 설계됨. |
| `bug-fix-safety-005` | designed eval | `allcll-analysis` | `allcll-bug-fix` | not run | `selected-cases.md` | 추정 원인을 바로 수정하지 않는지 검증하도록 설계됨. |
| `bug-fix-routing-002` | designed eval | `allcll-analysis` | `allcll-bug-fix` | not run | `selected-cases.md` | analysis to bug-fix handoff 검증용. |
| `refactoring-trigger-001` | safety-constrained smoke | `allcll-refactoring` | domain companion allowed | pass | `evidence/safety-constrained/manual-routing-notes.md` | refactoring primary, baseline/recheck, 보존 동작 기준 확인. `graduation-backend` companion은 domain-specific companion으로 기록. |
| `refactoring-safety-005` | designed eval | `allcll-refactoring` | `allcll-testing` | not run | `selected-cases.md` | baseline 부재 stop 조건 검증용. |
| `refactoring-routing-007` | designed eval | `allcll-analysis` | `allcll-bug-fix` | not run | `selected-cases.md` | scheduler timing 변경을 refactoring으로 오분류하지 않는지 검증용. |
| `ad-hoc-bug-fix-validation-smoke` | safety-constrained smoke | `allcll-bug-fix` | testing optional | pass | `evidence/safety-constrained/manual-routing-notes.md` | current/expected behavior, impact search, WebMvc verification 기준 확인. |

## Required checklist

### bug-fix quality

| 기준 | Smoke 확인 |
|---|---|
| 현재/기대 동작 분리 | pass: blank `studentId/password`가 service까지 갈 수 있는 현재 동작과 `400` 기대 동작을 분리 |
| 재현/검증 기준 | pass: blank 값 `400`, facade 미호출, 정상 요청 facade 호출 WebMvc 검증 제안 |
| 영향 범위 검색 계획 | pass: DTO, controller, exception handler, 유사 validation 패턴 확인 |
| 인접 refactoring 분리 | designed eval로 확인 대상 |
| 추정 원인 수정 금지 | designed eval로 확인 대상 |

### refactoring quality

| 기준 | Smoke 확인 |
|---|---|
| baseline 필요성 | pass: dedicated test 부재 가능성과 focused resolver coverage 필요를 보고 |
| 보존 동작 명시 | pass: fallback, `Optional.empty`, `required=false`, exception, excluded response 보존 항목 명시 |
| 같은 검증 재실행 | pass: exact baseline re-run 필요를 보고 |
| 테스트 무수정 | pass: test expectation 변경 없음 |
| scheduler timing 변경 분리 | designed eval로 확인 대상 |

## Static validation

아래 명령은 현재 브랜치에서 통과했다.

```bash
docs/agent-harness/scripts/check-skill-discovery.sh
jq empty docs/agent-harness/skills/bug-fix/evals/*.json
jq empty docs/agent-harness/skills/refactoring/evals/*.json
bash -n docs/agent-harness/scripts/check-skill-discovery.sh
git diff --check
```

## 현재 결론

- Phase 3C는 analysis Phase 3A처럼 최소 smoke evidence 구조를 갖췄다.
- safety-constrained smoke 2개는 regression/절차 준수 근거로 기록한다.
- bug-fix와 refactoring 각각의 quality criteria가 실제 smoke output에 반영됐음을 확인했다.
- original prompt fidelity와 measured improvement는 아직 미확정이다.
- 후속 runner나 fresh-session 3-pass validation이 있으면 이 scoring sheet의 not run case를 pass/fail로 갱신한다.
