# Review And RCA Rubric

Use this reference for PR-review-style defect analysis, root-cause analysis, incident-style "why did this happen" questions, and mixed "analyze first, fix later" requests.

## PR Review Findings

Every actionable finding should stand on its own:

- Location: tight file:line, method, or call chain.
- Severity: high, medium, or low, with user/operational impact.
- Failure scenario: concrete sequence of events, input, timing, or data shape.
- Evidence: what code path was read and why it implies the scenario.
- Intent inference: why the code may have been written this way.
- Pre-fix checks: tests, logs, metrics, product policy, concurrency constraint, or user decision needed before changing it.

Avoid "style" findings unless the prompt asks for style. Prefer correctness, data loss, race, security, operational risk, regression, and maintainability issues with a plausible failure scenario.

## RCA Workflow

Use a hypothesis ladder instead of jumping to a fix:

1. Symptom: restate the observable failure and affected user path.
2. Boundary: identify what is in scope and what is unknown.
3. Candidate causes: list plausible causes across data, scheduler, transaction, external API, cache/storage, and concurrency.
4. Evidence for/against: cite code paths, logs, tests, or missing evidence for each cause.
5. Most likely cause: only rank if evidence is strong enough; otherwise keep hypotheses open.
6. Confirmation plan: minimal test, log, metric, reproduction, or safe query to separate candidates.
7. Follow-up route: bug-fix if behavior is wrong, refactoring if structure is the issue, testing if evidence is missing.

## Common allcll Defect Patterns

Check these when relevant:

- Batch drain before persistence success can create data loss or retry ambiguity.
- Scheduler cancellation can race with in-flight work or leave stale status.
- In-memory storage and DB can diverge; know which one is source of truth for the user path.
- Per-token or per-subject loops can hide fan-out, N+1, or partial failure.
- External APIs and Google Sheets can be slow, rate-limited, stale, or policy-changing.
- SSE send failures need cleanup and should not poison unrelated emitters.
- Validation changes alter API behavior and should route to bug-fix after analysis.

## Mixed Request Handling

For "분석 후 고쳐줘" or "원인을 찾고 맞으면 수정해줘":

- First produce the analysis artifact with findings and confirmation criteria.
- Do not edit while the root cause is still speculative.
- If the user explicitly asked to continue and the defect is confirmed, load the correct companion skill before editing.
- If the fix changes external behavior, route to `allcll-bug-fix`.
- If the fix preserves behavior and only improves structure/performance, route to `allcll-refactoring`.
- If the next step is adding evidence tests only, route to `allcll-testing`.

## False Positive Control

Before presenting a finding, ask:

- Could this be an intentional constraint such as rate limiting, scheduler cadence, external API tolerance, or product policy?
- Did I read the caller and callee, not only one method?
- Is there a concrete failure scenario, or only a general suspicion?
- Would a test or metric disprove this cheaply?
