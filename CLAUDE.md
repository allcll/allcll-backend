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

- 테스트 DB: H2 인메모리 (create-drop)
- 테스트 리소스: `src/test/resources/application.yml`
- 테스트 패턴: `*Test.java`, `*IntegrationTest.java`, `*ApiTest.java`
- 픽스처: `src/test/java/kr/allcll/backend/fixture/`

## 주의사항

- 이모지 사용 금지 (PR 타이틀, 코드, 커밋 메시지 모두)
- `.env`, `credentials`, Google 서비스 계정 파일 절대 커밋 금지
- `application-prod.yml`, `application-dev.yml`은 별도 설정 저장소에서 관리
- 서브모듈(`allcll-crawler`)은 별도 빌드/관리
