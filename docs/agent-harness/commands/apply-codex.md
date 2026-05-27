# /apply-codex $ARGUMENTS

PR의 Codex 리뷰를 수집하고, 적용/보류/거부로 분류한 뒤, 사람 확인을 거쳐 코드를 수정하고 리뷰 스레드를 정리하는 로컬 커맨드.

## 사용법

```
/apply-codex 338
/apply-codex https://github.com/allcll/allcll-backend/pull/338
```

## 전제 조건

- 현재 디렉토리가 allcll-backend 레포
- 해당 PR의 브랜치가 로컬에 checkout 되어 있어야 함
- PR 본문에 `## Codex Review` 섹션이 존재해야 함

## 실행 순서

### 1. PR 번호 파싱

`$ARGUMENTS`에서 PR 번호를 추출한다. URL이면 마지막 숫자를 파싱.

```bash
PR_NUMBER=$(echo "$ARGUMENTS" | grep -oE '[0-9]+$')
```

PR 번호가 비어있으면 중단:
> "PR 번호를 입력해주세요. 예: /apply-codex 338"

### 2. PR 상태 확인

```bash
gh pr view $PR_NUMBER --repo allcll/allcll-backend --json body,headRefName,state
```

확인 사항:
- PR이 open 상태인지
- 현재 로컬 브랜치가 PR의 `headRefName`과 일치하는지 (불일치 시 사용자에게 경고)
- PR 본문에 `## Codex Review` 섹션이 있는지 (없으면 "Workflow 1이 아직 실행되지 않았습니다. PR 본문을 확인해주세요." 출력 후 중단)
- PR 본문에 `- [x]` `/apply-codex``가 이미 체크되어 있는지 (체크됨이면 "이미 처리된 PR입니다. 다시 실행하시겠습니까?" 확인)

### 3. Codex 리뷰 스레드 수집

GraphQL API로 미resolve 리뷰 스레드를 가져온다.

```bash
gh api graphql -f query='
  query($owner: String!, $repo: String!, $number: Int!) {
    repository(owner: $owner, name: $repo) {
      pullRequest(number: $number) {
        reviewThreads(first: 50) {
          nodes {
            id
            isResolved
            comments(first: 1) {
              nodes {
                author { login }
                body
                path
                line
              }
            }
          }
        }
      }
    }
  }
' -f owner="allcll" -f repo="allcll-backend" -F number=$PR_NUMBER
```

**필터링**:
- `isResolved: false`인 스레드만 대상
- PR 작성자의 댓글은 제외

리뷰가 0건이면 "처리할 Codex 리뷰가 없습니다" 출력 후 8단계(체크박스 체크)로 바로 이동.

### 4. 리뷰 분류

수집한 리뷰 각각을 코드를 읽고 다음 3가지로 분류한다.

| 분류 | 기준 | 예시 |
|------|------|------|
| **적용** | 코드에 실제 버그, 누락, 명백한 개선점 | null 체크 누락, 리소스 미해제, 동시성 버그 |
| **보류** | 타당하지만 현재 PR 범위 밖이거나 사이드이펙트 판단 필요 | 대규모 리팩토링 제안, 아키텍처 변경 |
| **거부** | 의도된 설계이거나 코드 컨텍스트를 잘못 이해한 리뷰 | 의도적 동기 처리를 비동기로 바꾸라는 제안 |

분류 시 반드시 해당 파일의 코드를 읽어서 컨텍스트를 파악한 뒤 판단한다.

### 5. 분류 결과 표시

다음 형식으로 출력:

```
## Codex 리뷰 분류 결과 (PR #338)

### 적용 (N건)
  1. [적용] src/domain/seat/SeatService.java:42
     리뷰: "이 메서드는 null 체크가 필요합니다"
     이유: findById 반환값이 Optional이 아니라 실제 NPE 가능성 있음

### 보류 (N건)
  2. [보류] src/domain/seat/SeatRepository.java:28
     리뷰: "쿼리 최적화가 필요합니다"
     이유: 성능 영향 범위가 넓어 별도 이슈로 분리 권장

### 거부 (N건)
  없음
```

### 6. 사용자 확인

분류 결과를 보여준 뒤 사용자의 선택을 기다린다.

| 입력 | 동작 |
|------|------|
| `전부 적용` 또는 `y` | 적용으로 분류된 모든 리뷰를 반영 |
| `1,2` (번호 지정) | 해당 번호의 리뷰만 반영 |
| `1 거부, 3 적용` (분류 변경) | 분류를 변경한 뒤 적용 진행 |
| `취소` 또는 `n` | 아무것도 하지 않고 중단 |

사용자가 특정 리뷰에 대해 질문하면 해당 코드를 읽고 추가 설명을 제공한다.

### 7. 코드 수정 + 검증

사용자가 승인한 리뷰에 대해 코드를 수정한다.

**제약 사항**:
- `CLAUDE.md`의 `forbidden_paths` 수정 금지
- 수정 후 반드시 컴파일 검증:

```bash
./gradlew compileJava
```

**컴파일 실패 시**:
- 수정 내용을 되돌린다: `git checkout -- .`
- 사용자에게 실패 원인을 보고한다
- 체크박스를 체크하지 않고 중단한다

### 8. 커밋 + push

적용 대상 리뷰를 하나의 커밋으로 묶는다.

```bash
git add {수정된 파일들}
git commit -m "refactor: apply Codex review feedback

Applied reviews:
- {파일}:{라인} -- {수정 요약}
- {파일}:{라인} -- {수정 요약}

Codex review for PR #{PR_NUMBER}"
git push
```

### 9. 리뷰 스레드 귀속 댓글 + resolve

각 리뷰 스레드에 처리 결과를 남긴다. GraphQL `addPullRequestReviewThreadReply` mutation으로 댓글을 추가하고, 필요 시 `resolveReviewThread`로 resolve한다.

| 판단 | 댓글 | resolve |
|------|------|:---:|
| 적용 | `Resolved by Claude (커밋 {sha}) -- 리뷰 반영 완료` | O |
| 보류 | `Flagged by Claude -- 사람 확인 필요: {사유}` | X |
| 거부 | `Dismissed by Claude -- {사유}. @leejin 이의 있으면 reopen` | O |

```bash
# 댓글 추가
gh api graphql -f query='
  mutation($threadId: ID!, $body: String!) {
    addPullRequestReviewThreadReply(input: {pullRequestReviewThreadId: $threadId, body: $body}) {
      comment { id }
    }
  }
' -f threadId="$THREAD_ID" -f body="$COMMENT_BODY"

# resolve (적용/거부만)
gh api graphql -f query='
  mutation($threadId: ID!) {
    resolveReviewThread(input: {threadId: $threadId}) {
      thread { isResolved }
    }
  }
' -f threadId="$THREAD_ID"
```

### 10. PR 본문 체크박스 체크

PR 본문의 체크박스를 체크하여 Workflow 2(리뷰어 배정)를 트리거한다.

```bash
BODY=$(gh pr view $PR_NUMBER --json body -q .body)
NEW_BODY=$(echo "$BODY" | sed 's/- \[ \] `\/apply-codex`/- [x] `\/apply-codex`/')
gh pr edit $PR_NUMBER --body "$NEW_BODY"
```

보류 항목이 있어도 체크박스는 체크한다. 보류 스레드는 `isResolved: false`로 남아있으므로 사람 리뷰어가 확인할 수 있다.

## 엣지 케이스

| 상황 | 동작 |
|------|------|
| Codex 리뷰 0건 | "처리할 Codex 리뷰가 없습니다" 출력 후 체크박스만 체크 |
| 모든 리뷰를 거부 | 코드 수정 없이 거부 댓글만 남기고 체크박스 체크 |
| 모든 리뷰를 보류 | 코드 수정 없이 보류 댓글만 남기고 체크박스 체크 |
| 컴파일 실패 | `git checkout -- .`로 되돌리고 실패 원인 보고. 체크박스 미체크 |
| 이미 체크박스가 체크됨 | "이미 처리된 PR입니다. 다시 실행하시겠습니까?" 확인 |
| PR 본문에 Codex Review 섹션 없음 | "Workflow 1이 아직 실행되지 않았습니다" 출력 후 중단 |
