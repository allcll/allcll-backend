---
name: allcll-testing
description: 테스트 작성·수정 요청 시 자동 발동. 올클은 통합 테스트가 72%(SpringBootTest 50% + WebMvcTest 22%)로 도메인 시나리오 결정은 사람의 몫. AI 독립 작성은 도메인 무관 인프라 유틸(전체의 약 10%)에 한정.
---

# 올클 테스트 작성 가이드 (v3 - 데이터 기반)

## 분류 판단: 이 테스트가 AI 작업 대상인가?

핵심 질문: **시나리오를 코드에서 읽을 수 있는가?**

### 🟢-A 단위 테스트 작성 가능

대상:

- support/ 하위 인프라 유틸 (SSE 빌더, 스케줄러 헬퍼 등)
- 도메인 무관 enum·DTO 변환
- 외부 의존 없음, 입력·출력이 코드로 자명

작업: 분류 명시 후 작성 진행

### 🟢-B 통합 테스트 작성 가능 (시나리오 자명)

대상:

- 단순 CRUD + 권한 검증 Service (예: TimeTableService)
- @SpringBootTest + 실제 Repository 패턴 적용
- 검증 시나리오가 코드만 보고도 자명

판단 기준:

- Service 메서드를 읽었을 때 "이 입력에 이 결과가 나와야 한다"가 명확
- 권한 검증, 토큰 일치 등 단순 분기 로직
- 외부 도메인 결정(예: 정책, 규칙)이 필요 없음

작업:

1. 분류 명시 (🟢-B)
2. @SpringBootTest 통합 테스트로 진행
3. 다른 *ServiceTest 한 개 정도를 컨벤션 참고용으로 보는 것 허용
4. 시나리오는 Service 코드에서 직접 도출

### 🟡 도메인 입력 형태 확인 필요

대상:

- 도메인 enum·정책 (ClassicsArea, Subject 도메인 메서드 등)
- 학번·과목코드 등 도메인 특수 형식을 다루는 파서·검증기

작업 절차:

1. Grep으로 호출부 1개 이상 확인
2. 실전 입력 형태 파악 (예: 학번 = `[21011138]` 8자리 숫자)
3. 형태가 불확실하면 사용자에게 보고

근거: Session 3에서 PrefixParser를 학번 파서가 아닌 과목 코드 파서로 오해.

### 🔴 사람 결정 필요

대상 (좁게):

- 외부 응답 파싱 (`*Fetcher*Parsing*` 등) — 실제 응답 샘플 필요
- SSE·Scheduler·DB 결합 — 시간 Mock 또는 동시성 검증 합의 필요
- 복잡한 비즈니스 로직 — 수강신청 규칙, 졸업 요건 계산 등
- 코드만으로 시나리오 판단이 어려움

❌ 단순 CRUD Service는 🔴이 아니라 🟢-B로 분류.

작업 절차:

1. 작성 중단
2. 다음 형식으로 보고:
    ```
    분류: [🔴 사람 결정 필요]
    이유: [구체적 근거]
    필요한 것: [샘플 / 시나리오 / Mock 전략 / 등]
    대안: [통합 테스트 권장 / 사람 작성 등]
    ```

## 표준 컨벤션 (전 영역 공통)

작성 가능 판정 후 적용. 모두 실제 데이터 기반.

### 검증

- AssertJ 사용 (96% 표준): `assertThat`, `assertThatThrownBy`
- JUnit5 assertions은 보조용으로만

### Mock

- `@MockitoBean` 사용 (11/11 WebMvcTest 표준)
- `@MockBean` 절대 사용 금지 (deprecated, 사용 이력 0)

### 격리

- `@AfterEach` + `deleteAllInBatch()` (18파일 표준)
- `@Transactional`은 lazy loading이 필요한 예외 케이스에만

### 구조·네이밍

- Given-When-Then 주석 (100%)
- `@DisplayName` 한국어 (100%)
- 파일명 규칙:
    - `*ServiceTest`: Service 통합
    - `*ApiTest`: Controller (WebMvc)
    - `*Test`: 도메인 단위
    - `*IntegrationTest`: RestAssured 등 진짜 통합

### Fixture (필수)

- 데이터 생성은 정적 import한 fixture 클래스 사용
- 예: `import static kr.allcll.backend.fixture.SubjectFixture.*;`
- 전 테스트 파일이 이 패턴 사용 → 신규 테스트도 반드시 따를 것
- 새 fixture 필요하면 **먼저 사용자에게 보고**

### 예외 검증 패턴

```
assertThatThrownBy(() -> ...)
.isInstanceOf(AllcllException.class)
.hasMessage(...)
```

도메인 예외는 메시지까지 검증 (타입만 체크 금지).

## 절대 사용 금지 (이력 0)

다음 어노테이션·라이브러리는 프로젝트에서 **한 번도 쓰인 적 없음**. AI가 도입 금지:

- `@MockBean` (deprecated)
- `@DirtiesContext`
- `@Sql`
- Hamcrest

## 절대 원칙

- 통과 자체가 목적인 테스트 금지 (`assertNotNull` 남발 등)
- 함수 결과를 expected에 복붙하는 순환 검증 금지
- 예외 타입만 체크하고 메시지 검증 생략 금지 (도메인 예외)
- 테스트 통과를 위한 프로덕션 코드 수정 금지
- Mock으로 도메인 로직 자체를 우회하지 않기

## 근거

**Session 6 전수 조사 (2026-04-24)**: 50개 테스트 파일 직접 검토.

- 분포: @SpringBootTest 50%, @WebMvcTest 22%, 순수 단위 20%, MockitoExtension 8%
- 표준: AssertJ 96%, @MockitoBean 100%, Fixture 정적 import 100%
- 사용 이력 0: @MockBean, @DirtiesContext, @Sql, Hamcrest

순수 단위 20% 중 AI 작성 적합은 5개(전체의 10%)뿐. 나머지는 도메인 또는 외부 응답 의존.

상세: docs/ai-experiments/[Session 6 일지 파일]
