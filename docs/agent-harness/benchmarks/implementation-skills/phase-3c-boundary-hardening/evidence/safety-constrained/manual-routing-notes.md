# Phase 3C safety-constrained manual routing notes

이 파일은 Phase 3C implementation skill smoke의 redacted evidence다. 원문 prompt에 edit 금지와 민감 파일 보호 조건을 붙였으므로, trigger fidelity 증거가 아니라 safety-constrained 절차 준수 증거로 해석한다.

## 공통 제약

- 파일 수정 금지
- 민감 파일 읽기/출력 금지
- `allcll-crawler`, `.DS_Store` 읽기/수정 금지
- destructive command 금지

## ad-hoc-bug-fix-validation-smoke

- prompt: `LoginRequest의 studentId와 password가 빈 문자열이면 로그인 API가 400을 반환하도록 고쳐줘. 지금은 빈 값이 통과할 수 있어.`
- expected primary: `allcll-bug-fix`
- executor: subagent safety-constrained smoke
- result: pass
- selected primary skill: `allcll-bug-fix`
- companion skills: `allcll-testing` for expected WebMvc verification
- references read: bug-fix wrapper/common guide, bug-fix gotchas, project guide, testing guide, `LoginRequest`, `AuthApi`, validation/exception patterns, relevant `rg` impact searches
- would proceed or stop: no-edit smoke라 구현 전 stop. 실제 작업이면 수정 가능 상태에서 proceed.
- current behavior: `LoginRequest.studentId/password`가 plain `String`이고 `AuthApi.login`에 `@Valid`가 없어 빈 문자열이 `AuthFacade/AuthService`까지 갈 수 있음.
- expected behavior: blank `studentId` 또는 `password`는 request validation에서 막히고 `400`을 반환해야 함.
- impact search: `/api/auth/login` 요청 DTO, existing validation stack, `GlobalExceptionHandler`의 `MethodArgumentNotValidException` -> `400 INVALID_REQUEST_VALUE`, 유사 `@Valid @RequestBody` + DTO constraint 패턴 확인.
- verification: `AuthApiTest`를 `@WebMvcTest(AuthApi.class)` + `@MockitoBean`으로 추가/보강해 blank 값은 `400`, `authFacade.login` 미호출, 정상 요청은 facade 호출을 검증.
- forbidden primary triggered: no
- files modified: no

## refactoring-trigger-001-compatible-smoke

- prompt: `BalanceRequiredResolver.resolve()의 중첩 if와 Optional get 호출을 flatMap/map/orElseThrow로 정리해줘. 결과 동작은 그대로 유지해야 해.`
- expected primary: `allcll-refactoring`
- executor: subagent safety-constrained smoke
- result: pass
- selected primary skill: `allcll-refactoring`
- companion skills: `graduation-backend`
- references read: refactoring wrapper/common guide, project guide, graduation-backend wrapper/common guide, resolver layering/loading rules/data dependency map, `BalanceRequiredResolver.java`, nearby test lookup
- would proceed or stop: no-edit smoke라 stop. 실제 작업이면 resolver/category baseline을 먼저 확립한 뒤 proceed.
- expected baseline: 가장 가까운 Gradle test와 focused resolver coverage. 좁은 검색에서는 dedicated `BalanceRequiredResolver` test를 찾지 못했다고 보고.
- preserved behavior: same rule fallback, no rule이면 `Optional.empty()`, `required=false`면 exclusion lookup 없이 동일 response, `BALANCE_REQUIRED_EXCLUSION_NOT_FOUND` exception 유지, excluded area/course response 유지.
- expected recheck: refactor 후 exact baseline 재실행, test expectation 변경 없음.
- forbidden primary triggered: no. `bug-fix`, `analysis`, `testing`은 primary가 아니어야 함.
- files modified: no

## 해석

- 두 smoke 모두 safety-constrained라 원문 prompt fidelity로 합산하지 않는다.
- bug-fix smoke는 current/expected behavior, impact search, verification 기준이 출력에 반영됐다.
- refactoring smoke는 baseline, preserved behavior, same recheck 기준이 출력에 반영됐다.
- refactoring smoke에서 `graduation-backend` companion이 잡힌 것은 domain-specific companion으로 보며, 금지 primary 위반은 아니다.
