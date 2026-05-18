---
description: GitHub 이슈를 분석하고, 코드를 수정한 뒤, PR을 생성하는 자동화 명령어
allowed-tools: Bash, Read, Write, Edit, Glob, Grep, Agent
---

# /fix-issue $ARGUMENTS

이슈 번호 또는 URL을 받아 코드를 수정하고 PR을 생성합니다.

## 실행 순서

### 1. 현재 작업 보존

```bash
git stash --include-untracked -m "fix-issue: 작업 보존"
```

### 2. main 브랜치 최신화

```bash
git checkout main
git pull origin main
```

### 3. 이슈 분석

```bash
gh issue view $ARGUMENTS --repo allcll/allcll-backend
```

이슈의 label을 확인한다:

```bash
gh issue view $ARGUMENTS --repo allcll/allcll-backend --json labels
```

`manual-required` label이 붙어 있으면 **즉시 중지**하고 사용자에게 알린다:
> "이 이슈는 `manual-required` label이 붙어 있어 사람이 직접 작업해야 합니다. (forbidden_paths 수정 등)"

중지 후 원래 브랜치를 복원하고 stash pop한다.

`manual-required`가 아닌 경우 이슈 내용을 분석하여:
- 이슈 타입 판별 (feat, fix, refactor, test, chore, docs)
- 수정 대상 파일 파악
- 구현 방향 결정

### 4. 영향 분석 (제약 사전 체크)

코드 수정 전에 `.claude/config/fix-limits.yaml` 제약을 기준으로 영향 분석:

1. **필요한 파일 목록 작성**: 이슈 해결에 필요한 모든 파일을 나열
2. **제약 체크**:
   - forbidden_paths에 해당하는 파일이 있는가?
   - 수정 파일이 3개를 초과하는가?
   - 파일당 50줄, 함수 2개를 초과하는가?
3. **잔여 작업 분류**:
   - `manual`: forbidden_paths에 해당 (사람이 직접 수정)
   - `ai-follow-up`: 양적 제한 초과 (AI가 후속 PR로 처리)

잔여 작업이 있으면 사용자에게 보고하고, 현재 PR에서 가능한 범위만 먼저 진행.

### 5. 브랜치 생성

이슈 타입에 따라 브랜치명 결정:
- feat: `feat/이슈-요약`
- fix: `fix/이슈-요약`
- refactor: `refactor/이슈-요약`
- 기타: `chore/이슈-요약`

```bash
git checkout -b {브랜치명}
```

### 6. 코드 수정

`.claude/config/fix-limits.yaml`의 제한 사항을 반드시 준수:
- 최대 3개 파일
- 파일당 최대 50줄
- 최대 2개 함수
- `forbidden_paths` 수정 금지

### 7. 검증

```bash
./gradlew compileJava
./gradlew test
```

컴파일 에러 또는 테스트 실패 시 `.claude/config/rollback-triggers.yaml`에 따라 롤백.

### 8. 커밋 & 푸시

```bash
git add -A
git commit -m "type: 설명

Closes #이슈번호"
git push -u origin {브랜치명}
```

### 9. PR 생성

`.claude/skills/create-pr/instruction.md` 스킬을 따라 PR 생성.
AI PR이므로 타이틀에 `[AI]` 포함.

### 10. PR 본문 갱신

추가 push가 발생한 경우, PR 본문을 현재 diff 기준으로 갱신한다.

1. `git diff main...HEAD --stat`로 변경된 파일 목록 확인
2. 변경 파일 목록을 기반으로 PR 본문을 `gh pr edit`으로 직접 수정
3. 변경 사유를 PR 코멘트로 한 줄 남김:
   ```bash
   gh pr comment {PR번호} --body "PR 본문 업데이트: {변경 사유 요약}"
   ```

### 11. 잔여 작업 이슈 생성

4단계에서 잔여 작업이 확인된 경우, 이슈 생성 전에 현재 브랜치의 실제 diff를 확인하여 이미 완료된 작업을 제외한다.

```bash
git diff main...HEAD --stat
```

4단계의 잔여 작업 목록에서 실제 diff에 이미 반영된 항목을 제거하고, 남은 항목만 이슈로 생성한다. 모든 잔여 작업이 이미 완료되었으면 이슈를 생성하지 않는다.

**이슈 생성 규칙**:
- `manual`과 `ai-follow-up`은 별도 이슈로 분리
- 원본 이슈를 참조: `Parent: #이슈번호`
- label 부여: `manual-required` 또는 `ai-follow-up`

**이슈 본문 형식**:

```markdown
## 배경
{원본_이슈_타이틀} (#원본이슈번호) 작업 중 fix-limits 제약으로 처리하지 못한 항목입니다.

## 작업 내용
- [ ] {구체적인 작업 항목}
- [ ] {구체적인 작업 항목}

## 브랜치 전략
- base: `{feature_브랜치명}`
- branch: `{feature_브랜치명}-{번호}`
- PR target: `{feature_브랜치명}`

## 참조
- Parent: #{원본이슈번호}
- PR: #{생성된PR번호}
```

**브랜치 번호 규칙**:
- 자식 브랜치는 `{feature_브랜치명}-1`, `-2`, `-3` ... 순서로 생성
- 자식 PR의 base는 feature 브랜치
- 모든 자식 PR이 merge된 후 feature 브랜치가 main으로 merge

```
feat/admin-auth-filter          # feature branch -> main
feat/admin-auth-filter-1        # [manual] -> feature branch
feat/admin-auth-filter-2        # [ai-follow-up] -> feature branch
```

### 12. 미구현 항목 재평가

11단계 완료 후, 이슈 체크리스트 중 **구현되지 않았고 잔여 이슈로도 분리되지 않은 항목**이 있는지 확인한다.

1. 이슈 체크리스트 항목을 나열
2. 각 항목을 분류:
   - 구현 완료 (diff에 반영됨) -> 통과
   - 잔여 이슈로 분리됨 (11단계에서 생성) -> 통과
   - **어디에도 해당하지 않음** -> 사용자에게 확인 필요
3. 해당 항목이 있으면 사용자에게 질문:
   > "이슈 체크리스트 중 다음 항목이 구현되지 않았고 잔여 이슈로도 분리되지 않았습니다:
   > - {항목}
   > 방향 변경으로 불필요한 건가요, 아니면 잔여 이슈로 생성할까요?"
4. 사용자 응답에 따라:
   - **불필요**: PR 본문의 제한 사항 섹션에 사유와 함께 취소선 표기 (예: `~~항목~~ -> {불필요 사유}`)
   - **잔여 이슈 필요**: 11단계 규칙에 따라 이슈 생성

### 13. 부모 PR 본문 갱신

현재 이슈가 자식 이슈인 경우 (본문에 `Parent: #번호`와 `PR: #번호`가 있는 경우), 부모 PR 본문을 갱신한다.

1. 이슈 본문에서 부모 PR 번호를 추출
2. 부모 PR 본문에서 현재 이슈에 해당하는 잔여 작업 항목을 찾아 취소선 처리 + 자식 PR 번호 링크 추가:
   ```
   ~~{잔여 작업 항목}~~ -> #{자식PR번호} 에서 완료
   ```
3. 부모 PR에 코멘트 남김:
   ```bash
   gh pr comment {부모PR번호} --body "잔여 작업 반영: #{자식PR번호} 에서 {작업 요약} 완료"
   ```

### 14. 원래 브랜치 복원

```bash
git checkout {원래_브랜치}
git stash pop
```

## 실패 시

- 코드 수정이 검증을 통과하지 못하면 변경 사항을 되돌리고 원래 브랜치로 복원
- 사용자에게 실패 원인 보고
