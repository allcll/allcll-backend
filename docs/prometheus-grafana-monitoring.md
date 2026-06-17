# Prometheus/Grafana 모니터링

## 목적

Prometheus/Grafana는 애플리케이션의 시계열 지표를 수집하고 대시보드로 확인하기 위해 도입한다. 이번 단계에서는 Spring Boot Actuator와 Micrometer 기본 지표를 Prometheus가 scrape할 수 있게 만들고, 로컬에서 Grafana로 확인 가능한 최소 구성을 제공한다.

Sentry는 예외 이벤트, 에러 컨텍스트, 스택트레이스 중심의 장애 원인 파악 도구다. Prometheus/Grafana는 요청량, latency, JVM, DB connection pool, SSE 연결 수 같은 수치 추세를 본다. 두 도구는 대체 관계가 아니라 서로 다른 신호를 담당한다.

## 로컬 실행

Spring Boot 애플리케이션을 먼저 실행한다.

```bash
./gradlew bootRun
```

Prometheus endpoint가 열렸는지 확인한다.

```bash
curl http://localhost:8080/actuator/prometheus
```

Prometheus와 Grafana를 실행한다.

```bash
docker compose -f observability/compose.yml up
```

확인 URL:

- Spring Boot Prometheus endpoint: `http://localhost:8080/actuator/prometheus`
- Prometheus targets: `http://localhost:9090/targets`
- Grafana: `http://localhost:3000`

Grafana 로컬 기본 계정은 `admin` / `admin`이다. 로컬 개발 전용 값이며 운영에 사용하지 않는다.

## 로컬 설정

`application-local.yml`은 다음 actuator endpoint를 노출한다.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus, health
```

공통 metric tag와 HTTP latency histogram도 로컬에서 활성화한다.

```yaml
management:
  metrics:
    tags:
      application: allcll-backend
      environment: ${spring.profiles.active}
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
| Scheduler active threads | `executor_active_threads{name=~"general-seat-sender|pin-seat-sender"}` |
| Scheduler queued tasks | `executor_queued_tasks{name=~"general-seat-sender|pin-seat-sender"}` |
| Scheduler completed tasks | `rate(executor_completed_tasks_total{name=~"general-seat-sender|pin-seat-sender"}[5m])` |
| Spring scheduled method duration | `rate(tasks_scheduled_execution_seconds_sum[5m]) / rate(tasks_scheduled_execution_seconds_count[5m])` |

Scheduler executor 지표는 `ExecutorServiceMetrics.monitor(...)`에서 등록되며, 로컬 출력에서 `executor_active_threads`, `executor_queued_tasks`, `executor_completed_tasks_total`과 `name="general-seat-sender"`, `name="pin-seat-sender"` label을 확인했다. Spring `@Scheduled` 메서드 지표는 `tasks_scheduled_execution_seconds`로 노출된다.

## 로컬 Prometheus scrape

`observability/prometheus/prometheus.yml`은 Docker 컨테이너에서 host의 Spring Boot 앱을 scrape한다.

```yaml
scrape_configs:
  - job_name: allcll-backend-local
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - host.docker.internal:8080
        labels:
          environment: local
```

`observability/compose.yml`에는 Linux Docker에서도 `host.docker.internal`이 동작하도록 `extra_hosts: host-gateway`를 둔다.

## Dev/Prod 적용

dev/prod의 `application-dev.yml`, `application-prod.yml`은 별도 config repository에서 관리한다. 이 레포에는 실제 운영 설정값을 커밋하지 않는다.

config repository에는 환경별로 다음 설정을 추가한다.

```yaml
management:
  server:
    port: 9091
  endpoints:
    web:
      exposure:
        include: health, prometheus
  metrics:
    tags:
      application: allcll-backend
      environment: dev
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

prod는 `environment: prod`로 설정한다.

## 운영 보안

운영에서는 `/actuator/prometheus`를 외부 인터넷에 공개하지 않는다.

권장 배치:

- 일반 API 포트와 actuator 포트를 분리한다. 예: API `8080`, management `9091`
- Prometheus만 `9091`에 접근할 수 있도록 Security Group을 제한한다.
- Prometheus `9090`은 외부에 공개하지 않는다.
- Grafana는 인증을 활성화하고 기본 계정을 사용하지 않는다.
- Grafana 접근은 관리자/VPN/사내망 등 필요한 사용자로 제한한다.

Metric label에는 사용자 token, 학번, subjectId, requestId, query string 같은 민감정보나 high-cardinality 값을 넣지 않는다. 여석 파이프라인 커스텀 메트릭은 `type=general|pin`, `task=general-seat|pin-seat`처럼 낮은 cardinality tag만 사용한다.
