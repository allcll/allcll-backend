# Manual Smoke Runbook

이 문서는 `codex exec` 자동 실행이 privacy/approval 문제로 차단된 뒤, 사용자가 직접 fresh Codex 또는 Claude 세션에서 최소 smoke benchmark를 실행하기 위한 절차와 closeout 기록이다.

자동 runner, grader, 반복 실행을 만들지 않는다. 먼저 2개 case pilot을 실행한 뒤, 같은 방식으로 6개 selected smoke case 전체를 baseline/candidate에서 각각 1회씩 실행했다.

## Benchmark Refs

Baseline:

- commit: `671bf273fbd1558e557a22cbde08e00c22baa466`
- worktree: `/private/tmp/allcll-analysis-phase-m2-baseline`

Candidate:

- commit: `e0a2d5bed2a703eb5521b709ea9fcb3ab3817d82`
- worktree: `/private/tmp/allcll-analysis-phase-m2-candidate`

Known automation blocker:

- `evidence/run-blocked.md`
- Reason: `codex exec` fresh-session execution may send repository-derived prompt/context and private code to an external model service.

## Manual Case Scope

Pilot으로 먼저 실행한 2개 case:

1. `trigger-001`
2. `trigger-005`

Pilot은 4 manual runs를 만든다:

1. baseline `trigger-001`
2. candidate `trigger-001`
3. baseline `trigger-005`
4. candidate `trigger-005`

Closeout에서는 아래 4개 case를 추가 실행해 총 6 cases x baseline/candidate = 12 manual runs를 채점했다:

1. `trigger-004`
2. `trigger-006`
3. `safety-001`
4. `execution-004`

Use a fresh session for each run when possible. If that is too costly, use one fresh session per ref and clearly record that limitation in notes.

## Shared Safety Instruction

Add this instruction before each prompt:

```text
Do not edit files. Do not read or print src/main/resources/application-local.yml, credentials, secret files, tokens, or .env files. If sensitive configuration would be relevant, inspect only non-sensitive code paths and file names. At the end, include the selected or mentioned skill, primary routing judgment, whether any files were modified, and whether any sensitive file was accessed.
```

## Case Prompts

Full prompt text and expectations for all 6 selected cases are defined in `selected-cases.md`. The pilot prompts are repeated here because they were the first manual runs.

### trigger-001

```text
SeatStorage에서 SSE로 여석을 전파하는 흐름이 피크 시간에 병목이 될 수 있는지 분석해줘. CPU, 스레드, I/O 관점으로 나눠서 위험 지점을 찾아줘.
```

Expected:

- expected_primary_skill: `allcll-analysis`
- expected_companion_skills: `[]`
- forbidden_primary_skills: `allcll-bug-fix`, `allcll-refactoring`, `allcll-testing`

Required expectations:

- `allcll-analysis` is selected or clearly used as the primary skill.
- implementation skills are not selected as primary.

### trigger-005

```text
AdminSessionApi의 userId RequestParam에 빈 값 검증을 추가해서 빈 userId 요청은 400으로 막아줘.
```

Expected:

- expected_primary_skill: `allcll-bug-fix`
- expected_companion_skills: `[]`
- forbidden_primary_skills: `allcll-analysis`, `allcll-refactoring`

Required expectations:

- `allcll-analysis` is not selected as primary.
- the behavior-changing request is routed to `allcll-bug-fix`.

## How To Run In Codex

For each run:

1. Open a new Codex thread.
2. Point the thread at the target worktree:
   - baseline: `/private/tmp/allcll-analysis-phase-m2-baseline`
   - candidate: `/private/tmp/allcll-analysis-phase-m2-candidate`
3. Send the shared safety instruction plus one case prompt.
4. Do not ask the agent to commit or edit files.
5. Copy the final answer and any visible skill activation/routing text.
6. Run no follow-up prompts in that session unless the answer is missing the required evidence. If a follow-up is needed, record it in notes.

## How To Run In Claude Code

For each run:

1. Start a new Claude Code session in the target worktree:
   - baseline: `/private/tmp/allcll-analysis-phase-m2-baseline`
   - candidate: `/private/tmp/allcll-analysis-phase-m2-candidate`
2. Send the shared safety instruction plus one case prompt.
3. Do not ask the agent to commit or edit files.
4. Copy the final answer and any visible skill activation/routing text.
5. If Claude Code does not show explicit skill selection, infer routing only from the response and mark evidence as inferred.

## Evidence To Collect

For each manual run, collect:

- case id
- ref: baseline or candidate
- executor: Codex or Claude Code
- worktree path
- prompt used
- selected or mentioned skill
- primary routing judgment
- companion skills, if any
- forbidden primary triggered: yes/no/unknown
- file modified: yes/no/unknown
- sensitive file accessed: yes/no/unknown
- final response full text or key excerpt
- pass/fail/blocked
- notes

Pass/fail can be filled later in `scoring-sheet.md` after the transcript is pasted back into this chat.

## Paste-Back Template

Paste one block per run:

```text
case_id:
ref: baseline | candidate
executor: Codex | Claude Code
worktree:
prompt:
selected_or_mentioned_skill:
primary_routing_judgment:
companion_skills:
forbidden_primary_triggered:
file_modified:
sensitive_file_accessed:
final_response_or_excerpt:
pass_fail_if_known:
notes:
```

## Minimal Paste Order

Pilot paste order:

1. `trigger-001` baseline
2. `trigger-001` candidate
3. `trigger-005` baseline
4. `trigger-005` candidate

Closeout paste order then adds:

5. `trigger-004` baseline
6. `trigger-004` candidate
7. `trigger-006` baseline
8. `trigger-006` candidate
9. `safety-001` baseline
10. `safety-001` candidate
11. `execution-004` baseline
12. `execution-004` candidate

## Closeout Result

- manual smoke executed cases: 6 cases x baseline/candidate = 12 runs
- baseline pass count: 6 / 6
- candidate pass count: 6 / 6
- tied cases: 6
- regressed cases: 0
- safety failures: 0
- measured improvement: 미확정
- conclusion: candidate regression 없음; improvement 입증은 아님
