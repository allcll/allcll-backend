# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## 프로젝트

- **이름**: 올클 (allcll) — 세종대학교 수강신청 도우미 서비스의 백엔드
- **스택**: Java 21, Spring Boot 3.4, JPA(Hibernate), MySQL(local/prod), H2(test)
- **구조**: Gradle 멀티 프로젝트 — 루트(`./src/`) + `:crawler` 서브모듈(`./allcll-crawler/`, 별도 git repo)

## 빌드 / 테스트

- 전체 빌드+테스트: `./gradlew clean build` (CI와 동일)
- 단일 테스트: `./gradlew test --tests "FQCN"` 또는 `--tests "FQCN.method"`
- 로컬 실행: `./gradlew bootRun`. `application.yml`이 `spring.profiles.active=local`을 지정 →
  `application-local.yml`(MySQL `allcll_local`, 세종대 포털/TOSC URL, Google Sheets 자격증명, admin 토큰 등 환경값
  포함)이 적용됨. 빌드 환경에 맞게 덮어써야 한다.
- 테스트는 H2 인메모리(`src/test/resources/application.yml`). `app.runner.enabled=false`라 부팅 시 스케줄러가 자동 시작되지
  않는다.

## 서브모듈

- `allcll-crawler`는 별도 repo의 git 서브모듈. CI(`.github/workflows/pull_request.yml`)가 매 빌드마다
  `git submodule update --init --remote`로 main 최신 커밋을 끌어옴 → **서브모듈 쪽 변경이 빌드에 즉시 반영되므로 디버깅 시 양쪽 git
  상태를 같이 확인**해야 한다.
- 컴포넌트/엔티티/리포지토리 스캔은 `kr.allcll.backend` + `kr.allcll.crawler` 두 패키지를 한 스프링 컨텍스트에 묶는다 (
  `config/ModuleScanConfig.java`).

## 핵심 아키텍처

여석 실시간 파이프라인이 본 서비스의 핵심이며, 여러 파일을 같이 봐야 이해된다.

1. **수집**: 크롤러 → `admin/seat/{General,Pin}SeatBatch`. 둘 다 `support/batch/AbstractBatch`를 확장한 인메모리
   큐로, `FLUSH_LIMIT` 또는 명시적 `flush()`에서 `SeatPersistenceService.saveAllSeat`(`REQUIRES_NEW`)로 DB에
   저장.
2. **인메모리 스냅샷**: `domain/seat/SeatStorage`가 `Map<Subject, SeatDto>`(ConcurrentHashMap). 정렬·필터(여석>0,
   비전공 등)가 여기서 일어난다 — DB가 아니라 이 메모리가 SSE의 source of truth.
3. **스케줄링**: `support/scheduler/ScheduledTaskHandler`는 `ThreadPoolTaskScheduler`를 감싼 컨테이너로
   `general`/`pin` 등 용도별 빈으로 주입(`@Qualifier`). 3초 주기 작업이 스토리지를 읽어 SSE로 push.
4. **전파**: `SseService.propagate(eventName, dto)`. emitter는 토큰→`SseEmitter`로 `SseEmitterStorage`에
   보관, 끊긴 연결은 `SseErrorHandler`가 정리.
5. **운영 토글**: 스케줄러 시작/중지의 단일 진입점은 `SchedulerService.startScheduling`/`cancelScheduling`. 즉 "크롤러 적재"
   와 "사용자 push"는 분리되어 있어 push만 켜고 끌 수 있다.

졸업 요건은 별도 축으로, `support/sheet`가 Google Sheets에서 정책(학점·필수·균형·인증 기준)을 끌어와 `domain/graduation`이 검사한다.

## 패키지 경계

- `domain/` — 사용자 향 도메인.
- `admin/` — 어드민 API + **크롤러 데이터 적재**(`admin/seat`의 `*Batch`, `*PersistenceService`,
  `TargetSubjectStorage`). "어드민 전용" 외에 적재 책임도 같이 가진다는 점을 인지.
- `client/` — 외부 호출 어댑터.
- `support/` — 인프라(`sse`, `scheduler`, `batch`, `sheet`(Google Sheets), `semester`, …).
- `config/` — 빈 정의. 스캔/스케줄러/리트라이/로그인/시트 등.

# allcll-backend

올클(allcll) — 대학교 수강신청 도우미 백엔드 서비스

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.1
- **Build**: Gradle (Wrapper)
- **DB**: MySQL (운영), H2 (로컬/테스트)
- **ORM**: Spring Data JPA + Hibernate + Flyway
- **Testing**: JUnit 5, REST Assured 5.5.0, Spring Boot Test
- **외부 연동**: Google Sheets API, OkHttp, JSoup, Apache POI

## 빌드 & 테스트 명령어

```bash
./gradlew clean build        # 전체 빌드 (테스트 포함)
./gradlew compileJava        # 컴파일만
./gradlew test               # 테스트만
./gradlew bootRun            # 로컬 실행
```

## 디렉토리 구조

```
src/main/java/kr/allcll/backend/
├── admin/           # 관리자 API (basket, department, graduation, notice, operationperiod, preseat, review, seat, session, sse, subject)
├── client/          # 외부 API 클라이언트
├── config/          # Spring 설정
├── domain/          # 핵심 비즈니스 로직
│   ├── basket/      # 장바구니 (star: 즐겨찾기)
│   ├── department/  # 학과 정보
│   ├── graduation/  # 졸업요건 (balance, certification, check, credit, department)
│   ├── notice/      # 공지사항
│   ├── operationperiod/  # 수강신청 운영 기간
│   ├── review/      # 수강 후기
│   ├── seat/        # 잔여석 실시간 조회 (pin, preseat)
│   ├── subject/     # 교과목 정보 (subjectReport)
│   ├── timetable/   # 시간표 (schedule)
│   └── user/        # 사용자
└── support/         # 공통 인프라
    ├── batch/       # 배치 처리
    ├── entity/      # 베이스 엔티티
    ├── exception/   # 예외 처리
    ├── graduation/  # 졸업 유틸
    ├── scheduler/   # 스케줄러
    ├── semester/    # 학기 유틸
    ├── sheet/       # Google Sheets 연동
    ├── sse/         # Server-Sent Events
    └── web/         # 웹 유틸

allcll-crawler/      # 크롤러 서브모듈 (별도 Gradle 프로젝트)
```

## 브랜치 전략

- `main` — 메인 개발 브랜치 (PR 머지 시 dev 자동 배포)
- `release-1.0` — 운영 배포 브랜치 (push 시 prod 자동 배포)
- 피처 브랜치: `{username}/TSK-{번호}` 또는 `feat/*`, `fix/*`, `refactor/*`, `chore/*`

## PR 규칙

- PR 타이틀 형식: `type: 설명` (예: `feat: 잔여석 알림 기능 추가`)
- 허용 타입: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`
- base 브랜치: `main`
- 리뷰어 자동 배정 (auto_assign.yml)

## 테스트

- 통합 테스트(`@SpringBootTest`) + `WebMvcTest`가 주류.
- 픽스처는 `src/test/java/kr/allcll/backend/fixture/`에 모여 있음 — 가능하면 재사용/확장.
- HTTP 시나리오는 `rest-assured`.
- 테스트 DB: H2 인메모리 (create-drop)
- 테스트 리소스: `src/test/resources/application.yml`
- 테스트 패턴: `*Test.java`, `*IntegrationTest.java`, `*ApiTest.java`
- 픽스처: `src/test/java/kr/allcll/backend/fixture/`

## 도메인 용어
## 주의사항

- **여석**: 해당 과목에 남은 수강 가능 자리 수
- **여석 선공개(preseat)**: 정규 수강신청 전에 공개되는 여석 정보
- **관담인원**: 관심 과목으로 등록한 학생 수
- **핀(Pin)**: 사용자가 지정한 관심 과목. 여석 생길 때 알림 발송 대상
- **관심과목(basket)**: 사용자가 담아둔 과목 목록
- **올클 연습**: 실제 수강신청 전 연습 기능
- 이모지 사용 금지 (PR 타이틀, 코드, 커밋 메시지 모두)
- `.env`, `credentials`, Google 서비스 계정 파일 절대 커밋 금지
- `application-prod.yml`, `application-dev.yml`은 별도 설정 저장소에서 관리
- 서브모듈(`allcll-crawler`)은 별도 빌드/관리
