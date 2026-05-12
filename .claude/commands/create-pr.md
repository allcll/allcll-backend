---
description: GitHub PR 생성. CI 통과 형식 강제, AI PR 규칙 자동 적용.
argument-hint: [ PR 제목 또는 작업 요약 ]
---

# PR 생성

코드 수정 완료 후 PR 생성이 필요할 때 이 절차를 따른다.

## 1단계: 사전 검증

다음 중 하나를 먼저 확인하고, 통과하지 못하면 PR 생성을 멈추고 사용자에게 알린다:

- 빠른 검증 (권장): 컴파일 에러 없는지 + 변경 파일 관련 테스트만 통과
  ```bash
  ./gradlew compileJava compileTestJava
  ./gradlew test --tests "*변경된클래스*"
  ```
- 풀 검증 (사용자가 명시 요청 시): `./gradlew clean build`

CI 가 어차피 풀 빌드를 돌리므로, 로컬에서 매번 풀 빌드는 비효율. 빠른 검증이 디폴트.

## 2단계: PR 타이틀 형식

**필수**: `type: 설명` (이모지 금지 — CI 실패 원인)

허용 타입:

- `feat` — 새 기능
- `fix` — 버그 수정
- `docs` — 문서
- `refactor` — 리팩토링
- `test` — 테스트
- `chore` — 기타

## 3단계: 본문 작성

```markdown
## 작업 내용

- 변경 사항 1
- 변경 사항 2

## 고민 지점과 리뷰 포인트

- 리뷰어가 집중적으로 봐야 할 부분

## 관련 이슈

- Closes #이슈번호
```

## 4단계: AI PR 규칙

Claude(나) 가 생성한 PR 은 반드시:

- 타이틀에 `[AI]` 포함 — 예: `feat: [AI] 잔여석 변동 알림 추가`
- `ai-generated` 라벨 부여

## 5단계: 실행

```bash
gh pr create \
  --repo allcll/allcll-backend \
  --base main \
  --title "type: [AI] 설명" \
  --label "ai-generated" \
  --assignee @me \
  --body "본문"
```

생성 후 PR URL 을 사용자에게 보여준다.
