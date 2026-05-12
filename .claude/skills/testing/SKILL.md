---
name: allcll-testing
description: 테스트 작성·수정 요청 시 반드시 사용. 올클은 통합 테스트가 72%, 분류 시스템(🟢-A/🟢-B/🟡/🔴)으로 AI 작업 범위를 결정한다. 단위 테스트, 통합 테스트, WebMvcTest, AssertJ·@MockitoBean·Fixture 정적 import 컨벤션 적용. 테스트 관련 작업이면 코드 짜기 전에 이 스킬을 먼저 발동.
---

# 올클 테스트 작성 가이드 (v3 - 데이터 기반)

## 분류 판단: 이 테스트가 AI 작업 대상인가?

핵심 질문: **시나리오를 코드에서 읽을 수 있는가?**

### 분류 결정 순서 (반드시 위에서부터)

1. **패키지·클래스명 패턴**으로 🔴 후보 먼저 체크. 해당하면 코드를 더 보기 전에 🔴 — "강등 금지 영역" 섹션 참고.
2. 외부 의존 없으면 🟢-A.
3. 단순 CRUD + 권한 검증이면 🟢-B.
4. 도메인 입력 형태 확인이 필요하면 🟡.

코드 길이가 짧거나 책임이 좁다는 이유로 🔴 후보를 강등하지 말 것 — "강등 정당화 패턴 차단" 섹션 참고.
근거: iteration-1 eval-4 (GraduationSheetFetcher) 에서 fetcher 책임 범위로 🔴→🟡 강등 시도 → mock 가정이 실 응답과 어긋날 위험
노출.

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

1. Grep 으로 호출부 1개 이상 확인
2. 실전 입력 형태 파악 (예: 학번 = `[21011138]` 8자리 숫자)
3. 형태가 코드·호출부에서 자명하면 작성 진행 (예: parser 위 주석에 컬럼 인덱스가 박제되어 있는 경우)
4. 형태가 불확실하면 작성 중단 후 사용자에게 보고

근거: Session 3에서 PrefixParser를 학번 파서가 아닌 과목 코드 파서로 오해.

### 🔴 사람 결정 필요

다음 영역은 **무조건 🔴**. 강등 금지.

#### 강등 금지 영역 (코드 길이·책임 범위와 무관)

- **외부 응답 의존 fetcher·parser**: `client/` 하위, `support/sheet/`, `*Fetcher*`, Google API/HTTP 응답을 mock
  한 단위 테스트 일체
    - 이유: 응답 형식이 변하면 mock 은 거짓 통과시킨다. mock 으로 분기를 정교하게 잡을수록 실 응답과 격차가 커짐. iteration-1 eval-4 에서 동일
      패턴 실패.
- **SSE·Scheduler 가 DB 트랜잭션과 함께 묶이는 시나리오**: `support/sse/` 또는 `support/scheduler/` + 실제 Repository
  연동
    - 이유: 시간 Mock 전략·동시성 가정을 사용자와 합의해야 정합.
- **정책·규칙 기반 도메인**: `domain/graduation/check/` 의 졸업 요건 계산, 수강신청 규칙
    - 이유: 시나리오가 정책 문서에 있고 코드만으론 의도 복원 불가.

#### 강등 정당화 패턴 차단

다음 사유로 🔴 영역을 🟡 이하로 강등하지 말 것:

- ❌ "fetcher 의 책임이 좁아 mock 만으로 분기 검증 가능"
- ❌ "외부 응답 형식이 단순해서 추측 가능"
- ❌ "통상 🔴 이지만 이 케이스는 예외"
- ❌ "Mock 으로 ValueRange / ResponseEntity 만 잡으면 됨"
- ❌ "정상 응답·404 분기 정도는 자명"

근거 (iteration-1 eval-4): GraduationSheetFetcher 에 위 사유로 🟡 강등이 시도됨 → 실 Google Sheets 응답 형식이 바뀌면 mock
통과·프로덕션 실패. 강등 결정은 사용자만 한다.

#### ❌ 반대 방향 강등 금지

단순 CRUD Service 는 🔴 이 아니라 🟢-B 로 분류. 외부 의존이 없는 코드를 "조심스러우니" 🔴 로 올리지 말 것.

#### 작업 절차

1. 분류 즉시 🔴 명시 후 **작성 중단**
2. 다음 형식으로 보고:
    ```
    분류: [🔴 사람 결정 필요]
    이유: [패키지/클래스명 패턴 또는 강등 금지 영역 해당 항목]
    필요한 것: [실 응답 샘플 / Mock 전략 합의 / 정책 문서]
    대안: [통합 테스트 권장 / 사람 작성 권장]
    ```
3. 사용자가 "그래도 단위 테스트로 진행" 이라고 **명시적으로 승인할 때만** 작성. 그 경우에도 mock 가정을 notes 에 박제.

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
- **새 fixture 신설은 사전 보고 필요** (테스트 파일 작성 전에). 자율 결정 금지.
    - 근거: 다른 테스트가 같은 도메인 fixture 를 다른 시그니처로 만들면 일관성 깨짐.

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
- 테스트 간 순서 의존성 금지 (각 테스트 독립 실행 가능해야 함)

## 근거

**Session 6 전수 조사 (2026-04-24)**: 50개 테스트 파일 직접 검토.

- 분포: @SpringBootTest 50%, @WebMvcTest 22%, 순수 단위 20%, MockitoExtension 8%
- 표준: AssertJ 96%, @MockitoBean 100%, Fixture 정적 import 100%
- 사용 이력 0: @MockBean, @DirtiesContext, @Sql, Hamcrest

순수 단위 20% 중 AI 작성 적합은 5개(전체의 10%)뿐. 나머지는 도메인 또는 외부 응답 의존.

상세: docs/ai-experiments/[Session 6 일지 파일]

## 비동기 검증 패턴

`Thread.sleep()` 절대 금지. Awaitility 사용:

​```java
await().atMost(2, SECONDS)
    .untilAsserted(() -> assertThat(...).isEqualTo(...));
​```

대상:

- SSE 이벤트 수신 검증
- @Scheduled 작업 결과 검증
- 비동기 큐 flush 검증