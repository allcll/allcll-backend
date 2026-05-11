# CLAUDE.md

allcll-backend (세종대 수강신청 도우미) 작업 시 가이드.

## 프로젝트

- **스택**: Java 21, Spring Boot 3.4, JPA, MySQL(운영) / H2(테스트)
- **구조**: Gradle 멀티 프로젝트 — 루트 + `:crawler` 서브모듈 (별도 git repo)

## 빌드·실행

```bash
./gradlew clean build              # 전체 빌드 + 테스트 (CI 와 동일)
./gradlew test --tests "FQCN"      # 단일 테스트
./gradlew bootRun                  # 로컬 실행 (profile=local 자동)
```

`application-local.yml` 에 MySQL `allcll_local`, 세종대 포털/TOSC URL, Google Sheets 자격증명, admin 토큰 포함. 빌드
환경에 맞게 덮어써야 함.

테스트는 H2 인메모리 + `app.runner.enabled=false` → **부팅 시 스케줄러 자동 시작 안 됨**.

## 핵심 아키텍처 (여석 실시간 파이프라인)

이 서비스의 본체. 5단계가 여러 파일에 걸쳐 있어 같이 봐야 이해됨.

1. **수집**: 크롤러 → `admin/seat/{General,Pin}SeatBatch`. `support/batch/AbstractBatch` 확장한 인메모리 큐.
   `FLUSH_LIMIT` 또는 명시 `flush()` 시 `SeatPersistenceService.saveAllSeat`(`REQUIRES_NEW`) 로 DB 저장.
2. **인메모리 스냅샷**: `domain/seat/SeatStorage` 의 `Map<Subject, SeatDto>` (ConcurrentHashMap). 정렬·필터(여석>
   0, 비전공 등) 가 여기서. **DB 가 아니라 이 메모리가 SSE 의 source of truth**.
3. **스케줄링**: `support/scheduler/ScheduledTaskHandler` 가 `ThreadPoolTaskScheduler` 래핑. `general`/
   `pin` 등 용도별 빈으로 `@Qualifier` 주입. 3초 주기로 스토리지 읽어 SSE push.
4. **전파**: `SseService.propagate(eventName, dto)`. `SseEmitterStorage` 가 토큰→`SseEmitter` 보관. 끊긴 연결은
   `SseErrorHandler` 정리.
5. **운영 토글**: `SchedulerService.startScheduling`/`cancelScheduling` 단일 진입점. **"크롤러 적재" 와 "사용자 push"
   가 분리** — push 만 켜고 끌 수 있음.

별도 축: 졸업 요건은 `support/sheet` 가 Google Sheets 에서 정책 끌어와 `domain/graduation` 이 검사.

## 패키지 경계 (네이밍과 책임 주의)

- `domain/` — 사용자 향 도메인 로직
- `admin/` — **어드민 API + 크롤러 데이터 적재** (`*Batch`, `*PersistenceService`, `TargetSubjectStorage`). 이름은
  어드민이지만 적재 책임도 같이 가짐
- `client/` — 외부 호출 어댑터
- `support/` — 인프라 (`sse`, `scheduler`, `batch`, `sheet`, `semester` 등)
- `config/` — 빈 정의 (스캔/스케줄러/리트라이/로그인/시트)

컴포넌트 스캔: `kr.allcll.backend` + `kr.allcll.crawler` 두 패키지를 한 컨텍스트에 묶음 (
`config/ModuleScanConfig.java`).

## 서브모듈 디버깅 시 주의

`allcll-crawler` 는 별도 repo 의 git 서브모듈. CI 가 매 빌드마다 `git submodule update --init --remote` 로 main 최신
커밋을 끌어옴 → **서브모듈 변경이 빌드에 즉시 반영되므로 디버깅 시 양쪽 git 상태 같이 확인 필수**.

## 테스트

- 통합(`@SpringBootTest`) + `WebMvcTest` 가 주류
- HTTP 시나리오: REST Assured
- 픽스처: `src/test/java/kr/allcll/backend/fixture/` — 가능하면 재사용·확장
- 파일명 분류:
    - `*Test.java` — 단위 (Service·도메인 로직)
    - `*IntegrationTest.java` — DB 포함 전체 흐름
    - `*ApiTest.java` — REST Assured 엔드포인트
- **`Thread.sleep()` 금지** — 동시성 테스트엔 Awaitility 사용

자세한 테스트 작성 절차는 `.claude/skills/testing/SKILL.md`.

## 도메인 용어

- **여석**: 남은 수강 가능 자리 수
- **여석 선공개(preseat)**: 정규 수강신청 전 공개 여석
- **관담인원**: 관심 과목으로 등록한 학생 수
- **핀(Pin)**: 사용자 지정 관심 과목, 여석 생기면 알림 대상
- **관심과목(basket)**: 사용자 담은 과목 목록
- **올클 연습**: 수강신청 전 연습 기능

## 절대 규칙

- 이모지 사용 금지 (PR 타이틀, 코드, 커밋 메시지 — CI 실패 원인)
- `.env`, `credentials`, Google 서비스 계정 파일 커밋 금지
- `application-prod.yml`, `application-dev.yml` 은 별도 설정 저장소에서 관리

## 브랜치·PR

- `main` — 메인 개발 (PR 머지 시 dev 자동 배포)
- `release-1.0` — 운영 (push 시 prod 자동 배포)
- 피처 브랜치: `{username}/TSK-{번호}` 또는 `feat/*`, `fix/*`, `refactor/*`, `chore/*`
- PR 형식·생성: `/create-pr` 커맨드 사용 (`.claude/commands/create-pr.md`)
- 이슈 생성: `/create-issue` 커맨드 사용 (`.claude/commands/create-issue.md`)

## 코드 배치 컨벤션

이 프로젝트의 결정 사항. Claude 가 임의로 바꾸면 안 됨.

- Entity: domain/<도메인명>/ 하위에 배치 (예: domain/seat/Seat.java)
- 커스텀 예외: support/exception/ 하위에 정의
- DTO 네이밍: 요청 *Request, 응답 *Response (예: SeatSearchRequest, SeatResponse)
- 읽기 전용 메서드: @Transactional(readOnly = true) 명시 (동시성·읽기 부하 도메인이라 누락 시 성능 영향)
