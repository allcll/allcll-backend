# Sentry 모니터링

## 안전 기본값

- 애플리케이션은 DSN을 커밋하지 않는다. `SENTRY_DSN`은 실행 환경의 환경변수로만 주입한다.
- `SENTRY_DSN`이 없거나 빈 값이면 `SentryConfig`가 Sentry 전송을 비활성화한다.
- 요청 본문은 `SentryOptions.RequestSize.NONE`으로 수집하지 않는다.
- 기본 PII 수집은 `SentryOptions#setSendDefaultPii(false)`로 비활성화한다.
- `beforeSend` 콜백에서 Sentry 이벤트의 user, request body, cookies, query string, headers를 제거한 뒤 전송한다.

## 에러 수집 정책

수집 대상:

- 예상하지 못한 일반 서버 예외
- HTTP 상태가 5xx인 `AllcllException`
- 외부 시스템 장애를 의미하는 HTTP 502 `AllcllException`

미수집 대상:

- HTTP 4xx 비즈니스 예외
- validation 오류
- 인증 실패
- 존재하지 않는 API 및 리소스
- 정상적인 SSE timeout

일반 `Exception`은 `SERVER_ERROR`와 HTTP 500으로 분류한다. `AllcllException`은
`AllcllErrorCode#getHttpStatus()`가 5xx인지 판별하므로 500뿐 아니라 502 등 모든 서버 오류를 수집한다.

## 이벤트 태그

각 수집 이벤트에는 요청 단위 Sentry scope를 사용해 다음 태그만 추가한다.

| 태그 | 값 |
| --- | --- |
| `method` | HTTP method |
| `path` | query string을 제외한 request URI |
| `status` | HTTP status code |
| `errorCode` | `AllcllErrorCode` enum 이름 |
| `exceptionType` | 예외 클래스의 simple name |

태그 및 extra에는 사용자 ID, 인증 토큰, cookie, query parameter, request body를 넣지 않는다.
요청 단위 scope를 사용하므로 한 요청의 태그가 다른 요청 이벤트로 유출되지 않는다.

## 환경변수

| 이름 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `SENTRY_DSN` | 아니오 | 미설정 | 값이 있으면 Sentry 이벤트 전송을 활성화한다. |
| `SENTRY_ENVIRONMENT` | 아니오 | 미설정 | `local`, `dev`, `prod` 같은 Sentry 환경 이름이다. |
| `SENTRY_DEBUG` | 아니오 | `false` | Sentry SDK 설정 디버그 로그를 출력한다. 로컬 확인 용도로만 사용한다. |

## 로컬 검증

운영용이 아닌 Sentry 프로젝트 DSN을 사용한다.

```bash
export SENTRY_DSN="https://public-key@example.ingest.sentry.io/project-id"
export SENTRY_ENVIRONMENT=local
export SENTRY_DEBUG=true
./gradlew bootRun
```

그다음 로컬에서 자연스럽게 처리되지 않은 예외가 발생하는 기존 엔드포인트나 테스트 경로를 호출한다. 에러 발생만을 목적으로 하는 운영 API를 새로 추가하지 않는다.

설정 테스트는 다음 명령으로 실행한다.

```bash
./gradlew test --tests kr.allcll.backend.config.SentryConfigTest
```

DSN이 없을 때 비활성화되는지 확인하려면 다음처럼 실행한다.

```bash
unset SENTRY_DSN
./gradlew bootRun
```

헬스 체크 엔드포인트는 기존 actuator 설정을 따른다. 로컬에서는 `prometheus`, `health`가 노출되며, 이 Sentry 설정은 actuator 엔드포인트를 추가하지 않는다.
