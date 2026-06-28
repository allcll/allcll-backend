# allcll-analysis Phase 3A Smoke Benchmark

작성일: 2026-06-28

## Purpose

Phase 3A에서 개선한 `allcll-analysis` skill이 실제 trigger/routing/safety/execution 기준을 개선했는지 최소 smoke benchmark로 확인하기 위한 실행 준비 문서다.

이번 단계는 runner나 grader를 구현하지 않는다. fresh session에서 수동으로 prompt를 실행하고, 아래 scoring sheet에 baseline/candidate 결과를 기록한다.

## Baseline And Candidate

Baseline:

- 기준: Phase 3A 이전 커밋
- commit: `671bf273fbd1558e557a22cbde08e00c22baa466`
- 상태: `docs/agent-harness/skills/analysis/SKILL.md`와 analysis wrappers가 Phase 3A 개선 전인 상태

Candidate:

- 기준: 현재 working tree의 Phase 3A 변경
- 변경 파일:
  - `.agents/skills/analysis/SKILL.md`
  - `.claude/skills/analysis/SKILL.md`
  - `docs/agent-harness/skills/analysis/SKILL.md`
- 상태: 아직 commit되지 않은 working tree snapshot

주의:

- candidate가 commit되기 전까지 benchmark 결과는 commit hash가 아니라 working tree snapshot에 묶인다.
- 재현 가능한 benchmark artifact로 승격하려면 candidate를 commit한 뒤 이 문서의 candidate 기준을 commit hash로 갱신한다.

## Selected Suite Scope

총 6개 smoke case를 사용한다.

- trigger positive: 2개
- hard negative: 2개
- safety: 1개
- execution: 1개

상세 목록은 `selected-cases.md`를 따른다.

## Manual Execution Protocol

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
- fresh-session execution: not run
- measured improvement: unknown

현재 말할 수 있는 결론:

- 측정 준비는 완료했다.
- 아직 baseline/candidate 실행 결과가 없으므로 Phase 3A가 실제로 개선됐다는 수치나 pass rate는 말할 수 없다.
