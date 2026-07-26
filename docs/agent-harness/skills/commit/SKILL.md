# 올클 커밋 생성 가이드

이 skill은 사용자가 "커밋 찍어줘", "commit 해줘", "커밋 메시지 만들어줘", "작업단위 나눠서 커밋해줘"처럼 git commit 생성을 요청할 때 사용한다.

## 근거

2026-01 ~ 2026-02 `main` 커밋을 기준으로 한 컨벤션:

- 기본 형식은 `type: 한국어 요약`
- `type`은 영어 소문자, 본문은 한국어가 기본
- 작업단위는 작게 쪼갠다. 예: 엔티티 추가, DTO 추가, 레포지토리 추가, 서비스 구현, API 구현, 테스트 추가가 별도 커밋으로 자주 나뉨
- 리팩토링은 책임 분리, 네이밍 개선, 중복 제거, 패키지 이동처럼 의미 단위별로 분리
- 테스트만 수정한 경우 `test: ...` 커밋을 별도로 둠
- merge commit, bot commit, `Initial plan`, 영어-only `Update ...`, 임시커밋은 참고하지 않는다

## 커밋 전 필수 절차

1. `git status --short --branch`로 현재 브랜치와 변경 파일을 확인한다.
2. `git diff --stat`, `git diff --name-status`, 필요하면 파일별 diff를 읽고 변경을 논리 단위로 분류한다.
3. 내가 만들지 않은 변경, 서브모듈 변경, `.DS_Store`, IDE 파일, credentials 류는 사용자 요청 없이는 포함하지 않는다.
4. 변경 단위가 2개 이상이면 커밋을 나눈다. 한 커밋이 자연스러운 경우에만 단일 커밋을 만든다.
5. 커밋마다 staged diff를 확인한 뒤 커밋한다.

## 브랜치 최신화 판단

- 커밋 전 현재 브랜치의 upstream, ahead/behind 상태를 확인한다.
- 브랜치가 오래됐거나 `main`보다 뒤처졌거나 PR 직전이면, 커밋 전에 최신화 필요 여부를 사용자에게 보고한다.
- `git pull`, `merge`, `rebase`는 자동 실행하지 않는다. 사용자 요청 또는 명시 승인 후 진행한다.
- 방금 `origin/main`에서 만든 브랜치이거나 문서/하네스 단독 변경처럼 충돌 위험이 낮으면 커밋을 먼저 진행해도 된다.

## 작업단위 분할 기준

다음 중 하나라도 다르면 별도 커밋 후보로 본다:

- 기능 흐름 단계가 다름: entity/schema, repository, dto, service, api/controller, config, test
- 변경 목적이 다름: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `delete`
- 도메인이 다름: seat, graduation, user, auth, scheduler 등
- 리팩토링과 동작 변경이 섞임
- 테스트/픽스처 변경이 프로덕션 변경과 독립적으로 설명 가능함

함께 묶어도 되는 경우:

- 하나의 버그 수정과 그 버그를 고정하는 작은 회귀 테스트
- 같은 클래스/메서드의 이름 변경과 그에 따른 호출부 기계적 수정
- 설정 파일과 이를 읽는 코드가 서로 없으면 깨지는 단일 변경

## 메시지 컨벤션

형식:

```text
type: 한국어 요약
```

허용 타입:

- `feat`: 새 기능, API, 도메인/DTO/Repository/Service 추가
- `fix`: 버그 수정, 잘못된 응답/로직/데이터 보정
- `refactor`: 동작 보존 구조 개선, 네이밍, 책임 분리, 중복 제거
- `test`: 테스트 추가·수정, fixture 추가
- `docs`: 문서만 변경
- `chore`: 빌드, 설정, 의존성, 하네스 관리 등 제품 동작과 직접 무관한 작업
- `delete`: 사용하지 않는 코드/파일 제거가 핵심인 경우
- `hotfix`: 긴급 운영 수정일 때만 사용

작성 규칙:

- 본문은 한국어로 작성한다. 영어-only 메시지 금지.
- 타입은 소문자로 쓴다. 과거의 `Feat:`/`Fix:`는 참고하지 않는다.
- 제목은 한 줄로 충분히 구체적으로 쓴다.
- `Codex`, `AI`, `with Claude`, `generated` 같은 생성 주체를 넣지 않는다.
- 티켓 번호는 사용자가 요청했거나 기존 브랜치/작업 흐름에서 명시적으로 요구될 때만 넣는다. 기본은 넣지 않는다.
- 임시성 표현 금지: `임시커밋`, `eof`, `충돌 해결`, `수정`, `작업`처럼 내용이 안 보이는 제목은 피한다.

좋은 예:

```text
feat: 사용자 리뷰 저장 엔티티 생성
feat: 사용자 리뷰 저장 서비스 및 dto 생성
feat: 사용자 리뷰 조회 admin api 생성
fix: 졸업요건검사Post API 응답body 삭제
fix: 대체 과목 매핑 누락 시 NPE 방어 및 로그 추가
refactor: 졸업요건 검사를 저장된 기이수 데이터 기반으로 수행하도록 변경
refactor: 대체과목 판별 로직 `englishCertCriterion`로 캡슐화
test: 영어 인증 대체 과목 정책에 따른 테스트 코드 작성
docs: 커밋 생성 가이드 추가
```

나쁜 예:

```text
feat: implement review api
Feat: 사용자 리뷰 저장 엔티티 생성
feat: eof
feat: 충돌 해결
chore: changes
[TSK-126] all changes with Codex
```

## staging 원칙

- 파일 단위로 안전하게 나뉘면 `git add <files>`를 사용한다.
- 한 파일에 여러 논리 변경이 섞였으면 `git diff <file>`을 읽고, 안전하게 나눌 수 있을 때만 `git add -p` 등으로 hunk staging을 고려한다.
- hunk를 나누면 컴파일이 깨질 위험이 있는 변경은 억지로 쪼개지 말고 한 커밋으로 묶은 이유를 사용자에게 설명한다.
- 커밋 직전 `git diff --cached --stat`와 `git diff --cached --name-status`로 staged 범위를 확인한다.
- 커밋 후 남은 변경이 있으면 `git status --short`로 다음 커밋 후보를 다시 분류한다.

## 커밋 후 보고

최종 응답에는 다음을 짧게 포함한다:

- 생성한 커밋 해시와 메시지
- 커밋을 여러 개로 나눈 기준
- 포함하지 않은 변경이 있으면 그 목록과 이유
- 테스트/검증을 새로 실행하지 않았다면 그 사실
