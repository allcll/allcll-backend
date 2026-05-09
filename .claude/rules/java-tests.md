---
description: 테스트 코드 작성 시 적용
globs: src/test/java/**/*.java
---

# 테스트 코드 규칙

## 테스트 구조

- 단위 테스트: `*Test.java` — Service, 도메인 로직 검증
- 통합 테스트: `*IntegrationTest.java` — DB 포함 전체 흐름 검증
- API 테스트: `*ApiTest.java` — REST Assured 기반 엔드포인트 검증

## 테스트 작성 원칙

- Given-When-Then 패턴 사용
- 테스트 메서드명: 한글 허용 (`@DisplayName` 또는 메서드명 직접 사용)
- 하나의 테스트는 하나의 동작만 검증
- 테스트 간 순서 의존성 금지
- 테스트 픽스처는 `fixture/` 패키지의 팩토리 메서드 활용

## 테스트 데이터

- DB: H2 인메모리 (create-drop)
- 외부 API 호출: Mock 처리
- 테스트 설정: `src/test/resources/application.yml` 사용

## 금지 사항

- `Thread.sleep()` 사용 금지 (Awaitility 등 사용)
- 프로덕션 DB 직접 접근 금지
- 테스트에서 `@SpringBootTest` 남용 금지 (필요한 슬라이스 테스트 우선)
