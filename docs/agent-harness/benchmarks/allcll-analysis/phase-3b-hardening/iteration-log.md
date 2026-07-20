# Phase 3B Iteration Log

This log records local hardening iterations for `allcll-analysis`. It is not a benchmark result; it documents hypotheses, checks, and remaining limits so future smoke runs can judge whether the changes helped.

## Iteration 1

Hypothesis: agents can read the new references but still produce weak outputs if the common body does not define the evidence floor, completion criteria, and failure behavior.

Verification: reviewed the current wrapper/body/reference diff against skill operation criteria: trigger conditions, negative boundaries, required evidence, procedure, output contract, completion criteria, failure behavior, gotchas, verification, and eval coverage.

Finding: the single-skill plus reference design matched progressive disclosure, but `SKILL.md` still left completion criteria implicit in the result format. Cross-cutting pitfalls such as mixed analyze-then-fix requests, subagent summaries, capacity overclaims, and sensitive-file security review were scattered across references.

Modification:

- Updated `docs/agent-harness/skills/analysis/SKILL.md` with an explicit evidence floor, completion criteria, and stop/failure behavior.
- Added `docs/agent-harness/skills/analysis/references/gotchas.md` for cross-cutting routing, evidence, safety, and review-quality pitfalls.

Result: the skill now has a clearer output contract and a repeatable stop condition without making the main `SKILL.md` a long checklist. Remaining limit: eval/runbook coverage still needs to assert the new gotchas/reference workflow explicitly.

## Iteration 2

Hypothesis: reference splitting only improves behavior if evals and smoke artifacts verify reference selection, gotcha handling, and harder failure modes instead of only checking final wording.

Verification: reviewed `routing.json`, `execution.json`, and `phase-3b-hardening/runbook.md` for reference-backed expectations, original-prompt fidelity evidence, safety-constrained smoke separation, and verification command coverage.

Finding: `routing-007` and `execution-005` were harder than Phase 3A cases, but they did not explicitly test the new gotchas workflow. The runbook distinguished smoke modes but did not require a `references-read.md` artifact or define an iteration record template.

Modification:

- Added `execution-006` to test that agents do not blindly trust a subagent summary and instead re-check omitted scheduler/SSE/storage flows.
- Strengthened `routing-007` to require separation between speculative causes, confirmation criteria, and confirmed bug-fix handoff.
- Updated the Phase 3B runbook with `references-read.md`, reference-discipline scoring, verification commands, and an iteration record template.

Result: the evaluation surface now covers the new cross-cutting gotchas and can distinguish "read the right supporting material and used it" from "produced a plausible final answer." Remaining limit: this is still a designed hardening suite; original-prompt trigger fidelity and quality improvement need fresh-session or runner-backed execution.

## Subagent Review Integration

Hypothesis: independent reviewers should catch non-discriminating evals and safety wording drift that the authoring pass may normalize.

Verification: ran two read-only subagent reviews. One reviewed trigger/body/reference design; one reviewed eval/runbook/verification. Both were instructed not to inspect sensitive files, `allcll-crawler`, or `.DS_Store`.

Finding:

- `execution-005` could pass with a generic RCA answer because it did not require scheduler/SSE call-chain grounding.
- `routing-007` was too close to `routing-002` because the prompt itself contained the safety instruction that the skill should supply.
- The runbook omitted `gotchas.md` from the reference list.
- `security-analysis.md` weakened the project safety policy by implying a user policy change could allow secret-value reading.
- New reference files are still unstaged; they must be included when committing or the reference workflow breaks.

Modification:

- Added a required call-chain grounding expectation to `execution-005`.
- Shortened `routing-007` prompt so staged analysis/no speculative edit behavior is enforced by the skill and expectations, not by prompt scaffolding.
- Added `gotchas.md` to the runbook reference list.
- Made sensitive-value reading prohibition unconditional in `security-analysis.md`.

Result: subagent review reduced four practical failure modes: generic RCA pass, over-scaffolded mixed-routing eval, missing gotchas smoke evidence, and safety-policy ambiguity. Remaining limit: the staged new files still need to be included in the eventual commit.
