# /review-pr $ARGUMENTS

PR 번호를 받아 코드 리뷰를 수행합니다.

## 사용법

```
/review-pr              # 현재 브랜치의 PR 자동 리뷰
/review-pr 91           # PR #91 자동 판단 리뷰
/review-pr 91 approve   # PR #91 강제 승인
/review-pr 91 request-changes  # PR #91 강제 변경 요청
```

## 인자 파싱

`$ARGUMENTS`에서 첫 번째 토큰은 PR 번호, 두 번째 토큰은 리뷰 모드(approve/comment/request-changes)로 분리한다.

```bash
PR_NUMBER=$(echo "$ARGUMENTS" | awk '{print $1}')
MODE=$(echo "$ARGUMENTS" | awk '{print $2}')
```

- `PR_NUMBER`가 비어있으면 현재 브랜치의 PR을 자동 탐색
- `MODE`가 비어있으면 3단계에서 자동 판단

## 실행 순서

### 1. PR 정보 수집

```bash
gh pr view $PR_NUMBER --repo allcll/allcll-backend
gh pr diff $PR_NUMBER --repo allcll/allcll-backend
```

### 2. 변경 사항 분석

모든 변경 파일을 읽고 다음을 검토:

#### 검토 카테고리

| 카테고리           | 심각도   | 설명                               |
|----------------|-------|----------------------------------|
| Bug            | 높음    | 버그 또는 런타임 에러 가능성                 |
| Security       | 높음    | 보안 취약점 (SQL Injection, XSS 등)    |
| Error Handling | 중간    | 에러 처리 누락                         |
| Style          | 낮음    | 코딩 컨벤션 위반 (`AGENTS.md` 및 에이전트별 skill 기준) |
| Suggestion     | 낮음    | 개선 제안                            |
| Nit            | 매우 낮음 | 사소한 지적                           |

### 3. 리뷰 판단 (자동)

강제 옵션이 없는 경우 자동 판단:

| 조건                    | 결과                |
|-----------------------|-------------------|
| Bug 또는 Security 이슈 있음 | `request-changes` |
| 개선 필요 사항만 있음          | `comment`         |
| 문제 없음                 | `approve`         |

### 4. 리뷰 제출

```bash
# MODE 값에 따라 플래그 결정
gh pr review $PR_NUMBER \
  --repo allcll/allcll-backend \
  --approve \
  --body "리뷰 본문"

# 또는
gh pr review $PR_NUMBER \
  --repo allcll/allcll-backend \
  --comment \
  --body "리뷰 본문"

# 또는
gh pr review $PR_NUMBER \
  --repo allcll/allcll-backend \
  --request-changes \
  --body "리뷰 본문"
```

### 5. 인라인 코멘트

심각도 중간 이상 이슈는 해당 라인에 인라인 코멘트 작성:

```bash
gh api repos/allcll/allcll-backend/pulls/$PR_NUMBER/comments \
  --method POST \
  -f body="[카테고리] 코멘트 내용" \
  -f commit_id="커밋SHA" \
  -f path="파일경로" \
  -F line=라인번호
```

## 리뷰 원칙

- 프로젝트 코딩 규칙(`AGENTS.md` 및 에이전트별 skill) 기준으로 검토
- 비난하지 않는 톤, 개선 방향 제시
- 사소한 스타일 지적보다 로직/안전성 우선
