# CLAUDE.md

@docs/agent-harness/PROJECT.md

## Claude Code

이 파일은 Claude Code 전용 하네스 입구다. 프로젝트 공통 규칙은 위의 `@docs/agent-harness/PROJECT.md` 로 가져오며, 이 파일에는 Claude Code 에만 필요한 안내만 둔다.

## Claude 전용 위치

- 권한 설정: `.claude/settings.json`
- slash command wrapper: `.claude/commands/`
- skill discovery wrapper: `.claude/skills/`

## 공통 본문 위치

- 프로젝트 컨텍스트: `docs/agent-harness/PROJECT.md`
- command 본문: `docs/agent-harness/commands/*.md`
- skill 본문: `docs/agent-harness/skills/*/SKILL.md`
- 도메인 코드 가이드 (타입 1 지식): `docs/agent-harness/knowledge/<도메인>/` (구 `.claude/knowledge` — 2026-07-12 이동)
- skill eval: `docs/agent-harness/skills/*/evals/evals.json`

`.claude/commands/*.md` 와 `.claude/skills/*/SKILL.md` 는 Claude Code 가 command/skill 을 발견하기 위한 얇은 wrapper 다. 실제 절차나 가이드를 바꿀 때는 wrapper 를 길게 만들지 말고 `docs/agent-harness/` 아래의 공통 본문을 수정한다.

## Command 매핑

- `/create-pr` → `.claude/commands/create-pr.md` → `docs/agent-harness/commands/create-pr.md`
- `/create-issue` → `.claude/commands/create-issue.md` → `docs/agent-harness/commands/create-issue.md`
- `/review-pr` → `.claude/commands/review-pr.md` → `docs/agent-harness/commands/review-pr.md`

> 졸업요건 QA 처리는 별도 명시 command 없이 **자연어 자동 발동**만 지원한다 (`allcll-graduation-qa` skill — 아래 매핑 참조). 트리거: "QA", "후기", "신고", "오류", 특정 학번 오판 분석 등.

## Skill 매핑

- `allcll-analysis` → `.claude/skills/analysis/SKILL.md` → `docs/agent-harness/skills/analysis/SKILL.md`
- `allcll-bug-fix` → `.claude/skills/bug-fix/SKILL.md` → `docs/agent-harness/skills/bug-fix/SKILL.md`
- `allcll-refactoring` → `.claude/skills/refactoring/SKILL.md` → `docs/agent-harness/skills/refactoring/SKILL.md`
- `allcll-testing` → `.claude/skills/testing/SKILL.md` → `docs/agent-harness/skills/testing/SKILL.md`
- `allcll-graduation-qa` → `.claude/skills/graduation-qa/SKILL.md` → `docs/agent-harness/skills/graduation-qa/SKILL.md`
- `graduation-backend` → `.claude/skills/graduation-backend/SKILL.md` → `docs/agent-harness/skills/graduation-backend/SKILL.md` (졸업요건 코드 게이트)

## 수정 원칙

- 공통 프로젝트 규칙은 `docs/agent-harness/PROJECT.md` 에 수정한다.
- Claude Code 에만 필요한 설정은 `.claude/settings.json` 또는 이 파일에 수정한다.
- command/skill 본문은 `docs/agent-harness/` 에서 한 번만 관리한다.
- `.claude/` 아래 wrapper 에 공통 절차를 복사해 넣지 않는다.
