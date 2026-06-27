# Prometheus Endpoint 및 로컬 Grafana 확인

## 목적

이번 단계에서는 Spring Boot Actuator와 Micrometer 기본 지표를 Prometheus가 scrape할 수 있게 만들고, 로컬에서
Prometheus/Grafana로 확인 가능한 최소 구성을 제공한다. dev/prod에는 Prometheus scrape endpoint를 열기 위한 config repo 설정 예시를
문서화한다.

운영 서버에서 Prometheus/Grafana 프로세스를 실제로 실행하고 대시보드를 운영하는 작업은 별도 provisioning 또는 배포 절차에서 다룬다.

Sentry는 예외 이벤트, 에러 컨텍스트, 스택트레이스 중심의 장애 원인 파악 도구다. Prometheus/Grafana는 요청량, latency, JVM, DB
connection pool, SSE 연결 수 같은 수치 추세를 본다. 두 도구는 대체 관계가 아니라 서로 다른 신호를 담당한다.

## 로컬 실행

Spring Boot 애플리케이션을 먼저 실행한다.

```bash
./gradlew bootRun
```

Prometheus endpoint가 열렸는지 확인한다.

```bash
curl http://localhost:8081/actuator/prometheus
```

Prometheus와 Grafana를 실행한다.

```bash
docker compose -f observability/compose.yml up
```

확인 URL:

- Spring Boot Prometheus endpoint: `http://localhost:8081/actuator/prometheus`
- Prometheus targets: `http://localhost:9090/targets`
- Grafana: `http://localhost:3000`

Grafana 로컬 기본 계정은 `admin` / `admin`이다. 로컬 개발 전용 값이며 운영에 사용하지 않는다.

## 로컬 설정

로컬에서 Prometheus endpoint를 확인하려면 `application-local.yml`에 다음 actuator 설정이 필요하다.

```yaml
management:
  server:
    port: 8081
  endpoints:
    jmx:
      exposure:
        exclude: "*"
    web:
      exposure:
        include: health, prometheus
    access:
      default: none
  endpoint:
    health:
      access: read-only
    prometheus:
      access: read-only
```

기존 metric tag 구조는 유지하고 HTTP latency histogram을 로컬에서 활성화한다.

```yaml
management:
  metrics:
    tags:
      host: local-server
      application: backend
      env: local
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

`http.server.requests` histogram은 p95/p99 latency 패널에서 `histogram_quantile`을 쓰기 위한 설정이다.

## 기본 대시보드 패널

처음 Grafana 대시보드는 아래 패널로 구성한다. datasource는 provisioning으로 자동 등록된 `Prometheus`를 선택한다.

| 패널 | PromQL |
| --- | --- |
| Prometheus target UP | `up{job="allcll-backend-local"}` |
| HTTP 요청 수 | `sum(rate(http_server_requests_seconds_count{uri!="/actuator/prometheus"}[5m]))` |
| HTTP 5xx rate | `sum(rate(http_server_requests_seconds_count{status=~"5..", uri!="/actuator/prometheus"}[5m]))` |
| HTTP p95 latency | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{uri!="/actuator/prometheus"}[5m])))` |
| JVM heap memory used | `sum(jvm_memory_used_bytes{area="heap"})` |
| GC pause | `rate(jvm_gc_pause_seconds_sum[5m])` |
| Hikari active connections | `hikaricp_connections_active` |
| Hikari pending connections | `hikaricp_connections_pending` |
| SSE active connections | `sse_active_connections` |
| Scheduler active threads | `executor_active_threads{name=~".*-seat-sender"}` |
| Scheduler queued tasks | `executor_queued_tasks{name=~".*-seat-sender"}` |
| Scheduler completed tasks | `rate(executor_completed_tasks_total{name=~".*-seat-sender"}[5m])` |

여석 전송은 `@Scheduled`가 아니라 `ScheduledTaskHandler`가 `ThreadPoolTaskScheduler`에 작업을 동적으로 등록하는 구조다. 따라서
여석 전송 스케줄러 상태는 `tasks_scheduled_execution_seconds`가 아니라 `ExecutorServiceMetrics.monitor(...)`에서 등록되는
`executor_*` 지표에서 `name="general-seat-sender"` 또는 `name="pin-seat-sender"` label로 확인한다.

`tasks_scheduled_execution_seconds`는 Spring `@Scheduled` 메서드 실행 시간 지표다. 현재 코드에서는
`SeatStorageLogger`, `ExternalPreInvoker` 같은 별도 scheduled method에 해당하므로, 여석 전송 상태를 판단하는 기본 패널에는 포함하지
않는다.

## 후속 커스텀 메트릭 후보

이번 단계는 Spring Boot Actuator와 Micrometer 기본 지표를 Prometheus가 수집할 수 있게 만드는 작업이다. SSE 전송 지연이나
backpressure 여부를 더 자세히 보려면 후속 PR에서 커스텀 메트릭을 추가한다.

현재 `SseService`는 emitter별 전송 상태를 두고, 전송 중 같은 event name의 새 이벤트가 들어오면 pending 이벤트를 최신 값으로 덮어쓴다.
즉 느린 SSE 연결이 전체 전송 흐름을 막지 않도록 하는 coalescing 기반 backpressure 완화 로직은 있다. 다만 이 동작을 별도 지표로
노출하지는 않으므로 현재 대시보드에서는 실제 병합 빈도를 확인할 수 없다.

후속 후보:

- `sse.send.duration`: SSE `send` 호출에 걸리는 시간. Prometheus에서는 `sse_send_duration_seconds`로 확인한다.
- `sse.event.coalesced`: backpressure 완화를 위해 같은 event name의 pending 이벤트를 최신 값으로 덮어쓴 횟수.
  Prometheus에서는 `sse_event_coalesced_total{event=~"pinSeats|nonMajorSeats"}`로 확인한다.

두 지표는 커스텀 메트릭이므로 기본 수집 구성을 다루는 이번 PR에는 포함하지 않고, 여석 파이프라인 커스텀 메트릭을 다루는
`feat/seat-pipeline-metrics` 브랜치에서 구현한다.

## 로컬 Prometheus scrape

`observability/prometheus/prometheus.yml`은 Docker 컨테이너에서 host의 Spring Boot 앱을 scrape한다.

```yaml
scrape_configs:
  - job_name: allcll-backend-local
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - host.docker.internal:8081
        labels:
          env: local
```

`observability/compose.yml`에는 Linux Docker에서도 `host.docker.internal`이 동작하도록
`extra_hosts: host-gateway`를 둔다.

이 compose 파일은 로컬 확인용이다. Prometheus `9090`, Grafana `3000`을 host에 publish하고 Grafana 기본 계정도
`admin` / `admin`을 사용하므로 운영 서버에 그대로 적용하지 않는다.

## Dev/Prod 적용

dev/prod의 `application-dev.yml`, `application-prod.yml`은 별도 config repository에서 관리한다. 이 레포에는 실제 운영
설정값을 커밋하지 않는다.

approve 이후 config repository에는 환경별로 다음 설정을 추가한다. 이 설정은 Spring Boot 애플리케이션이 Prometheus scrape endpoint를
노출하기 위한 설정이며, Prometheus/Grafana 프로세스를 실행하지는 않는다. 기존 dev/local처럼 actuator를 별도 management port에서
운영한다면 해당 포트를 유지한다.

```yaml
management:
  server:
    port: 8081
  endpoints:
    jmx:
      exposure:
        exclude: "*"
    web:
      exposure:
        include: health, prometheus
    access:
      default: none
  endpoint:
    health:
      access: read-only
    prometheus:
      access: read-only
  metrics:
    tags:
      host: dev-server
      application: backend
      env: dev
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

prod는 `host`, `env` 값을 prod 환경에 맞게 설정한다.

현재 GitHub Actions 배포 workflow는 JAR를 복사한 뒤 `java -jar`로 애플리케이션만 실행한다. 따라서 운영 서버에서
Prometheus/Grafana까지 실제로 사용하려면 별도 provisioning 또는 배포 절차가 필요하다. 예를 들어 운영용 compose 파일 배치,
`docker compose up -d`, Prometheus scrape target 설정, Grafana datasource/dashboard 설정, 방화벽 또는 보안그룹 제한을 별도 작업으로
처리한다.

## 운영 보안

운영에서는 `/actuator/prometheus`를 외부 인터넷에 공개하지 않는다.

Prometheus/Grafana 운영 배포를 별도로 진행할 때는 처음에는 단순성을 위해 한 서버에서 Spring Boot 애플리케이션, Prometheus, Grafana를
함께 실행할 수 있다.

예:

- Spring Boot API: `8080`
- Spring Boot actuator endpoint: `http://server:8081/actuator/prometheus`
- Prometheus: `9090`
- Grafana: `3000`

초기 운영에서는 다음 원칙을 지킨다.

- `/actuator/prometheus`는 일반 사용자에게 공개하지 않는다.
- Prometheus만 Spring Boot management port에 접근할 수 있도록 제한한다.
- Prometheus `9090`은 외부에 공개하지 않는다.
- Grafana는 인증을 활성화하고 기본 계정을 사용하지 않는다.
- Grafana 접근은 관리자/VPN/사내망 등 필요한 사용자로 제한한다.
- 로컬 확인용 `observability/compose.yml`의 포트 공개와 `admin` / `admin` 계정 설정을 운영에 그대로 사용하지 않는다.

추후 트래픽이나 운영 요구가 커지면 API port와 actuator management port를 더 엄격히 분리하고, Prometheus/Grafana를 별도 서버로 분리한 뒤
Prometheus만 management port에 접근할 수 있도록 접근 제어한다.

Metric label에는 사용자 token, 학번, subjectId, requestId, query string 같은 민감정보나 high-cardinality 값을 넣지
않는다.
