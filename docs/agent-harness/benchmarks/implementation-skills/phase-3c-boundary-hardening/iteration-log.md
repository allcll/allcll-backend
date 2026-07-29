# Phase 3C iteration 기록

이 문서는 benchmark 결과가 아니라 local hardening 과정 기록이다. 실제 품질 개선 수치는 fresh-session smoke나 runner 기반 검증에서 따로 측정한다.

## Iteration 1

가설: bug-fix와 refactoring을 각각 강화하는 것보다, 두 스킬이 공유하는 경계 기준을 먼저 만들면 "성능 개선", "안전하게 개선", "분석 후 고쳐줘" 요청의 오라우팅이 줄어든다.

검증: 기존 wrapper, 공통 본문, routing/safety eval, skill audit의 bug-fix/refactoring gap을 비교했다.

발견: bug-fix는 현재/기대 동작과 검증 절차가 강하지만 analysis/testing/refactoring negative boundary가 wrapper에 부족했다. refactoring은 동작 보존 원칙이 강하지만 성능 개선이 analysis 또는 bug-fix로 갈 조건이 본문 안에서만 흩어져 있었다.

수정:

- `docs/agent-harness/skills/shared/implementation-boundary.md`를 추가해 primary 판정표를 한 곳에 둔다.
- bug-fix/refactoring 본문에서 shared boundary를 먼저 읽을 조건을 명시한다.
- 각 스킬에 `references/gotchas.md`를 추가해 반복 실패 지점을 분리한다.
- 각 스킬에 `references/quality-criteria.md`를 추가해 리뷰/완료 판정 기준을 분리한다.

결과: 두 스킬이 같은 경계 기준을 보면서도, bug-fix는 재현성·최소 수정·검증 중심으로, refactoring은 baseline·보존 동작·재검증 중심으로 평가된다. 남은 한계는 실제 trigger fidelity가 아직 fresh-session으로 측정되지 않았다는 점이다.

## Iteration 2

가설: 경계 문서만 있으면 실제로 읽히지 않을 수 있으므로, harder eval과 runbook이 reference 선택과 stop 조건을 검증해야 한다.

검증: bug-fix/refactoring routing/safety eval에 애매한 성능 개선, 혼합 요청, baseline 부재 케이스가 있는지 확인했다.

발견: 기존 eval은 기본 negative case가 있었지만, "버그 수정 중 리팩토링 욕구", "리팩토링 중 운영 timing 변경", "baseline 없는 외부 연동 리팩토링" 같은 실제 작업형 실패 모드가 약했다.

수정:

- bug-fix routing/safety eval에 혼합 요청과 인접 리팩토링 분리 케이스를 추가한다.
- refactoring routing/safety eval에 scheduler timing 변경과 baseline 부재 stop 케이스를 추가한다.
- Phase 3C runbook에 원문 prompt fidelity와 safety 제약 smoke를 분리해 기록한다.

결과: eval이 단순 문구 확인이 아니라 경계 reference의 기대 행동을 직접 확인한다. 남은 한계는 실제 실행 artifact가 아직 없다는 점이다.

## Iteration 3

가설: 구조와 평가 기준이 좋아져도 smoke evidence가 runbook 수준에 머물면 analysis Phase 3A보다 설득력이 약하다.

검증: analysis Phase 3A smoke의 `selected-cases.md`, `scoring-sheet.md`, `evidence/manual-routing-notes.md` 구조와 Phase 3C 문서 구성을 비교했다.

발견: Phase 3C에는 runbook과 iteration log만 있고, 선택 case 이유, scoring sheet, safety-constrained evidence, original prompt fidelity 미실행 기록이 없었다.

수정:

- `selected-cases.md`를 추가해 6개 smoke case와 선택 이유를 남긴다.
- `scoring-sheet.md`를 추가해 정적 검증, designed eval, safety-constrained smoke 결과를 분리한다.
- `evidence/safety-constrained/manual-routing-notes.md`를 추가해 subagent smoke 결과를 기록한다.
- `evidence/original-prompt-fidelity-not-run.md`를 추가해 아직 입증하지 않은 영역을 명확히 분리한다.

결과: Phase 3C도 analysis 작업처럼 smoke evidence 구조를 갖췄다. safety-constrained smoke 2개에서 bug-fix의 current/expected behavior, impact search, verification 기준과 refactoring의 baseline, preserved behavior, recheck 기준이 출력에 반영됐다. 남은 한계는 원문 prompt fidelity와 measured improvement가 아직 미확정이라는 점이다.
