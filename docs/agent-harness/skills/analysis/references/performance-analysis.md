# Performance Analysis Rubric

Use this reference for latency, throughput, scheduler drift, peak-time load, SSE fan-out, batch persistence, DB query, external API, and memory/allocation analysis.

## Evidence Map

Start by mapping the hot path before judging it:

1. Entry point: controller, scheduled sender, batch flush, repository call, or external adapter.
2. Data source of truth: in-memory `SeatStorage`, DB, external API, Google Sheets, request body, or scheduler state.
3. Loop and fan-out points: per-subject, per-token, per-emitter, per-page, per-policy, or per-batch iteration.
4. Blocking points: DB, network, synchronized/lock, scheduler thread, executor pool, serialization, or logging.
5. Exit path: SSE send, persistence, API response, cache update, or status flag.

Do not infer peak capacity from code shape alone. Capacity claims need workload shape, event frequency, payload size, emitter count, DB size, hardware/runtime, and measurement data.

## Required Axes

Cover these axes unless the prompt explicitly narrows the scope. If an axis is not relevant, say why.

- CPU/allocation: repeated filtering, sorting, mapping, JSON serialization, DTO construction, string building, log formatting, large collection copying.
- Threading/scheduler: fixed delay vs fixed rate, task duration vs period, thread pool saturation, blocking work inside scheduler threads, virtual-thread handoff, cancellation races, stale futures.
- I/O: DB query count, query shape, transaction boundary, external API latency, Google Sheets fetch, SSE network send, retry behavior.
- N+1/bulk access: loop-inside-query, per-token repository calls, per-subject lookups, repeated sheet calls, missing batching.
- Domain timing: course registration peaks, 3-second seat push expectations, preseat vs regular seat flow, pin notification fan-out, crawler ingestion vs user push separation.

## allcll-Specific Questions

- Is `SeatStorage` being treated as the SSE source of truth, not the DB?
- Does the analysis distinguish "crawler ingestion" from "user push" scheduling?
- Are batch flush failure modes separated from steady-state read performance?
- Does pin-specific logic multiply work by token, subject, emitter, or DB query?
- Is graduation policy analysis separating sheet fetch/cache cost from domain policy evaluation?
- Are scheduler status flags, in-flight tasks, and cancellation semantics checked together?

## Measurement Suggestions

Prefer concrete measurement proposals over guessed numbers:

- p50/p95/p99 duration for scheduled senders and `SseService.propagate`.
- Scheduler drift: actual interval minus configured interval under load.
- Emitter count, token count, subject count, payload bytes, and serialized event size.
- DB query count and slow query logs for pin and graduation flows.
- Allocation profile or heap pressure around repeated sort/map/serialization.
- Failure/cleanup rate for SSE emitters and send exceptions.

## Output Standard

For each performance finding include:

- Location: file:line or method.
- Bottleneck axis: CPU, thread, I/O, N+1, domain timing, or mixed.
- Scenario: concrete load shape that makes it visible.
- Evidence: code path or data flow that creates the risk.
- Unknowns: missing measurement needed before capacity or improvement claims.
- Next step: benchmark, metric, log, test, or follow-up refactoring/bug-fix split.
