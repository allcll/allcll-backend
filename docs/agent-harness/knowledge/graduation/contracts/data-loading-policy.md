# 졸업요건 데이터 적재 정책

구글 시트 → MySQL DB 적재의 lifecycle 계약. **이 파일은 `AdminGraduationSyncService` 의 동작 사양**이다.
시트가 ground truth 인 만큼, *어떻게* 시트가 DB 로 옮겨지는가가 시스템 전체 신뢰의 출발점.

마지막 갱신: 2026-05-18 (코드 기준: `AdminGraduationSyncService.java`)
관련 파일: `support/sheet/`, `admin/graduation/AdminGraduationSyncService`, `support/sheet/validation/*`

---

## 1. 트리거 (Who/When 데이터를 적재하는가)

### 1.1 유일한 트리거: 어드민 수동 호출

- 엔드포인트: `POST /api/admin/graduation/sync` (`AdminGraduationApi#syncGraduationRules`)
- 인증: `AdminRequestValidator` 의 rate limit + 권한 검증 (401 반환)
- **자동 스케줄러 없음** — 시트 수정 후 사람이 명시적으로 호출해야 DB 반영
- 부팅 시 자동 적재도 없음 (테스트 환경 `app.runner.enabled=false` 와 무관 — 운영도 마찬가지)

### 1.2 정책적 의미

- "시트 수정 = 즉시 운영 반영" 이 *아님*. 항상 사람 결정의 sync 호출이 끼어있음
- 동기화 누락 시 운영 데이터가 stale — sync 호출 누락이 무음 장애 원인이 될 수 있음
- 학기 시작 시 sync 호출은 **운영 절차의 일부**로 박제 필요 (../decisions/ ADR 후보)

---

## 2. 적재 순서 (Why this order)

`syncGraduationRules()` 의 호출 순서:

| 순서 | 메서드 | 탭 키 | 비고 |
|---|---|---|---|
| 1 | `syncCreditCriteria` | `credit_criteria` | 기본 기준 (가장 큼) |
| 2 | `syncDoubleCreditCriteria` | `double_credit_criteria` | 1 의 override |
| 3 | `syncRequiredCourses` | `required_courses` | 학과별 필수 과목 |
| 4 | `syncCourseEquivalences` | `course_equivalences` | 동일 과목 인정 매핑 |
| 5 | `syncBalanceRequiredRule` | `balance_required_rules` | 균형교양 규칙 |
| 6 | `syncBalanceRequiredCourseAreaMap` | `balance_required_course_area_map` | 과목 → 영역 매핑 |
| 7 | `syncBalanceRequiredAreaExclusion` | `balance_required_area_exclusions` | 학과별 영역 제외 |
| 8 | `syncGraduationCertRule` | `graduation_cert_rules` | 인증제 적용 규칙 |
| 9 | `syncEnglishCertCriteria` | `english_cert_criteria` | 영어 인증 기준 |
| 10 | `syncCodingCertCriteria` | `coding_cert_criteria` | 코딩 인증 기준 |
| 11 | `syncClassicCertCriteria` | `classic_cert_criteria` | 고전 인증 기준 |
| 12 | `syncGraduationDepartmentInfo` | `graduation_department_info` | 학과 메타 (인증 대상 분류 포함) |

### 2.1 순서의 의미 (관찰)

- 코드에 외래키 제약은 없음 (JPA 엔티티에 `@ManyToOne` 없음)
- 그래서 *기술적으로*는 순서가 결과에 영향 없음
- 그러나 *논리적 의존*은 있음: 예) `graduation_department_info` 의 `english_target_type` 은 9 의 `english_cert_criteria` 분류와 매칭됨. department-info 가 마지막인 건 다른 모든 데이터가 들어간 뒤 "매핑 키" 격으로 위치한다는 신호
- **변경 시 주의**: 순서 바꿔도 컴파일·테스트 통과하지만, 한 탭만 실패해서 부분 롤백 분석할 때 *어디까지 들어갔다가 롤백됐나* 추적이 더 어려워짐. 변경하려면 ../decisions/ ADR

### 2.2 시트1 (스크래치) 처리

- 구글 시트에 13번째 탭 외 `시트1` (기본 스크래치) 존재 — 코드에서 처리 안 함
- `tabName(tabKey)` 에 등록되지 않은 탭은 fetch 자체가 안 일어남 → 안전

---

## 3. 적재 패턴: Delete-All + Save-All (Full Replace)

각 sync 메서드는 정확히 같은 패턴:

```java
private void syncXxx() {
    String tabKey = XxxSheetValidator.TAB_KEY;
    GraduationSheetTable table = fetchAndValidate(tabKey);  // fetch + validate

    List<Entity> entities = new ArrayList<>();
    for (List<Object> row : table.getDataRows()) {
        entities.add(new Entity(...));  // 행 → 엔티티 변환
    }

    repository.deleteAllInBatch();  // 기존 전부 삭제
    repository.saveAll(entities);    // 새로 전부 적재

    log.info("[졸업요건 데이터 동기화] 탭 이름={}, {}개 저장 완료", tabKey, entities.size());
}
```

### 3.1 정책적 의미

- **PK (id) 가 매번 재발급됨** — 다른 테이블에서 이 PK 로 외래키 거는 건 위험 (현재는 안 함, 유지 필요)
- **incremental update 불가능** — 한 행만 수정해도 전체 재적재
- **soft delete 없음** — 시트에서 빼면 DB 에서도 사라짐. 운영 중인 사용자가 검사 중일 때 sync 돌면 *없어진 기준* 으로 결과 산출 위험 (트랜잭션 격리 의존)
- **순차 처리** — 12개 탭이 한 트랜잭션 안에서 순차로 12번의 deleteAll + saveAll. 한 탭의 행이 많으면 락 유지 시간 길어짐

### 3.2 대안 패턴 (적용 안 함, 설명용)

- merge/upsert 패턴: 시트 vs DB 비교 후 변경분만 적용. 구현 복잡, 외래키 안전. 현재 안 씀
- versioned snapshot 패턴: 버전 ID 부여 후 새 버전 적재 → 원자적 swap. 운영 절차 복잡, 안전성 최고. 현재 안 씀
- **현재 선택의 trade-off**: 단순성·결정성 우선, 운영 중 sync 호출의 사용자 영향은 트랜잭션 격리에 위임

---

## 4. 검증 단계

### 4.1 두 단계 검증

1. **구조 검증** (fetch 직후): `GraduationSheetTable.from(tableValues)` 에서 헤더 인덱스 생성 시 빈 헤더 무시 등 기본 정규화
2. **시트별 검증** (`GraduationSheetValidatorRegistry` 가 dispatch): `*SheetValidator.validate(sheetTable)`
    - 필수 헤더 존재 (`REQUIRED_HEADERS`)
    - 각 행의 필수 컬럼 타입·enum 일치 (`requireInt`, `requireEnum`, `requireString`, `requireBoolean`)

### 4.2 검증 범위의 한계 (중요)

- **형식만 검증**: 컬럼 존재·타입 일치·enum 값 범위
- **검증 안 하는 것**:
    - 키 유일성 (같은 (admission_year, dept_cd, ...) 조합이 2회 나와도 안 잡음)
    - 합계 정합성 (카테고리별 합 vs TOTAL_COMPLETION)
    - 참조 무결성 (`graduation_department_info` 에 없는 dept_cd 가 `credit_criteria` 에 있어도 안 잡음)
    - admission_year vs admission_year_short 정합 (EC-008)
    - 의미상 일치 (수강편람 PDF 값과 시트 값) — `graduation-data-validation` skill 의 영역
- 즉 **validator 통과 = 적재 가능, 절대 = 옳음 아님**

### 4.3 검증 실패 처리

- `*SheetValidator.validate()` 가 예외 던지면 → `fetchAndValidate()` 도 예외 → `@Transactional` 롤백
- 이미 적재된 (sync 순서상 앞선) 탭도 *전부 롤백* — 이게 "단일 트랜잭션" 설계의 핵심 이득
- 사용자 영향: 401 또는 500 응답. **부분 적재 상태로 운영에 들어가는 시나리오 없음**

---

## 5. 외부 의존 (Google Sheets API)

### 5.1 호출

- `GraduationSheetFetcher.fetchAsTable(tabName)` → Google Sheets API `spreadsheets.values.get`
- 범위: `tabName + "!A:Z"` (`TAB_RANGE_SUFFIX`) — A부터 Z 컬럼까지 (Z 초과 컬럼은 잘림)

### 5.2 실패 처리

- `GoogleJsonResponseException` + 404 → `GOOGLE_SHEET_TAB_NOT_FOUND`
- 그 외 `GoogleJsonResponseException` → `GOOGLE_SHEET_ERROR`
- 일반 `Exception` → `GOOGLE_SHEET_ERROR` (cause 전달 안 됨 — 디버깅 시 스택 트레이스 손실, **개선 후보**)

### 5.3 재시도·rate limit 없음

- Sheets API 호출은 단발 시도. 일시 실패 시 sync 전체 실패
- Google Sheets API rate limit (분당 100회 등) 초과 가능성 있으나 현재 보호 코드 없음
- 12개 탭 = 12회 호출 (한 sync 당). 학기 시작 시 1~수회 sync 호출이라면 안전. 디버깅 중 반복 호출은 주의

### 5.4 자격 증명

- `GraduationSheetProperties` 의 `credentialsLocation` (application-local.yml 의 `google.sheets.credentials-location`)
- `.env`, credentials, Google 서비스 계정 파일은 커밋 금지 (CLAUDE.md)

---

## 6. 트랜잭션·동시성

### 6.1 트랜잭션 경계

- `syncGraduationRules()` 전체가 `@Transactional` — 단일 트랜잭션
- 12개 탭의 fetch (외부 API) + validate + delete + save 가 모두 같은 트랜잭션에 포함
- 외부 API 호출이 트랜잭션 안에 있는 건 일반적으로 안티패턴이지만, 여기서는 *부분 적재 방지* 가 더 큰 가치라서 의도적 선택으로 보임

### 6.2 동시성 정책

- `POST /api/admin/graduation/sync` 가 동시 호출되면? — 락 코드 없음. 두 트랜잭션이 동시에 deleteAll → saveAll 할 수 있음
- 발생 가능 시나리오:
    - 두 어드민이 동시 호출 → 마지막 commit 이 이김. 중간 결과는 deadlock or 한쪽 fail 가능성
    - JPA 의 `deleteAllInBatch` 는 single DELETE 문이라 일부 DB 에서 락 유지 시간 짧음 — 그래도 race 가능
- **개선 후보**: `@Lock` 또는 어드민 단일 동시성 보장 (../decisions/ ADR)

### 6.3 사용자 검사 vs sync 동시 발생

- 사용자가 `GraduationChecker.calculate()` 호출 중 sync 가 시작하면:
    - MySQL 기본 격리 수준 (REPEATABLE READ) 이면 사용자 트랜잭션은 sync 이전 스냅샷 봄
    - 사용자 검사 중에 reader 가 락 잡는 SELECT 가 아니라 일반 SELECT 면 → consistent read 로 안전
    - 다만 사용자 트랜잭션이 길면 (성적표 파싱+검사) sync 가 그동안 대기 또는 사용자 트랜잭션 중 데이터 변경 못 봄
- **정책 결정**: 사용자 트랜잭션이 짧다는 가정에 의존. 실측 미확인 → ../decisions/ ADR 후보

---

## 7. 관측 (Logging)

각 sync 메서드는 일관된 로그 형식:

```
[졸업요건 데이터 동기화] 시작
[졸업요건 시트 검증 시작] tabKey=credit-criteria
[졸업요건 시트 검증 완료] tabKey=credit-criteria
[졸업요건 데이터 동기화] 탭 이름=credit-criteria, 5234개 저장 완료
... (12개 탭 반복)
[졸업요건 데이터 동기화] 완료
```

### 7.1 알려진 로그 결함

- `syncGraduationCertRule` (line 300) 의 로그 메시지: `"... {}개 저장 완료}"` — 닫는 중괄호 오타
- 외부 시스템이 이 로그를 파싱하지 않으면 무해. 파싱 의존 있으면 수정 필요
- 분류 D (버그 수정) 후보. 단 *동작 변경* 으로 보고 graduation-domain 분류 D 로 분기 (refactoring 스킬의 "로그 메시지도 외부 시스템 의존 가능" 원칙)

### 7.2 관측 갭

- **저장된 행 수만 로그** — 변경 비율 (예: "기존 5230 → 신규 5234, 4행 증가") 안 보임
- **검증 실패 시 어느 행에서 실패했는지** 는 validator 내부 예외 메시지에 의존
- 메트릭 수집 없음 (sync 빈도·소요 시간 등)

---

## 8. 시트 ↔ 엔티티 매핑 표

| 탭 키 | Validator | 엔티티 | Repository | sync 메서드 |
|---|---|---|---|---|
| `credit_criteria` | `CreditCriteriaSheetValidator` | `CreditCriterion` | `CreditCriterionRepository` | `syncCreditCriteria` |
| `double_credit_criteria` | `DoubleCreditCriteriaSheetValidator` | `DoubleCreditCriterion` | `DoubleCreditCriterionRepository` | `syncDoubleCreditCriteria` |
| `required_courses` | `RequiredCoursesSheetValidator` | `RequiredCourse` | `RequiredCourseRepository` | `syncRequiredCourses` |
| `course_equivalences` | `CourseEquivalencesSheetValidator` | `CourseEquivalence` | `CourseEquivalenceRepository` | `syncCourseEquivalences` |
| `balance_required_rules` | `BalanceRequiredRulesSheetValidator` | `BalanceRequiredRule` | `BalanceRequiredRuleRepository` | `syncBalanceRequiredRule` |
| `balance_required_course_area_map` | `BalanceRequiredCourseAreaMapSheetValidator` | `BalanceRequiredCourseAreaMap` | `BalanceRequiredCourseAreaMapRepository` | `syncBalanceRequiredCourseAreaMap` |
| `balance_required_area_exclusions` | `BalanceRequiredAreaExclusionsSheetValidator` | `BalanceRequiredAreaExclusion` | `BalanceRequiredAreaExclusionRepository` | `syncBalanceRequiredAreaExclusion` |
| `graduation_cert_rules` | `GraduationCertRulesSheetValidator` | `GraduationCertRule` | `GraduationCertRuleRepository` | `syncGraduationCertRule` |
| `english_cert_criteria` | `EnglishCertCriteriaSheetValidator` | `EnglishCertCriterion` | `EnglishCertCriterionRepository` | `syncEnglishCertCriteria` |
| `coding_cert_criteria` | `CodingCertCriteriaSheetValidator` | `CodingCertCriterion` | `CodingCertCriterionRepository` | `syncCodingCertCriteria` |
| `classic_cert_criteria` | `ClassicCertCriteriaSheetValidator` | `ClassicCertCriterion` | `ClassicCertCriterionRepository` | `syncClassicCertCriteria` |
| `graduation_department_info` | `GraduationDepartmentInfoSheetValidator` | `GraduationDepartmentInfo` | `GraduationDepartmentInfoRepository` | `syncGraduationDepartmentInfo` |
| `course_replacements` | (확인 필요 — sync 메서드에서 사용 안 함) | (확인 필요) | (확인 필요) | **없음** |

### 8.1 미해결: `course_replacements`

- 시트에는 탭이 존재 (`course_replacements`)
- `AdminGraduationSyncService` 에 sync 메서드 없음
- worktrees 에는 `CourseReplacementsSheetValidator` 가 보였으나 main 에는 없음
- **가설**: 미적용 신규 탭이거나, 다른 sync 서비스가 따로 있을 수 있음
- **검수 필요**: 사용자가 운영 현황 확인 (분류 B 의 갭일 가능성)

---

## 9. 알려진 결함·개선 후보

### 분류 D (버그 수정 후보)

- DF-001: `syncGraduationCertRule` log 오타 `완료}` (line 300)
- DF-002: `GraduationSheetFetcher` 의 마지막 `catch (Exception e)` — cause 전달 누락. 디버깅 시 스택 트레이스 손실

### 분류 B (스키마·구조 변경 후보)

- ST-001: `course_replacements` 탭이 시트에 있는데 sync 메서드 없음 — 의도된 미적용인지 누락인지 확인 필요
- ST-002: 검증이 형식만 — 키 유일성·합계 정합성 보강 (validator 강화 또는 별도 의미 validator 신설)
- ST-003: admission_year vs admission_year_short 정합 validator 없음 (EC-008)

### 분류 C (리팩토링 후보)

- RF-001: 12개 sync 메서드의 *delete-all + save-all + log* 패턴 중복. 제네릭화 가능 (단 각 엔티티 생성자 시그니처가 달라 trade-off 있음)
- RF-002: `GraduationSheetFetcher` 의 마지막 catch 가 cause 전달 안 함. 단순 수정이지만 *동작 변경* (스택 트레이스 추가 = 외부 의존 가능) → DF-002 와 같음

### 분류 A (정책 결정 필요)

- PD-001: 운영 절차에 "학기 시작 sync 호출" 박제 (../decisions/ ADR 필요)
- PD-002: 동시 sync 호출 처리 정책 (락? 큐? 동시 호출 금지?)
- PD-003: 사용자 검사 중 sync 호출 시 결과 일관성 보장 정책

---

## 10. 검수 상태

- [x] 트리거 (단일 어드민 엔드포인트): 코드 일치
- [x] 단일 `@Transactional`: 코드 일치
- [x] Delete-All + Save-All 패턴: 12 sync 메서드 모두 동일 확인
- [x] 12개 탭 sync 순서: 코드 일치
- [x] 시트 ↔ 엔티티 매핑 표: 코드 일치
- [ ] **`course_replacements` 탭의 의도** (시트 존재, sync 없음): 운영 확인 필요
- [ ] **순서 의존성의 정책 의도**: PR 히스토리·구두 합의 확인 필요
- [ ] **동시 sync 호출 처리 정책**: 미정 → ADR 필요
- [ ] **rate limit 보호 필요성**: Google API 사용량 실측 필요
