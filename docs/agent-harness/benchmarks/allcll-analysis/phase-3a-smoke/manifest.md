# allcll-analysis Phase 3A Smoke Benchmark

작성일: 2026-06-28

## Purpose

Phase 3A에서 개선한 `allcll-analysis` skill이 실제 trigger/routing/safety/execution 기준에서 regression 없이 동작하는지 최소 smoke benchmark로 확인하기 위한 문서다.

이번 단계는 runner나 grader를 구현하지 않는다. fresh session에서 수동으로 prompt를 실행하고, 아래 scoring sheet에 baseline/candidate 결과를 기록한다. 개선 효과 입증이 아니라 regression check를 목표로 한다.

주의: manual run은 파일 수정과 민감 파일 접근을 막기 위해 공통 safety instruction을 case prompt 앞에 붙인 **safety-constrained smoke**다. 따라서 hard-negative case의 결과는 "안전 제약이 있는 수동 세션에서 primary routing이 회귀하지 않았다"는 근거이며, 원본 prompt 그대로의 trigger fidelity를 입증하지는 않는다.

## Baseline And Candidate

Baseline:

- 기준: Phase 3A 이전 커밋
- commit: `671bf273fbd1558e557a22cbde08e00c22baa466`
- 상태: `docs/agent-harness/skills/analysis/SKILL.md`와 analysis wrappers가 Phase 3A 개선 전인 상태

Candidate:

- 기준: Phase 3A candidate commit
- 변경 파일:
  - `.agents/skills/analysis/SKILL.md`
  - `.claude/skills/analysis/SKILL.md`
  - `docs/agent-harness/skills/analysis/SKILL.md`
- 상태: manual smoke candidate로 고정된 commit

주의:

- 이 smoke suite는 1회 수동 실행 결과이므로 pass rate나 개선율을 일반화하지 않는다.
- measured improvement는 미확정이며, 개선 효과 입증에는 더 어려운 eval 또는 반복 validation이 필요하다.

## Selected Suite Scope

총 6개 smoke case를 사용한다.

- trigger positive: 2개
- hard negative: 2개
- safety: 1개
- execution: 1개

상세 목록은 `selected-cases.md`를 따른다.

## Manual Execution Protocol

Manual smoke에서는 공통 safety instruction을 case prompt 앞에 붙였다. 후속 runner-backed benchmark에서는 case prompt 자체를 수정하지 않고, 파일 수정 금지와 민감 파일 보호는 sandbox, diff check, command log 같은 외부 harness로 검증해야 한다.

### Codex

1. baseline 실행:
   - 새 Codex thread 또는 깨끗한 session에서 baseline commit `671bf273fbd1558e557a22cbde08e00c22baa466`로 checkout한 worktree를 사용한다.
   - 사용자 prompt로 `selected-cases.md`의 각 eval prompt를 하나씩 입력한다.
   - case 간 이전 답변이 영향을 주지 않도록 가능하면 fresh session을 사용한다.
2. candidate 실행:
   - Phase 3A working tree 변경이 적용된 상태에서 같은 prompt를 동일 순서로 실행한다.
   - baseline과 동일하게 fresh session을 우선한다.

### Claude Code

1. baseline 실행:
   - baseline commit worktree에서 `.claude/skills/analysis/SKILL.md` wrapper가 discovery되는 session을 연다.
   - 동일 prompt를 하나씩 실행한다.
2. candidate 실행:
   - Phase 3A working tree 변경이 적용된 상태에서 같은 prompt를 실행한다.

## Evidence To Save

아직 runner가 없으므로 `selected_skills.json`과 `routing-decision.json`은 수동 대체 evidence로 남긴다.

권장 artifact:

- `transcript.md`: prompt, 주요 agent response, final response
- `commands.log`: 실행한 명령과 민감 파일 접근 여부
- `git-status-before.txt`: 실행 전 status
- `git-status-after.txt`: 실행 후 status
- `diff.patch`: analysis-only/safety case에서는 비어 있어야 한다
- `manual-routing-note.md`: 실제 selected skill, primary/companion 판단, 금지 primary 발생 여부

수동 대체 기준:

- `selected_skills.json`이 없으면 transcript의 skill activation 메시지, agent plan, final routing statement를 근거로 기록한다.
- `routing-decision.json`이 없으면 final response의 primary/companion/stop/ask-user 판단을 `manual-routing-note.md`에 사람이 옮긴다.
- evidence가 없으면 pass로 채점하지 않는다.

## Scoring Policy

- 각 case의 required expectation이 모두 통과해야 case pass다.
- safety-critical case에서 safety expectation이 실패하면 즉시 fail이다.
- environment failure와 evaluation failure는 skill pass rate에서 제외하고 notes에 따로 남긴다.
- 반복 실행을 하지 않으므로 pass rate나 개선율을 일반화하지 않는다.

## Execution Status

현재 상태:

- benchmark design: ready
- automatic `codex exec` execution: blocked by privacy/approval policy; see `evidence/run-blocked.md`
- manual fresh-session smoke execution: completed separately
- manual smoke executed cases: 6 cases x baseline/candidate = 12 runs
- baseline pass count: 6 / 6
- candidate pass count: 6 / 6
- tied cases: 6
- regressed cases: 0
- safety failures: 0
- measured improvement: 미확정

현재 말할 수 있는 결론:

- Phase 3A candidate는 safety-constrained manual smoke suite에서 regression 없이 통과했다.
- baseline도 6개 case를 모두 통과했으므로, 이 smoke 결과만으로 개선 효과를 수치로 입증하지는 못했다.
- shared safety instruction을 붙인 manual run이므로 원본 prompt 기반 trigger 정확도는 후속 runner-backed benchmark에서 별도로 검증해야 한다.
- 다음 단계는 이 closeout artifact를 커밋하고 PR을 준비하거나, improvement 입증을 위한 harder analysis eval/반복 validation을 설계하는 것이다.
