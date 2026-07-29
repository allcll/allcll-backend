# Benchmark Artifact Policy

이 문서는 allcll agent harness skill benchmark 산출물 저장 정책을 정의한다.

## Problem

현재 `bug-fix`와 `refactoring` canonical `SKILL.md`는 다음 benchmark 경로를 근거로 참조한다.

```text
docs/agent-harness/skills/bug-fix-workspace/iteration-1/benchmark.json
docs/agent-harness/skills/refactoring-workspace/iteration-1/benchmark.json
```

Phase 2A 기준으로 위 파일들은 저장소에 존재하지 않는다. 따라서 현재 `SKILL.md`의 100% 개선 주장은 역사적 메모로는 유용하지만, 저장소 안에서 재현 가능한 benchmark evidence로는 취급할 수 없다.

이번 단계에서는 대상 `SKILL.md` 본문을 수정하지 않는다. 대신 stale reference를 static audit으로 탐지하고, 다음 단계에서 정리할 정책을 둔다.

## Storage Policy

권장 저장 위치:

```text
docs/agent-harness/benchmarks/
└── <skill-name>/
    └── <iteration-or-date>/
        ├── benchmark.json
        ├── manifest.json
        ├── summary.md
        └── representative-failures.md
```

예:

```text
docs/agent-harness/benchmarks/allcll-bug-fix/iteration-1/benchmark.json
docs/agent-harness/benchmarks/allcll-refactoring/iteration-1/benchmark.json
```

## What To Commit

Commit:

- `benchmark.json`: aggregate pass rates, run counts, configuration ids.
- `manifest.json`: repository commit, skill snapshot hash, model, sandbox, runner version, suite name.
- `summary.md`: human-readable interpretation and limitations.
- `representative-failures.md`: redacted examples of important failures.

Do not commit by default:

- full transcripts for every run
- large raw logs
- secrets or environment dumps
- generated worktrees
- local credential-dependent outputs

Full transcripts may be retained outside git if needed. If a transcript excerpt is committed, redact sensitive data and include only the minimal evidence required.

## Required Metadata

Each benchmark run set should record:

- skill name
- suite name
- eval file path and checksum
- repository commit SHA
- skill wrapper path and checksum
- canonical body path and checksum
- model/executor identity
- grader identity, if used
- run count
- started_at and completed_at
- sandbox and approval settings
- network allowed or blocked
- environment failures separated from skill failures
- token/time/tool metrics, if available

## Stale Reference Handling

Phase 2B should choose one of these options:

1. Restore old artifacts into the new benchmark location and update `SKILL.md` links.
2. If old artifacts cannot be restored, move old claims into `references/measurement-history.md` as historical notes and remove direct benchmark claims from the main workflow.
3. Re-run the old evals under the new schema and publish fresh benchmark artifacts.

Until one of these is done, static audit should keep reporting missing benchmark references.

## Relationship To Eval Suites

Benchmark artifacts are outputs of repeated eval runs. Eval definitions live with each skill:

```text
docs/agent-harness/skills/<skill>/evals/
```

Benchmark outputs live under:

```text
docs/agent-harness/benchmarks/<skill>/
```

This separation keeps stable test definitions close to the skill while keeping run artifacts out of the skill body directory.

