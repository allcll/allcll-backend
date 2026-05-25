# AGENTS.md

@docs/agent-harness/PROJECT.md

## Codex

이 파일은 Codex 전용 하네스 입구다. 프로젝트 공통 규칙은 위의 `@docs/agent-harness/PROJECT.md` 로 가져오며, 이 파일에는 Codex 에만 필요한 안내만 둔다.

## Codex 전용 위치

- command wrapper: `.agents/commands/`
- skill discovery wrapper: `.agents/skills/`

## 공통 본문 위치

- 프로젝트 컨텍스트: `docs/agent-harness/PROJECT.md`
- command 본문: `docs/agent-harness/commands/*.md`
- skill 본문: `docs/agent-harness/skills/*/SKILL.md`
- skill eval: `docs/agent-harness/skills/*/evals/evals.json`

`.agents/commands/*.md` 와 `.agents/skills/*/SKILL.md` 는 Codex 가 command/skill 절차를 찾기 위한 얇은 wrapper 다. 실제 절차나 가이드를 바꿀 때는 wrapper 를 길게 만들지 말고 `docs/agent-harness/` 아래의 공통 본문을 수정한다.

Codex 는 Claude Code 의 slash command 시스템을 직접 실행하지 않는다. 사용자가 `/create-pr`, `/create-issue`, `/review-pr` 처럼 요청하면 아래 command wrapper 를 통해 공통 command 본문을 따른다.

## Command 매핑

- `/create-pr` → `.agents/commands/create-pr.md` → `docs/agent-harness/commands/create-pr.md`
- `/create-issue` → `.agents/commands/create-issue.md` → `docs/agent-harness/commands/create-issue.md`
- `/review-pr` → `.agents/commands/review-pr.md` → `docs/agent-harness/commands/review-pr.md`

## Skill 매핑

- `allcll-analysis` → `.agents/skills/analysis/SKILL.md` → `docs/agent-harness/skills/analysis/SKILL.md`
- `allcll-bug-fix` → `.agents/skills/bug-fix/SKILL.md` → `docs/agent-harness/skills/bug-fix/SKILL.md`
- `allcll-refactoring` → `.agents/skills/refactoring/SKILL.md` → `docs/agent-harness/skills/refactoring/SKILL.md`
- `allcll-testing` → `.agents/skills/testing/SKILL.md` → `docs/agent-harness/skills/testing/SKILL.md`

## 수정 원칙

- 공통 프로젝트 규칙은 `docs/agent-harness/PROJECT.md` 에 수정한다.
- Codex 에만 필요한 안내는 이 파일에 수정한다.
- command/skill 본문은 `docs/agent-harness/` 에서 한 번만 관리한다.
- `.agents/` 아래 wrapper 에 공통 절차를 복사해 넣지 않는다.
