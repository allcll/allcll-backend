# MAJOR_BASIC 학번 보정 로직 위치 결정 (Parser vs Policy vs fromRaw)

- 결정 일자: 2026-02-21 ~ 2026-03-02 (PR #278 논의 + 후속 리팩토링)
- 결정자: @goldm0ng (제안), @boyekim, @haeyoon1, @2Jin1031 (리뷰)
- PR: allcll/allcll-backend#278 (TSK-56-125)

## 컨텍스트

24학번부터 성적표에 `전공기초(전기)` 이수구분이 신설됐는데, 20~23학번 복수전공 성적표에도 `[전기]` 가 섞여 표기되는 케이스 발견.

- 18~19학번: 학문기초교양의 레거시 명 = "전공기초교양" → 성적표에 `[기교]` 로 찍힘 → ACADEMIC_BASIC alias 매핑으로 정상 처리됨
- 20~23학번 단일 전공: 성적표에 `[기필]` 로 찍힘 → 정상
- 20~23학번 복수 전공: 성적표에 `[전기]` 가 섞여 표기 → MAJOR_BASIC 으로 파싱되어 ACADEMIC_BASIC 집계에서 누락 (버그)

따라서 18~23학번에서 성적표 이수구분이 `[전기]` 로 들어오면 `[기필]` 로 보정해야 함.

## 옵션

1. **파싱 단계** (`CompletedCourseDto.convertCategoryType` 또는 `CategoryType.fromRaw`)
2. **정책 레이어** (`MajorBasicPolicy` 별도 클래스 신설)

## 결정 흐름 (시간 순)

### 1차 결정 (PR #278, 2026-02-21~22)

**옵션 2 선택**: `MajorBasicPolicy` 신설

**근거** (@goldm0ng):
- GradeExcelParser 는 순수 파싱 역할로 두고, 학번 의존 예외 규칙은 정책 레이어가 책임
- 미래에 다른 학번 의존 규칙 추가 시 정책 클래스가 모이는 구조가 확장 용이

**반대 의견** (@boyekim):
- 다른 로직이 처음부터 CategoryType 값을 믿을 수 있어야 함
- 파싱 후 정책 통과 전에도 CategoryType 기반 로직이 생길 수 있음 → 또 다른 엣지 발생 가능
- 첫 단계에서 정규화하면 추후 헷갈리지 않음

**보류 결정** (@haeyoon1, @2Jin1031 의견 수렴 후): 우선 옵션 2 로 머지, 추후 재검토

### 2차 결정 (후속 리팩토링, ~2026-03-02 머지 후)

**옵션 1 변형으로 이동**: `CategoryType.fromRaw(raw, admissionYear)` 에 통합

`MajorBasicPolicy` 클래스 제거 → `CategoryType.fromRaw` 가 학번 받아 normalize:

```java
public static CategoryType fromRaw(String raw, int admissionYear) {
    CategoryType type = ...alias matching...;
    if (isMajorBasic(type) && admissionYear < 2024) {
        return ACADEMIC_BASIC;
    }
    return type;
}
```

**근거** (추정 — 명시 ADR 없음):
- @boyekim 우려가 결국 채택됨: 파싱 후 모든 코드가 정규화된 CategoryType 신뢰 가능
- `MajorBasicPolicy` 가 *호출 시점* 의존 (Calculator 가 호출해야만 정규화 발생) — 호출 누락 시 무음 버그 가능
- enum 정적 메서드에서 처리하면 *진입점 통일* + 호출 의무 없음

## 결정

**최종 (현행 코드)**: `CategoryType.fromRaw(raw, admissionYear)` 에 통합. `MajorBasicPolicy` 클래스 제거.

## 영향

코드:
- `CategoryType.fromRaw` 에 `admissionYear` 파라미터 + `normalizeMajorBasic` private 메서드
- `MajorBasicPolicy` 클래스 제거
- `GradeExcelParser` 는 raw 문자열만 파싱, `CategoryType.fromRaw(raw, admissionYear)` 호출

knowledge:
- edge-cases.md EC-001 에 정책 출처 + 폐기 이력 박제
- data-usage-policy.md §11.2 카테고리 매핑·보정 정책 박제

## 미해결

- **수강편람 정확 페이지 인용** — 학번 < 2024 [전기] 가 학문기초교양으로 인정된다는 정책의 1차 출처. 학사팀 문의 필요 (PR #278 @boyekim 언급)
- 결과적으로 18~19학번 [기교] → ACADEMIC_BASIC, 18~23학번 [전기] → ACADEMIC_BASIC, 24+ [전기] → MAJOR_BASIC. 일관성 있는 시나리오인지 운영 검증 필요

## 검수 후속 작업

- [x] 코드 머지 (PR #278)
- [x] knowledge 박제 (EC-001 갱신, data-usage-policy.md 보강)
- [ ] 학교 측 [전기] 표기 정책 확답 (학사팀 문의)
- [ ] 24+ 학번 단일 전공자에 [전기] 가 등장하는 케이스 있는지 운영 확인 (있다면 보정 추가 필요)
