# 버그 수정 gotchas

bug-fix 작업에서 반복해서 실패하기 쉬운 지점을 고정한다. 검증/권한/응답 변경, scheduler/SSE, 사용자 변경이 있는 작업이면 읽는다.

## 수정 전 확인

- 현재 동작, 기대 동작, 재현 조건을 분리한다.
- 원인이 아직 추정이면 수정부터 하지 말고 analysis로 분기한다.
- 사용자 변경이 있는 파일은 현재 diff를 확인하고 그 위에서 최소 수정한다.
- 민감 설정 값이 필요하면 파일을 열지 말고 redacted evidence를 요청한다.

## 검증 어노테이션 짝 점검

| 추가 대상 | 함께 점검할 컨테이너 |
|---|---|
| DTO 필드의 `@NotNull`, `@NotBlank`, `@Size` | controller의 `@RequestBody`/`@ModelAttribute`에 `@Valid` |
| method parameter의 `@NotBlank`, `@Min`, `@Pattern` | controller class 또는 method validation 위치의 `@Validated` |
| `@PreAuthorize`, `@Secured` | method security 활성화 여부 |
| `@Cacheable`, `@CacheEvict` | cache 활성화 여부 |
| `@Async`, `@Scheduled` | async/scheduling 활성화 여부 |

어노테이션만 추가하고 트리거를 놓치면 diff는 그럴듯해도 동작은 바뀌지 않는다.

## 영향 범위 grep

- 같은 DTO suffix: `*Create*Request`, `*Update*Request`, `*Search*Request`
- 같은 controller prefix: `Admin*Api`, 같은 domain의 API
- 같은 validation/authorization annotation 사용처
- 같은 scheduler, batch, SSE, persistence 호출 체인

점검 결과는 "N개 파일 확인, 동일 결함 M개"처럼 보고한다. 인접 문제를 발견했지만 범위 밖이면 수정하지 않고 분리 보고한다.

## 테스트와 완료 조건

- 가능하면 재현 테스트를 먼저 만든다.
- 테스트를 먼저 못 만들었으면 수정 후라도 버그 케이스와 정상 케이스를 검증한다.
- 컴파일만으로 완료 선언하지 않는다.
- 테스트 실패를 기대값 완화, 삭제, 비활성화로 해결하지 않는다.
- 관련 테스트를 못 돌렸으면 이유와 대체 검증을 적는다.

## 분리해야 할 작업

- 동작 보존 cleanup: refactoring
- 테스트 작성만: testing
- 근본 원인 미확정: analysis
- 정책/도메인 모델 변경: 사용자 확인 또는 설계 단계
