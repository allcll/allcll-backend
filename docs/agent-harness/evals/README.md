# Agent Harness Evals

이 디렉터리는 allcll agent harness skill 개선을 측정하기 위한 공통 평가 기반을 담는다.

## Structure

```text
docs/agent-harness/evals/
├── README.md
├── failure-taxonomy.md
├── discovery-static-audit.md
├── schemas/
│   └── skill-eval.schema.md
└── rubrics/
    └── README.md
```

## Phase 2A Scope

Phase 2A는 evaluation runner 전체를 만들지 않는다. 대신 다음 기반만 고정한다.

- failure taxonomy
- 기존 `evals.json`과 호환되는 schema
- discovery/load static audit 기준
- benchmark artifact 저장 정책
- static audit script

## Related Files

- `docs/agent-harness/scripts/check-skill-discovery.sh`
- `docs/agent-harness/benchmarks/artifact-policy.md`
- `docs/agent-harness/skills/skill-improvement-audit.md`

