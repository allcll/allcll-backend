# allcll Agent Harness Skill Improvement Audit

Phase 1 audit for `analysis`, `bug-fix`, and `refactoring`.

작성일: 2026-06-28
브랜치: `haeyoon/TSK-93-1`

## Scope

이번 Phase 1의 목적은 실제 `SKILL.md` 본문을 크게 고치는 것이 아니라, 다음 작업 전에 필요한 discovery/load 검증, 현재 상태 감사, Phase 2 작업 계획을 문서화하는 것이다.

대상:

- `allcll-analysis`
- `allcll-bug-fix`
- `allcll-refactoring`

제외:

- `allcll-testing` 본문 개선. 다만 routing 기준과 복합 작업 경계에는 참고한다.

작업 전 확인한 사용자 변경:

- `allcll-crawler` modified
- `src/main/resources/application-local.yml` modified
- `.DS_Store` untracked

위 변경은 이번 감사 범위 밖으로 보고 되돌리지 않는다. `application-local.yml`은 민감 정보 가능성이 있어 내용을 출력하거나 참조하지 않았다.

## References

공식 문서:

- Anthropic, [Building effective agents](https://www.anthropic.com/engineering/building-effective-agents)
- Claude Code, [Best practices for Claude Code](https://code.claude.com/docs/en/best-practices.md)
- Claude Code, [Extend Claude with skills](https://code.claude.com/docs/en/skills.md)
- Claude Code, [Extend Claude Code](https://code.claude.com/docs/en/features-overview.md)
- Claude Platform, [Agent Skills overview](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview.md)
- Claude Platform, [Skill authoring best practices](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/best-practices.md)

기획서:

- Notion: `스킬 개선 계획` (`38d6a8c6159b808a9dd4f78b3f78564f`)

## Applied Principles From Official Docs

1. Keep always-loaded context small. Put project-wide rules in `PROJECT.md`/`AGENTS.md`, and put reusable, task-specific workflows in skills.
2. Skill metadata is the discovery layer. `description` must say both what the skill does and when to use it, with key trigger terms near the front.
3. Skill body is loaded on demand and then stays in context. `SKILL.md` should be concise, action-oriented, and avoid long narrative evidence that can live in references.
4. Use progressive disclosure. Keep the main skill as a table of contents plus core workflow; move long case histories, benchmark evidence, examples, and detailed rubrics into one-level `references/` files.
5. Design for verification. A workflow should include checks the agent can actually run or evidence it must report, not just “be careful” instructions.
6. Evaluate trigger and execution separately. A skill appearing in the list only proves discovery; it does not prove the right skill triggers or that outcomes improve.
7. Use fresh, isolated runs for evals. Leftover authoring context can mask missing instructions, so with-skill/without-skill or before/after comparisons need fresh sessions.
8. Prefer simple, composable patterns. Do not add complex harness machinery until a simpler workflow, eval schema, or deterministic script is insufficient.
9. Safety rules that must never fail need deterministic gates where possible. Prompt instructions alone are not enforcement.
10. Measure consistency, not identical wording. The stable parts should be routing, proceed/stop decisions, scope control, validation strategy, and safety behavior.

## Current Structure

Canonical project rules:

- `AGENTS.md` imports `docs/agent-harness/PROJECT.md`.
- `docs/agent-harness/PROJECT.md` is the shared project context.

Codex skill discovery wrappers:

- `.agents/skills/analysis/SKILL.md`
- `.agents/skills/bug-fix/SKILL.md`
- `.agents/skills/refactoring/SKILL.md`
- `.agents/skills/testing/SKILL.md`

Claude skill discovery wrappers:

- `.claude/skills/analysis/SKILL.md`
- `.claude/skills/bug-fix/SKILL.md`
- `.claude/skills/refactoring/SKILL.md`
- `.claude/skills/testing/SKILL.md`

Canonical skill bodies:

- `docs/agent-harness/skills/analysis/SKILL.md`
- `docs/agent-harness/skills/bug-fix/SKILL.md`
- `docs/agent-harness/skills/refactoring/SKILL.md`
- `docs/agent-harness/skills/testing/SKILL.md`

Existing eval files:

- `docs/agent-harness/skills/bug-fix/evals/evals.json`
- `docs/agent-harness/skills/refactoring/evals/evals.json`
- `docs/agent-harness/skills/testing/evals/evals.json`

Missing eval file:

- `docs/agent-harness/skills/analysis/evals/evals.json`

Line counts:

- `analysis/SKILL.md`: 55 lines
- `bug-fix/SKILL.md`: 176 lines
- `refactoring/SKILL.md`: 102 lines
- `testing/SKILL.md`: 200 lines

No `references/` or `scripts/` directories currently exist under the skill directories.

## Discovery And Load Audit

### Wrapper Descriptions

Current wrapper descriptions are generally usable for automatic trigger, but they are not yet sharp enough for trigger/routing evals.

`allcll-analysis`:

- Strength: includes performance bottleneck, code review, structure understanding, security review, and multi-axis analysis.
- Risk: too broad around “code review” and “analysis”; could trigger when the user actually wants bug-fix, refactoring, or testing. It does not explicitly say “do not use for code changes unless the request is analysis-first.”
- Phase 2 candidate: add should-trigger examples for RCA, architecture reading, performance/security audit, PR review; add should-not-trigger hard negatives for “fix it”, “refactor it”, and “write tests”.

`allcll-bug-fix`:

- Strength: clearly says intentional behavior change is the goal and distinguishes from refactoring.
- Risk: “작은 기능 보완” can overlap with small feature work or validation/test-only work. It also does not explicitly route tests-only requests to testing.
- Phase 2 candidate: sharpen boundaries: use for externally observable behavior changes, validation/authorization/response behavior fixes; do not use for behavior-preserving cleanup or tests-only requests.

`allcll-refactoring`:

- Strength: strongest boundary text today. It says behavior preservation, scope control, tests must pass without test edits, and bug-fix split if behavior changes.
- Risk: “성능 개선” can mean analysis-only, behavior-preserving optimization, or behavior-changing correction. Description may over-trigger for “performance diagnosis” where analysis should lead.
- Phase 2 candidate: distinguish “diagnose performance” (analysis), “behavior-preserving optimization” (refactoring), and “fix incorrect or degraded behavior” (bug-fix).

### Wrapper Reference Stability

The `.agents` wrappers and `.claude` wrappers all point to:

```text
../../../docs/agent-harness/skills/<skill>/SKILL.md
```

This is stable when interpreted relative to the wrapper file path. It also preserves the rule that shared procedures live under `docs/agent-harness/`.

Risk:

- If an agent or runner treats `@../../../...` as relative to current working directory rather than wrapper file directory, subdirectory execution could fail.
- Current Codex session listed `.agents/skills/*/SKILL.md` as the skill source and the common body was readable through the wrapper. This is positive evidence, but not a formal smoke test across fresh sessions and subdirectories.

Recommended Phase 2 smoke tests:

- Start at repository root and invoke each skill directly.
- Start under `src/main/java` and ask a prompt that should trigger each skill.
- Start under `docs/agent-harness` and repeat.
- Start under `allcll-crawler` only if the intended harness scope includes the submodule; otherwise document that submodule cwd is out of scope.
- In each run, confirm which wrapper and which common body were read.

### Duplicate Skill Name Exposure

Normal intended behavior:

- Codex uses `.agents/skills`.
- Claude Code uses `.claude/skills`.
- `docs/agent-harness/skills/*/SKILL.md` has no YAML frontmatter, so it should not be discovered as a separate skill by standard discovery.

Risk:

- A custom evaluator or broad filesystem scanner that loads both `.agents/skills` and `.claude/skills` would see duplicate `name` values (`allcll-analysis`, `allcll-bug-fix`, `allcll-refactoring`, `allcll-testing`).
- A runner that treats canonical docs as discoverable skills could create a second, metadata-less variant.

Recommended Phase 2 guard:

- Define the discovery roots per surface explicitly.
- Add a static harness check: only `.agents/skills/*/SKILL.md` and `.claude/skills/*/SKILL.md` may have skill frontmatter; canonical docs must not.
- Add duplicate-name detection within each surface, while allowing the same names across Codex and Claude surfaces.

### Subdirectory Discovery Risk

Claude Code official docs say project skills are loaded from `.claude/skills` in the starting directory and parent directories up to the repository root. That supports root-level `.claude/skills` when launched from a normal subdirectory.

Codex `.agents` discovery behavior is not proven by the official Claude docs. In this session, Codex exposed the `.agents` wrappers because the workspace root was `/Users/haeyoon/IdeaProjects/allcll-backend`.

Risk:

- Starting a Codex task from a nested directory may or may not discover root `.agents/skills` depending on Codex’s actual loader.
- Starting from `allcll-crawler`, a separate git submodule, may be ambiguous: it is physically nested but logically a different repo.

Recommended Phase 2 guard:

- Treat root cwd support as verified only after a fresh-session smoke test.
- Add an explicit “supported cwd” note to the audit/runner docs.
- Keep wrapper paths relative and do not duplicate common body into `.agents`.

### Common Body Change Propagation

Evidence:

- The skill source locators point to `.agents/skills/*/SKILL.md`.
- Wrappers contain an explicit `@../../../docs/agent-harness/skills/*/SKILL.md` import.
- In this session, the common bodies were read from `docs/agent-harness`.
- Claude Code docs say skill directory changes are watched for `SKILL.md` text, and skill bodies are read on demand.

Limits:

- Description changes must happen in wrappers to affect automatic trigger metadata.
- Common body changes should affect execution after the wrapper is loaded, but this has not yet been demonstrated in a clean Codex or Claude session.

Recommended Phase 2 test:

- Add a temporary harmless marker in a scratch branch/common body, start a fresh session, invoke the skill, confirm marker load, then remove it before committing.
- Alternatively, use a non-committed smoke fixture and record transcript evidence.

## Skill Audit: allcll-analysis

### Current Strengths

- Single responsibility is directionally clear: analysis across performance, quality, security, structure, and domain impact.
- It explicitly prevents one-axis analysis by requiring CPU, thread, I/O, N+1, and domain perspectives.
- It requires intent inference before changing code.
- It requires file/line references and scenario-based findings.

### Gaps By Axis

Trigger:

- Description is broad enough to catch many useful analysis tasks, but lacks hard negative boundaries against bug-fix/refactoring/testing.
- “코드 리뷰” may overlap with slash command review workflows.

Routing:

- Body does not define when analysis should stop at findings versus route into bug-fix or refactoring.
- No explicit companion-skill rules: analysis followed by bug-fix, analysis plus refactoring, or analysis plus testing.

Execution:

- Good multi-axis checklist, but no step order: scope, evidence collection, cross-check, finding synthesis, final confidence.
- No explicit instruction to avoid code edits during pure analysis requests.
- Subagent warning is useful but too specific to one failure mode.

Safety:

- Needs a clear “read-only by default” gate.
- Needs sensitive-file handling reminder for analysis/security review, especially credentials and `application-local.yml`.
- Needs a stop condition for insufficient evidence.

Eval gap:

- No eval file exists.
- Needed suites:
  - trigger positives: RCA, performance audit, security review, architecture map, PR review.
  - trigger negatives: “fix validation”, “refactor duplicate logic”, “write tests”.
  - execution: known-fixture finding recall, file/line grounding, severity calibration, false-positive control.
  - safety: analysis-only request must not edit files; sensitive config must not be printed.

Progressive disclosure:

- Current body is short enough to keep in `SKILL.md`.
- Session history and examples could move to `references/case-studies.md` if expanded.
- A future `references/analysis-rubric.md` could hold severity calibration and finding taxonomy.

Improvement candidates:

- Add explicit read-only default and “ask before fixing” rule.
- Add routing matrix for analysis-only, analysis-to-fix, analysis-to-refactor, and analysis-plus-testing.
- Add a concise output template without requiring emoji severity markers.
- Create `analysis/evals/evals.json`.

## Skill Audit: allcll-bug-fix

### Current Strengths

- Single responsibility is strong: intentional behavior change and bug fixing.
- Step order is concrete: reproduce or clarify, explore existing patterns, check impact, implement, verify.
- Validation-trigger pairing is excellent project-specific guidance.
- Self-report requirements reduce false completion claims.

### Gaps By Axis

Trigger:

- Description catches bug fixes and validation/authorization changes.
- “작은 기능 보완” is useful but broad; may pull in feature work that needs design or testing-only requests.

Routing:

- Body says behavior-preserving work should use refactoring.
- It references testing behavior, but not as a companion-skill boundary. For test creation itself, testing should be primary.
- It should explicitly route “root-cause first” or “why is this happening” to analysis before bug-fix.

Execution:

- The workflow is detailed, but it mixes core procedure with iteration evidence.
- The required test command `./gradlew :test --tests "*관련패턴*"` may be too generic for multi-module or package-specific cases; Phase 2 can define command-selection guidance in references or PROJECT.
- Impact grep examples are good but can be broadened into reusable patterns.

Safety:

- Strong scope and verification rules exist.
- Needs explicit “do not print sensitive config” reminder because bug investigations may touch env and credentials.
- Needs “do not modify unrelated existing user changes” as a skill-local safety gate, or rely on `PROJECT.md`.

Eval gap:

- Existing evals cover validation DTOs, controller parameter validation, and an Optional boundary case.
- Missing:
  - should-not-trigger cases for pure refactoring, tests-only, analysis-only, and large feature design.
  - safety cases for scope expansion, user-change preservation, sensitive files, and false completion.
  - fixture-backed execution cases with diff/test evidence, not only expected narrative.
  - repeated-run consistency data.

Progressive disclosure:

- `SKILL.md` is still below 500 lines, but iteration history and detailed benchmark notes pollute the main execution path.
- Current body references missing paths:
  - `docs/agent-harness/skills/bug-fix-workspace/iteration-1/benchmark.json`
- Move baseline/iteration evidence to `references/measurement-history.md` after benchmark artifacts are restored or the stale reference is resolved.

Improvement candidates:

- Split “core workflow” from “measurement evidence.”
- Sharpen trigger boundaries around small features and tests-only work.
- Add trigger/routing/safety evals.
- Restore or replace missing benchmark artifact references before treating the current 100% claim as reproducible.

## Skill Audit: allcll-refactoring

### Current Strengths

- Single responsibility is very clear: behavior-preserving structure/performance improvement.
- Strong safety gate around scope, test immutability, and user approval when range exceeds `PROJECT.md` guidance.
- It directly addresses a known failure mode: accidental behavior changes during refactoring.
- Baseline-before-change and same-test-after-change loop is concrete.

### Gaps By Axis

Trigger:

- Description strongly covers refactoring, structural change, and behavior preservation.
- “성능 개선” needs disambiguation from analysis-only diagnosis and bug-fix correction.

Routing:

- Body has good bug-fix split rules.
- It does not explicitly say when to use analysis first for performance bottleneck investigation before changing code.
- It references tests, but should clarify that adding or changing tests is not the primary path unless the user asks for test work separately.

Execution:

- Baseline loop is strong.
- Needs a concise “allowed evidence of behavior preservation” list: test result, output comparison, API contract comparison, SQL/query count comparison, or benchmark, depending on task.
- Needs a fallback when no relevant tests exist: currently says report and agree on manual scenario, but Phase 2 can make that a strict stop/ask gate.

Safety:

- Strongest safety section among target skills.
- Needs explicit sensitive-file and user-change handling if refactoring touches configuration or build files.
- Needs clearer rule for performance changes that alter timing, logging, ordering, exceptions, or persistence side effects.

Eval gap:

- Existing evals cover broad extraction, small dedupe, Optional cleanup, and Set performance conversion.
- Missing:
  - trigger negatives: bug fix, tests-only, analysis-only, feature design.
  - safety cases: accidental log/message change, test edit to pass, scope expansion, baseline skipped.
  - routing cases where analysis should lead and refactoring follows only after diagnosis.
  - repeated-run consistency and changed-file-set stability.

Progressive disclosure:

- Current body is concise enough.
- Measurement history and Session 4-C details can move to `references/measurement-history.md`.
- Current body references missing path:
  - `docs/agent-harness/skills/refactoring-workspace/iteration-1/benchmark.json`

Improvement candidates:

- Add performance-routing distinction.
- Add strict stop/ask rule for absent baseline tests.
- Move measurement history out of main path after artifact location is settled.
- Add trigger/routing/safety eval suites.

## Cross-Skill Routing Matrix

Use this as a Phase 2 starting point, not as a final spec.

| User intent | Primary skill | Companion skill | Notes |
| --- | --- | --- | --- |
| “원인 분석”, “왜”, “구조 파악”, “성능 병목 찾아줘” | analysis | none or testing for evidence | Read-only by default. Do not edit unless explicitly asked. |
| Reported bug, validation/auth/response behavior change | bug-fix | testing | Must define current vs expected behavior and verify fix. |
| Behavior-preserving cleanup, duplicate removal, method extraction | refactoring | testing for baseline/recheck | Tests should pass without test edits. |
| Behavior-preserving performance optimization with known target | refactoring | analysis, testing | If bottleneck is not identified, analysis first. |
| Test creation or test modification request | testing | analysis if scenario unclear | Testing is excluded from this Phase 1 improvement but remains routing reference. |
| “분석 후 고쳐줘” | analysis then bug-fix/refactoring | testing | First produce findings, then choose behavior-change vs behavior-preserving path. |
| Large feature or domain model redesign | none of these as primary | analysis for planning if requested | Needs separate design/spec step. |

## Work Needed Before Editing Skill Bodies

1. Define canonical discovery roots for Codex and Claude:
   - Codex: `.agents/skills`
   - Claude: `.claude/skills`
   - Common body: `docs/agent-harness/skills`
2. Add a static validation script or checklist:
   - wrapper has frontmatter
   - canonical docs do not have frontmatter
   - wrapper `@` target exists
   - no duplicate names within one surface
   - no stale benchmark links
3. Decide benchmark artifact location:
   - restore old `*-workspace/iteration-1/benchmark.json`, or
   - move historical claims into references and mark old benchmark paths as deprecated.
4. Define eval schema before adding many evals:
   - keep compatibility with existing `skill_name`, `evals`, `prompt`, `expected_output`, `files`
   - add `expectations` as structured assertions
   - separate trigger/routing/safety/execution suites.
5. Define failure taxonomy:
   - `discovery_failure`
   - `trigger_false_positive`
   - `trigger_false_negative`
   - `wrong_skill`
   - `missing_companion_skill`
   - `process_skip`
   - `unsafe_scope_expansion`
   - `false_completion_claim`
   - `test_manipulation`
   - `user_change_damage`
   - `sensitive_data_exposure`
   - `invalid_eval`
   - `environment_failure`
6. Define a smoke protocol for fresh sessions and subdirectory cwd.
7. Decide whether references will be introduced per skill in Phase 2 or after eval harness setup.

## Phase 2 Work List

1. Create `docs/agent-harness/evals/failure-taxonomy.md`.
2. Create `docs/agent-harness/evals/schemas/skill-eval.schema.md` or equivalent schema note.
3. Add static discovery audit script or documented command:
   - check wrapper frontmatter
   - check wrapper target exists
   - check duplicate names per surface
   - check missing referenced benchmark files.
4. Add discovery/load smoke test documentation:
   - root cwd
   - nested source cwd
   - docs cwd
   - optional submodule cwd.
5. Create or update eval suites:
   - `analysis/evals/evals.json`
   - `analysis/evals/trigger.json`
   - `analysis/evals/routing.json`
   - `analysis/evals/safety.json`
   - `bug-fix/evals/trigger.json`
   - `bug-fix/evals/routing.json`
   - `bug-fix/evals/safety.json`
   - `refactoring/evals/trigger.json`
   - `refactoring/evals/routing.json`
   - `refactoring/evals/safety.json`
6. Convert existing bug-fix/refactoring evals to structured `expectations` without changing their intent.
7. Restore or explicitly deprecate missing benchmark references.
8. Draft minimal SKILL.md edits only after eval gaps are represented:
   - analysis: read-only default, routing boundaries, safety gates.
   - bug-fix: trigger boundaries, testing/analysis routing, references split.
   - refactoring: performance routing, no-baseline stop/ask gate, references split.
9. Run a small validation:
   - one discovery smoke per target skill.
   - one trigger positive and one hard negative per target skill.
   - one safety case per target skill.

## Phase 2A Outputs

Phase 2A created the evaluation foundation documents and static audit script:

- `docs/agent-harness/evals/README.md`
- `docs/agent-harness/evals/failure-taxonomy.md`
- `docs/agent-harness/evals/schemas/skill-eval.schema.md`
- `docs/agent-harness/evals/discovery-static-audit.md`
- `docs/agent-harness/evals/rubrics/README.md`
- `docs/agent-harness/benchmarks/artifact-policy.md`
- `docs/agent-harness/scripts/check-skill-discovery.sh`

## Risks And Deferred Items

- Current session proves the common bodies are readable, but not that fresh-session automatic trigger works from every cwd.
- Official Claude docs describe `.claude/skills`; Codex `.agents` behavior must be validated empirically.
- Existing benchmark claims in bug-fix/refactoring point to missing files. Treat them as historical notes until artifacts are restored.
- Existing evals are narrative `expected_output`; they are useful but not enough for deterministic grading.
- Trigger/routing quality is currently assumed from description text, not measured.
- Testing skill is excluded from body improvement now, but Phase 2 routing evals must include testing as a competing/companion skill.
- The Notion plan calls for 10-run stability and holdout validation; Phase 2 should prepare schema and smoke tests, not jump straight to full 10-run certification.
- Sensitive config handling should be covered in safety evals without reading or printing actual credentials.

## Suggested Phase 2 Prompt

```text
/Users/haeyoon/IdeaProjects/allcll-backend 에서 작업해줘.

목표:
haeyoon/TSK-93-1 브랜치에서 allcll agent harness skill 개선 Phase 2를 진행한다. Phase 1 문서 docs/agent-harness/skills/skill-improvement-audit.md 를 기준으로, SKILL.md 본문 대규모 개선 전에 평가 기반과 discovery/load smoke 기반을 만든다.

중요:
- AGENTS.md와 docs/agent-harness/PROJECT.md를 먼저 읽어라.
- 작업 시작 전 git status --short 로 사용자 변경을 확인하고, 내가 만들지 않은 변경은 되돌리지 마라.
- application-local.yml, credentials, 민감 정보는 출력하지 마라.
- testing 스킬 본문 개선은 제외하지만, routing eval에서는 competing/companion skill로 포함한다.
- .agents/ 아래 wrapper에 공통 절차를 복사하지 말고, 공통 본문은 docs/agent-harness/ 아래에서 관리한다.
- 이번 단계에서도 analysis/bug-fix/refactoring SKILL.md 본문은 최소한으로만 건드리고, 가능하면 eval/schema/smoke 문서와 기반 파일을 먼저 만든다.

해야 할 일:
1. docs/agent-harness/skills/skill-improvement-audit.md 를 읽고 Phase 2 작업 범위를 확정한다.
2. discovery/load static audit 기준을 문서 또는 스크립트로 추가한다.
   - wrapper frontmatter 존재
   - wrapper @ target 존재
   - canonical docs에는 skill frontmatter 없음
   - surface별 duplicate skill name 없음
   - stale benchmark link 탐지
3. docs/agent-harness/evals/ 아래에 failure taxonomy와 eval schema 문서를 추가한다.
4. 기존 bug-fix/refactoring evals.json을 보존하면서 structured expectations 도입 방안을 적용하거나 별도 validation 초안으로 작성한다.
5. analysis eval 공백을 메운다.
   - trigger
   - routing
   - safety
   - execution 기본 문항
6. bug-fix/refactoring에 trigger/routing/safety eval 초안을 추가한다.
7. missing benchmark artifact 경로를 실제로 복구할지, references로 옮기며 historical note 처리할지 제안하고, 이번 단계에서 가능한 최소 조치를 한다.
8. 마지막에 변경 파일, 변경 요약, 검증 결과, 남은 Phase 3 작업을 보고한다.

완료 기준:
- discovery/load를 검증할 수 있는 static 기준이 생긴다.
- failure taxonomy와 eval schema 초안이 생긴다.
- analysis의 eval 공백이 최소 초안으로 해소된다.
- bug-fix/refactoring의 trigger/routing/safety eval 초안이 생긴다.
- SKILL.md 본문 대규모 개선 없이 다음 단계에서 측정 가능한 상태가 된다.
```
