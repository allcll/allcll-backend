# 교양·전공 Resolver 분리 결정

- 결정 일자: 2026-02-16 (PR #255 머지)
- 결정자: @goldm0ng (제안), @boyekim, @haeyoon1, @2Jin1031 (리뷰)
- PR: allcll/allcll-backend#255 (TSK-54)

## 컨텍스트

사용자 기준 졸업요건 이수구분별 조회 API (`/api/graduation/categories`) 구현 시, *어떤 카테고리가 있고 어떤 과목·학점이 필요한지* 응답해야 함.

졸업요건은 크게 **교양 영역** 과 **전공 영역** 으로 나뉘는데:
- 교양: MajorType 와일드카드 (ALL) 적용, 학과별 override 패턴
- 전공: 학과 종속성 강함, 복수전공은 학과 페어 예외 존재

두 영역은 *DB 적재 방식·조회 정책·예외 처리* 가 달라서 하나의 로직으로 통합 불가.

## 옵션

1. 단일 서비스에서 분기 처리 (`if (categoryType.isMajor) ... else ...`)
2. 교양·전공 Resolver 클래스 분리
3. 카테고리 타입별 strategy 패턴 (모든 카테고리마다 Resolver)

## 결정

**옵션 2**: `NonMajorCategoryResolver` + `MajorCategoryResolver` + `BalanceRequiredResolver` 분리

## 근거

- 도메인 성격이 다름 (교양은 학과 무관 기본 + 학과 override, 전공은 학과 페어 예외)
- 카테고리 타입별 분리 (옵션 3) 는 과잉 — 같은 책임이 너무 잘게 쪼개짐
- 균형교양은 영역 정책이 추가로 들어가 별도 Resolver 필요 (BalanceRequiredResolver)
- 복수전공 예외는 DoubleCreditCriterionResolver 로 한 번 더 분리 (3단계 fallback 로직)
- 책임이 명확해야 추후 학사 정책 변경 시 수정 범위 식별 쉬움

## 영향

코드 구조:
- `NonMajorCategoryResolver` — `loadRequiredCourses` 의 courseKey override 패턴 (EC-010)
- `MajorCategoryResolver` — SINGLE/DOUBLE 분기, Subject 도메인에서 전필/전선 조회
- `BalanceRequiredResolver` — 학과별 룰 + 영역 제외 처리
- `DoubleCreditCriterionResolver` — 학과 페어 3단계 fallback (EC-003)
- `RequiredCourseResolver` — DEPRECATED 과목 치환 (EC-011)

향후:
- 새 정책 영역 추가 시 별도 Resolver 신설이 자연스러움
- SINGLE/DOUBLE 분기가 GraduationChecker.resolveCreditCriteria 와 중복 — 검사 경로와 통합 검토 가능 (data-usage-policy.md RU-002)

## 검수 후속 작업

- [x] 코드 머지 (PR #255)
- [x] knowledge 박제 (architecture.md 핵심 패턴 2, edge-cases.md EC-003, EC-010)
- [ ] 분리 패턴이 다른 영역 (검사 경로의 SINGLE/DOUBLE 분기) 에도 적용 가능한지 검토

## 후속 PR

- PR #254 (TSK-53 검사 구현): 검사 경로의 SINGLE/DOUBLE 분기 — `GraduationChecker`, `CategoryCreditCalculator` 에 별도 구현
