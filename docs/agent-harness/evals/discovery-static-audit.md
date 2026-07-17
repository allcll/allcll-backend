# Discovery Static Audit

Phase 2A static audit 기준. 이 검사는 skill 실행 품질을 평가하기 전에 wrapper와 canonical body 구조가 측정 가능한 상태인지 확인한다.

## Scope

대상 surface:

- Codex wrappers: `.agents/skills/*/SKILL.md`
- Claude wrappers: `.claude/skills/*/SKILL.md`
- Canonical bodies: `docs/agent-harness/skills/*/SKILL.md`

공통 원칙:

- `.agents/`와 `.claude/`에는 discovery용 얇은 wrapper만 둔다.
- 공통 절차는 `docs/agent-harness/skills/*/SKILL.md`에서 관리한다.
- canonical body는 skill frontmatter를 가지지 않는다.

## Static Checks

1. Wrapper frontmatter exists.
   - `.agents/skills/*/SKILL.md`와 `.claude/skills/*/SKILL.md`는 YAML frontmatter로 시작해야 한다.
   - `name`과 `description`이 있어야 한다.
2. Wrapper target exists.
   - wrapper 안의 `@../../../docs/agent-harness/skills/<skill>/SKILL.md` 대상 파일이 존재해야 한다.
3. Canonical body has no skill frontmatter.
   - `docs/agent-harness/skills/*/SKILL.md`는 `---`로 시작하면 안 된다.
4. No duplicate skill names within a surface.
   - `.agents` 안에서 같은 `name`이 중복되면 fail.
   - `.claude` 안에서 같은 `name`이 중복되면 fail.
   - `.agents`와 `.claude` 사이의 같은 이름은 의도된 mirror이므로 fail이 아니다.
5. Missing benchmark references are detected.
   - canonical body가 `docs/agent-harness/.../benchmark.json` 경로를 참조하면 해당 파일이 존재해야 한다.
   - 존재하지 않으면 stale benchmark reference로 fail.

## Script

스크립트:

```bash
docs/agent-harness/scripts/check-skill-discovery.sh
```

실행:

```bash
./docs/agent-harness/scripts/check-skill-discovery.sh
```

기대 결과:

- 모든 구조 문제가 없으면 exit code `0`.
- 하나라도 문제가 있으면 stderr/stdout에 `[FAIL]` 항목을 출력하고 exit code `1`.

현재 Phase 2A 기준으로는 bug-fix/refactoring skill body의 missing benchmark link가 탐지될 수 있다. 이 경우 스크립트 실패는 스크립트 문제가 아니라 기존 stale reference를 발견한 결과다.

## Not Covered

이 static audit은 다음을 보장하지 않는다.

- fresh session에서 자동 trigger가 실제로 발생하는지
- Codex가 nested cwd에서 `.agents/skills`를 discovery하는지
- wrapper import가 런타임에서 실제 본문으로 주입되는지
- skill이 작업 품질을 개선하는지

위 항목은 Phase 2B 이후 fresh-session smoke/eval로 검증한다.

