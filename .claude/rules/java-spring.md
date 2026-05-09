---
description: Java/Spring Boot 소스 코드 작성 시 적용
globs: src/main/java/**/*.java
---

# Java/Spring Boot 코딩 규칙

## 클래스 구조

- Controller → Service → Repository 계층 구조 준수
- Controller는 요청/응답 변환만, 비즈니스 로직은 Service에 작성
- Entity는 `domain/` 패키지 내 해당 도메인 하위에 배치
- DTO는 요청(`*Request`), 응답(`*Response`)으로 명확히 구분

## 네이밍

- 패키지명: 소문자 (camelCase 금지)
- 클래스명: PascalCase
- 메서드/변수명: camelCase
- 상수: UPPER_SNAKE_CASE

## 코드 스타일

- Lombok 사용 허용 (`@Getter`, `@RequiredArgsConstructor`, `@Builder` 등)
- `Optional` 반환은 Repository 계층에서만, Service에서는 예외 처리로 변환
- `@Transactional(readOnly = true)` 조회 메서드에 반드시 적용
- 생성자 주입 사용 (`@RequiredArgsConstructor`), 필드 주입 금지

## API 설계

- RESTful URL 설계 (명사 복수형, 소문자, 하이픈 구분)
- 응답 상태코드 적절히 사용 (200, 201, 204, 400, 404 등)
- `@Valid`로 요청 검증, 커스텀 예외는 `support/exception/` 하위에 정의

## 금지 사항

- `System.out.println` 사용 금지 (Logger 사용)
- 하드코딩된 매직 넘버/문자열 금지 (상수 또는 설정값 사용)
- `*` 와일드카드 import 금지