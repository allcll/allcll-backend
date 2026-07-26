# 구현 스킬 경계 기준

bug-fix와 refactoring은 둘 다 코드를 바꾸지만, primary 기준은 다르다. 애매한 요청이면 이 문서를 먼저 읽고 primary skill을 정한다.

## 판정표

| 사용자 의도 | Primary | 기준 |
|---|---|---|
| 현재 동작이 잘못됐고 기대 동작으로 바꿔야 함 | `allcll-bug-fix` | API 응답, 예외, 저장 결과, 검증, 권한, 알림, 스케줄 상태처럼 외부 동작이 바뀐다. |
| 이미 기대 동작이 확정된 작은 보정 | `allcll-bug-fix` | 단일 엔드포인트/단일 흐름 수준이고 정책·UX·도메인 설계 결정이 새로 필요하지 않다. |
| 같은 동작을 더 읽기 쉽고 안전한 구조로 정리 | `allcll-refactoring` | 반환값, 예외, 메시지, 로그, 저장 순서, 공개 계약을 보존한다. |
| 원인이나 병목이 아직 불명확함 | `allcll-analysis` | 먼저 findings, 확인 지표, 후속 분기를 만든다. |
| 테스트 작성 또는 테스트 수정 자체가 목표 | `allcll-testing` | 프로덕션 코드 변경이 primary가 아니다. |
| 큰 기능, 정책 엔진, 도메인 모델 재설계 | stop/ask-user | 설계와 사용자 결정이 먼저 필요하다. |

## 애매한 표현 처리

- "성능 개선해줘": 병목이 불명확하면 analysis, 이미 확인된 동작 보존 최적화면 refactoring, 시간/순서/응답/저장 의미를 바꾸면 bug-fix 또는 사용자 확인.
- "안전하게 개선해줘": 실제 결함과 기대 동작이 있으면 bug-fix, 동작 보존 cleanup이면 refactoring, 위험 여부 판단부터면 analysis.
- "작은 기능 보완해줘": 기대 동작이 이미 구체적이면 bug-fix, 새 정책·UX·도메인 설계가 필요하면 stop/ask-user.
- "분석 후 고쳐줘": analysis가 primary다. finding과 확인 기준을 만든 뒤 결함이면 bug-fix, 동작 보존 정리면 refactoring으로 분리한다.
- "테스트도 같이": 구현이 primary면 bug-fix/refactoring 뒤 검증 companion으로 testing을 고려한다. 테스트만 요청하면 testing이 primary다.

## 동작 변경으로 보는 것

아래 중 하나라도 바뀌면 refactoring이 아니라 bug-fix 또는 사용자 확인 대상이다.

- API status, 응답 body, DTO 필드, exception type/message
- 로그 메시지, metric name, scheduler 주기, 실행 순서, retry 횟수
- DB 쓰기 순서, transaction boundary, flush 타이밍, 외부 API 호출 횟수
- validation, authorization, null/empty 처리, 정렬/필터 결과
- public method signature, Bean wiring, profile/config behavior

## 공통 safety gate

- 작업 전후 `git status --short`로 사용자 변경을 확인한다.
- 기존 사용자 변경을 되돌리거나 덮어쓰지 않는다.
- `allcll-crawler`는 별도 repo 경계로 보고, 사용자 범위에 없으면 들어가지 않는다.
- `application-local.yml`, `.env`, credentials, service account JSON, token/private key 값은 읽거나 출력하지 않는다.
- 범위가 `PROJECT.md`의 변경 범위 가이드를 넘을 것 같으면 구현 전에 멈추고 분할안을 보고한다.
- 확신 없는 원인을 고치지 않는다. "가능성"은 finding이나 확인 필요로 남긴다.
