# Architecture Analysis Rubric

Use this reference for package responsibility maps, data-flow analysis, domain boundary review, impact analysis before change, and large-feature/design triage.

## Mapping Workflow

Build a map before making judgments:

1. User-facing path: controller/API, scheduled sender, admin ingestion, or domain service entry point.
2. Data ownership: DB entity, in-memory storage, external source, sheet policy, request/session state.
3. Transformation steps: DTO mapping, filtering, sorting, policy checks, persistence, fan-out.
4. Package responsibility: `domain`, `admin`, `client`, `support`, `config`, and test fixtures.
5. Cross-cutting concerns: transaction boundary, scheduler, cache/storage lifetime, external API, error handling, logging.
6. Change impact: files/packages that must be read together and why.

## allcll Package Boundary Hints

- `domain/`: user-facing domain behavior and policy decisions.
- `admin/`: admin APIs plus crawler data ingestion and batch persistence.
- `client/`: external call adapters.
- `support/`: SSE, scheduler, batch, sheet, semester, and infrastructure helpers.
- `config/`: bean wiring, scans, scheduler/retry/login/sheet configuration.
- `SeatStorage`: in-memory source of truth for SSE seat push.
- Graduation flow: separate sheet policy acquisition from domain policy evaluation.

Treat `allcll-crawler` as a separate repository boundary. Do not cross into it unless the user scope includes it and current safety constraints allow reading it.

## Impact Analysis

For each proposed change area, identify:

- Direct files: methods/classes likely to change.
- Callers and callees: at least one level each when available.
- Tests or fixtures that encode current behavior.
- Operational assumptions: scheduler period, external API rate, sheet policy freshness, SSE user expectations.
- Boundaries that require user confirmation: domain policy, large model redesign, external mock strategy, public API contract.

## Stop Conditions

Stop at analysis or ask the user when:

- The request asks to redesign a domain model, policy engine, or major data flow and also "just implement it."
- The correct behavior depends on product policy, university policy, or external service semantics not present in code.
- The analysis would require reading sensitive local config or secret files.
- The proposed change crosses into another repository or submodule outside the stated scope.

## Output Standard

Architecture analysis should include:

- Scope summary: what was mapped and what was intentionally excluded.
- Flow map: source -> transformation -> sink.
- Responsibility map: package/file groups with reasons.
- Risk map: where coupling, hidden state, or unclear ownership can break behavior.
- Follow-up split: analysis-only, bug-fix, refactoring, testing, or ask-user.
