---
description: GitHub PR을 생성하는 스킬. CI 통과 형식을 강제하고 구조화된 본문으로 자동 생성한다.
---

# PR 생성 스킬

## 사용법

코드 수정 완료 후 PR 생성이 필요할 때 이 스킬을 따른다.

## PR 타이틀 형식

**필수**: `type: 설명` (이모지 금지 — CI 실패 원인)

허용 타입:
- `feat` — 새 기능
- `fix` — 버그 수정
- `docs` — 문서
- `refactor` — 리팩토링
- `test` — 테스트
- `chore` — 기타

## PR 본문 구조

```markdown
## 작업 내용
- 변경 사항 1
- 변경 사항 2

## 고민 지점과 리뷰 포인트
- 리뷰어가 집중적으로 봐야 할 부분

## 관련 이슈
- Closes #이슈번호
```

## 실행 명령

```bash
gh pr create \
  --repo allcll/allcll-backend \
  --base main \
  --title "type: 설명" \
  --assignee @me \
  --body "본문"
```

## AI PR 규칙

- AI가 생성한 PR은 타이틀에 `[AI]` 포함
- `ai-generated` 라벨 자동 부여
- 예: `feat: [AI] 잔여석 변동 알림 기능 추가`

## 사전 검증

PR 생성 전 반드시 확인:
1. `./gradlew clean build` 성공 여부
2. 컴파일 에러 없음
3. 모든 테스트 통과
