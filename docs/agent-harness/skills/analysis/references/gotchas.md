# Analysis Gotchas

Use this reference when the request mixes analysis with possible implementation, touches scheduler/batch/SSE/concurrency, includes security risk, relies on subagent summaries, or asks for benchmark/capacity claims.

## Trigger And Routing

- The wrapper description is the trigger surface. Rules only in the common body cannot rescue a bad trigger decision.
- "성능 개선해줘" is ambiguous. Diagnose unknown bottlenecks with analysis first; apply a behavior-preserving known optimization with refactoring; fix externally wrong behavior with bug-fix.
- "분석 후 고쳐줘" is staged work. Produce findings and confirmation criteria before loading a companion implementation skill.
- Tests-only requests route to testing even if the tests are meant to prove an analysis hypothesis.

## Evidence

- Do not turn a suspicion into a finding unless it has a code location, scenario, and pre-fix check.
- Read both caller and callee for scheduler, batch, SSE, persistence, and external API paths.
- Treat `Thread.sleep`, fixed scheduler periods, retries, and TODOs as possible product or external-service constraints.
- If a finding depends on workload size, emitter count, token count, DB cardinality, or hardware, name the missing measurement instead of inventing a number.

## Safety

- Security review does not require opening secrets. Use property names, config classes, call paths, log sinks, endpoint behavior, and policy files.
- Never read or print `application-local.yml`, `.env`, credentials, service account JSON, token files, private keys, or local-only secret config.
- Analysis-only work should not create production, test, or docs diffs. If a tool may have written files, check the diff before final response.
- Do not cross into `allcll-crawler` unless the user explicitly includes that repository in scope.

## Review Quality

- PR review findings should prioritize correctness, data loss, race, security, regression, and operational risk over style.
- RCA should compare candidate causes. A single guessed root cause is not enough when logs/tests are absent.
- Subagent classifications can hide relevant files. Before accepting a categorized summary, inspect at least one key method in each critical flow.
- A non-discriminating eval pass is still useful as regression evidence, but it does not prove improvement.
