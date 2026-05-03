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

## 테스트

- 통합 테스트(`@SpringBootTest`) + `WebMvcTest`가 주류.
- 픽스처는 `src/test/java/kr/allcll/backend/fixture/`에 모여 있음 — 가능하면 재사용/확장.
- HTTP 시나리오는 `rest-assured`.

## 도메인 용어

- **여석**: 해당 과목에 남은 수강 가능 자리 수
- **여석 선공개(preseat)**: 정규 수강신청 전에 공개되는 여석 정보
- **관담인원**: 관심 과목으로 등록한 학생 수
- **핀(Pin)**: 사용자가 지정한 관심 과목. 여석 생길 때 알림 발송 대상
- **관심과목(basket)**: 사용자가 담아둔 과목 목록
- **올클 연습**: 실제 수강신청 전 연습 기능
