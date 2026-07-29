# allcll 리팩토링 가이드

## 기본 원칙

refactoring은 **동작 보존 변경**을 다룬다. 같은 외부 결과를 유지하면서 중복을 줄이거나, 구조를 단순화하거나, 이미 확인된 병목을 의미 변화 없이 개선할 때 사용한다.

애매하면 먼저 `../shared/implementation-boundary.md`를 읽고 primary skill을 판정한다. 성능 개선, scheduler/SSE, 외부 연동, baseline 부재, 테스트 실패 위험이 있으면 `references/gotchas.md`도 읽는다. 수정안 리뷰, PR 준비, 완료 판단처럼 작업 품질을 평가해야 하면 `references/quality-criteria.md`를 읽는다.

## 적용 대상

- 중복 제거, 메서드 추출, 조건문 단순화
- Optional/Stream/collection 사용 등 동작 보존 cleanup
- public contract를 유지하는 구조 변경
- 이미 병목이 확인된, 결과 의미를 바꾸지 않는 성능 최적화

## 적용하지 않는 대상

- validation, authorization, API 응답, 저장 결과, 알림 상태처럼 외부 동작을 바꾸는 수정: `allcll-bug-fix`가 primary
- 원인이나 병목이 아직 불명확한 성능/구조 진단: `allcll-analysis`가 primary
- 테스트 작성/수정 자체가 목표: `allcll-testing`이 primary
- 큰 기능, 정책 엔진, 도메인 모델 재설계: 구현 전에 사용자 확인 또는 설계 단계

## 작업 순서

1. **경계와 범위 선언**
   - 보존할 동작과 수정할 파일/메서드 범위를 먼저 정한다.
   - `PROJECT.md` 변경 범위 가이드를 넘을 것 같으면 구현 전 사용자에게 보고한다.
   - "분석 후 정리" 요청은 analysis가 primary이고, 동작 보존 후보가 확인된 뒤 refactoring으로 넘어온다.

2. **baseline 확보**
   - 수정 전에 관련 테스트를 실제로 실행한다.
   - 테스트가 없으면 바로 구현하지 말고 수동 검증 시나리오 또는 테스트 필요성을 보고한다.
   - 환경 의존 실패가 있으면 변경 전후 같은 실패인지 비교할 수 있게 기록한다.

3. **보존 동작 명시**
   - 반환값, 예외, 로그, API 응답, 저장 순서, scheduler timing, 외부 호출 횟수 중 무엇을 보존할지 적는다.
   - 성능 개선이면 결과 보존 조건과 측정 없이 단정하지 않을 항목을 함께 적는다.

4. **최소 구현**
   - 지정 범위 안에서 기존 패턴을 따른다.
   - 테스트 기대값을 리팩토링에 맞춰 바꾸지 않는다.
   - 동작 변경 가능성이 있는 오타, 메시지, 정책 값은 수정하지 않고 bug-fix 후보로 분리한다.
   - 민감 설정 파일 값과 `allcll-crawler`는 범위 밖이면 보지 않는다.

5. **재검증**
   - baseline과 같은 테스트를 다시 실행한다.
   - 실패하면 구현 문제인지 기존 실패인지 구분한다.
   - 테스트 실패를 정보로 보고, 필요하면 내가 만든 변경만 되돌려 더 작은 변경으로 다시 시도한다.

## 완료 조건

- primary가 refactoring인 이유와 다른 skill이 primary가 아닌 이유를 판단했다.
- 수정 전 baseline 또는 baseline 부재 보고가 있다.
- 보존해야 할 동작이 구체적으로 적혀 있다.
- 테스트 코드와 외부 관찰 가능한 문자열을 임의 변경하지 않았다.
- 변경 후 같은 검증을 다시 실행했거나, 실행하지 못한 이유와 대체 검증을 적었다.
- 분리한 인접 문제와 적용 안 한 개선점이 "해당 없음"까지 포함해 보고됐다.
- PR/리뷰 단계에서는 `references/quality-criteria.md` 기준으로 baseline, 보존 동작, diff 성격, 재검증을 점검했다.

## 실패 시 행동

- 결함 수정으로 판정: bug-fix로 분기한다.
- 원인/병목 미확정: analysis로 분기한다.
- 테스트만 필요한 상황: testing으로 분기한다.
- baseline이 없고 검증 방법도 불명확: 구현을 멈추고 사용자에게 확인한다.
- 범위가 커짐: 분할안과 검증 전략을 보고한다.
- 민감 파일 확인이 필요함: 파일을 열지 말고 redacted evidence를 요청한다.

## 자가 보고 형식

작업 끝나면 아래 5개를 모두 적는다. 해당 사항이 없으면 "해당 없음"으로 적는다.

1. 변경 파일 목록과 라인 수
2. 변경 핵심 한 줄 요약과 보존 동작
3. Baseline과 재검증 결과
4. 분리한 인접 문제
5. 적용 안 한 개선점

## 절대 원칙

- baseline 없이 동작 보존을 단정하지 않는다.
- 리팩토링을 위해 테스트 기대값을 바꾸지 않는다.
- 로그, 예외 메시지, API 응답, scheduler timing, transaction boundary 변경은 동작 변경으로 취급한다.
- `allcll-crawler`, `.env`, `application-local.yml`, credentials, service account JSON, token/private key 값은 읽거나 출력하지 않는다.
- 기존 사용자 변경을 되돌리지 않는다.
