# Sentry 모니터링

## 안전 기본값

- 애플리케이션은 DSN을 커밋하지 않는다. `SENTRY_DSN`은 실행 환경의 환경변수로만 주입한다.
- `SENTRY_DSN`이 없거나 빈 값이면 `SentryConfig`가 Sentry 전송을 비활성화한다.
- 요청 본문은 `SentryOptions.RequestSize.NONE`으로 수집하지 않는다.
- 기본 PII 수집은 `SentryOptions#setSendDefaultPii(false)`로 비활성화한다.
- `beforeSend` 콜백에서 Sentry 이벤트의 user, request body, cookies, query string, headers를 제거한 뒤 전송한다.

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
