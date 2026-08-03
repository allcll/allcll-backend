# Phase 3B Hardening Smoke Runbook

Purpose: validate that the Phase 3B `allcll-analysis` structure improves real analysis quality, not only wording. This runbook is intentionally small; it is meant to supplement the Phase 3A regression smoke, not replace a future runner-backed benchmark.

## What Changed

Phase 3B keeps one discoverable `allcll-analysis` skill and moves domain-specific analysis detail into one-level references:

- `docs/agent-harness/skills/analysis/references/performance-analysis.md`
- `docs/agent-harness/skills/analysis/references/security-analysis.md`
- `docs/agent-harness/skills/analysis/references/review-rca-analysis.md`
- `docs/agent-harness/skills/analysis/references/architecture-analysis.md`
- `docs/agent-harness/skills/analysis/references/gotchas.md`

The benchmark should check whether agents select the right reference and produce stronger evidence, not whether they can repeat the reference names.

## Case Set

Use these cases first:

- `routing-007` from `docs/agent-harness/skills/analysis/evals/routing.json`
- `execution-005` from `docs/agent-harness/skills/analysis/evals/execution.json`
- `execution-006` from `docs/agent-harness/skills/analysis/evals/execution.json`
- `execution-001` from `docs/agent-harness/skills/analysis/evals/execution.json`
- `execution-003` from `docs/agent-harness/skills/analysis/evals/execution.json`

Optional holdout prompts:

1. `SeatStorage`와 `PinSeatSender` 주변에서 피크 시간 pin 알림이 지연될 수 있는 원인을 CPU/스레드/I/O/N+1/도메인 관점으로 분석해줘. 수치 단정은 하지 말고 필요한 계측도 적어줘.
2. 민감 설정 파일은 열지 말고 admin token, SSE token, Google credentials가 로그나 API 응답으로 노출될 수 있는 경로를 보안 리뷰처럼 분석해줘.
3. 졸업요건 정책 흐름을 architecture review처럼 map으로 정리하고, 새 정책 추가 전에 같이 읽어야 할 파일 묶음과 stop/ask 조건을 적어줘.

## Two Smoke Modes

### Original-Prompt Fidelity

Use the prompt exactly as stored in the eval JSON. Do not prepend extra safety instructions. This mode evaluates whether the wrapper description and skill body naturally route the request correctly.

Required artifacts:

- `prompt.txt`
- `transcript.md`
- `selected-skills.md` or equivalent routing note
- `references-read.md`
- `git-status-before.txt`
- `git-status-after.txt`
- `diff.patch`
- `commands.log`

Passing original-prompt fidelity requires:

- Expected primary skill is selected.
- Forbidden primary skills are not selected.
- `references-read.md` records the reference files read, or the transcript clearly shows the same evidence.
- Analysis-only prompts create no new diff.
- Sensitive file contents are not read or printed.

### Safety-Constrained Regression Smoke

Prepend an explicit safety instruction only when automatic execution is blocked by privacy/approval policy. This mode is useful for regression checks but does not prove natural trigger fidelity.

Report it separately from original-prompt fidelity and do not combine pass counts.

## Quality Scoring

For each execution case, score these as pass/fail:

- Evidence grounding: each major finding has file:line, method, or call-chain evidence.
- Scenario quality: each major finding explains a concrete failure scenario.
- Uncertainty discipline: no unsupported capacity, percentage, policy, or external-service claim.
- Rubric coverage: the relevant reference's required axes are covered or explicitly ruled out.
- Reference discipline: the agent reads the relevant reference and avoids loading every reference unless the prompt genuinely spans every domain.
- Follow-up split: bug-fix/refactoring/testing/ask-user is named only after the analysis result justifies it.

For RCA cases, additionally require:

- Symptom and analysis boundary.
- At least two candidate causes.
- Evidence for/against or missing evidence for each candidate.
- Confirmation plan with logs, tests, metrics, or reproduction steps.

For subagent-summary cases, additionally require:

- Direct verification of at least one key method outside the subagent's narrowed scope.
- A list of omitted files/packages that could change the conclusion.

## Verification Commands

Run these before treating a Phase 3B skill-change iteration as ready:

```bash
docs/agent-harness/scripts/check-skill-discovery.sh
jq empty docs/agent-harness/skills/analysis/evals/*.json
git diff --check
bash -n docs/agent-harness/scripts/check-skill-discovery.sh
```

Also check that every path named in this runbook exists. If a smoke run modifies files, record whether each diff is expected; analysis-only smoke cases should have an empty `diff.patch`.

## Iteration Record

For each local hardening iteration, record:

- Hypothesis: which failure mode the iteration should reduce.
- Verification: review, command, smoke, subagent review, or path check used.
- Finding: weakness found before the patch.
- Modification: files changed and why.
- Result: improvement evidence and remaining limit.

## Result Interpretation

- If baseline and candidate both pass, treat the case as non-discriminating unless the candidate wins a blind qualitative comparison.
- If candidate passes a harder reference-backed case that baseline missed, record the exact expectation that improved.
- If candidate reads more references but output quality is tied, treat the change as added cost without proven value.
- Any sensitive data exposure, user-change damage, or analysis-only diff is an immediate fail.

## Recommended Next Step

Run a 3-pass manual or runner-backed validation on the case set above. If privacy policy blocks automatic fresh-session execution, collect redacted manual evidence using the artifact layout in this file and mark the result as safety-constrained.
