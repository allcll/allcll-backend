# Phase 3C 선택 smoke case

이 문서는 bug-fix/refactoring 스킬 개선이 실제로 무엇을 검증해야 하는지 고른 이유를 기록한다.

## 선택 기준

- bug-fix 자체 품질 기준을 확인할 것
- refactoring 자체 품질 기준을 확인할 것
- 두 스킬 사이 경계와 analysis/testing 분기를 함께 확인할 것
- 민감 파일, `allcll-crawler`, `.DS_Store` 접근을 요구하지 않을 것
- safety-constrained smoke와 원문 prompt fidelity를 분리해 해석할 수 있을 것

## Cases

| Slot | Eval file | Eval id | Name | 이유 |
|---|---|---|---|---|
| bug-fix positive | `docs/agent-harness/skills/bug-fix/evals/routing.json` | `bug-fix-routing-007` | `bug-fix-with-adjacent-refactoring-split` | bug-fix가 현재/기대 동작과 검증을 잡으면서 인접 refactoring을 섞지 않는지 확인한다. |
| bug-fix safety | `docs/agent-harness/skills/bug-fix/evals/safety.json` | `bug-fix-safety-005` | `do-not-fix-speculative-root-cause` | 추정 원인을 바로 고치지 않고 analysis finding/확인 기준으로 되돌리는지 확인한다. |
| analysis to bug-fix | `docs/agent-harness/skills/bug-fix/evals/routing.json` | `bug-fix-routing-002` | `analysis-to-bug-fix` | "원인 확인 후 맞으면 수정" 요청에서 analysis primary와 bug-fix 후속 분리가 되는지 확인한다. |
| refactoring positive | `docs/agent-harness/skills/refactoring/evals/trigger.json` | `refactoring-trigger-001` | `behavior-preserving-cleanup` | 동작 보존 cleanup에서 refactoring이 primary가 되고 baseline/recheck가 요구되는지 확인한다. |
| refactoring safety | `docs/agent-harness/skills/refactoring/evals/safety.json` | `refactoring-safety-005` | `baseline-absent-stop-before-edit` | baseline과 외부 mock 전략이 없으면 바로 수정하지 않는지 확인한다. |
| performance boundary | `docs/agent-harness/skills/refactoring/evals/routing.json` | `refactoring-routing-007` | `scheduler-timing-change-not-refactoring` | scheduler timing 변경을 refactoring으로 오분류하지 않는지 확인한다. |

## 실행 상태

- 원문 prompt fidelity: 아직 미실행. fresh-session 또는 runner 기반 실행이 필요하다.
- safety-constrained smoke: 최소 2개 case를 서브에이전트로 실행해 evidence를 남긴다.
- 정적 검증: discovery, JSON, shell syntax, whitespace 검증을 실행한다.

## 해석 기준

- safety-constrained smoke는 regression과 절차 준수 근거로만 사용한다.
- 원문 prompt에 safety 문구를 덧붙였거나 edit 금지 조건을 추가했다면 trigger fidelity 증거로 합산하지 않는다.
- pass 수가 같아도 각 스킬의 품질 기준이 출력에 반영됐는지를 별도로 본다.
