# 데이터 의존 지도 (각 데이터가 어디서 오나)

졸업요건 검사가 쓰는 데이터의 **출처**. "이 값 어디서 오지?"를 코드 짜기 전에 확인. 특히 **전공 과목 목록이 시트가 아니라 크롤러**라는 점이 가장 자주 헷갈리는 함정.

마지막 갱신: 2026-06-23 (코드 기준)

---

## 한눈에

```
수강편람 PDF ─(사람 적재)→ 구글시트 13탭 ─(sync)→ DB ─→ resolver/checker
크롤러 ─→ Subject (전공 과목 목록) ──────────────────┘
사용자 업로드 성적표(SJPT 엑셀) ─→ GradeExcelParser ─→ CompletedCourse
외부 크롤링(대휴칼·TOSC) ─→ 인증 실적 ─→ CertResult
```

## 출처별 상세

| 데이터 | 출처 | 적재/조회 코드 | 주의 |
|---|---|---|---|
| 이수구분별 요구학점 | 구글시트 `credit_criteria`·`double_credit_criteria` | `CreditCriterionRepository`, `DoubleCreditCriterionResolver` | 사람 적재. ALL_DEPT·페어 fallback |
| 균형교양 규칙·영역·제외 | 시트 `balance_required_*` (3탭) | `BalanceRequiredRuleRepository` 등 | 학과 무관 단일 ALL 행 |
| 인증 기준 | 시트 `*_cert_criteria` + `graduation_cert_rules` | `*CertCriterionRepository` | 학번·target_type별 |
| 학과 메타(target_type·dept_group) | 시트 `graduation_department_info` | `GraduationDepartmentInfoRepository` | 인증 대상 분류 키 |
| **전공필수/전공선택 과목 목록** | **Subject 도메인 (크롤러)** | `SubjectRepository.findByDeptCdAndCuriTypeCdNm(deptCd, "전필"/"전선")` (in `MajorCategoryResolver#loadMajorSubjects`) | **시트 아님!** 가장 흔한 오해 |
| 교양 지정과목·학문기초 과목 | 시트 `required_courses` | `RequiredCourseRepository`, `RequiredCourseResolver` | 전공 과목과 출처 다름 |
| 동일/대체 과목 | 시트 `course_equivalences` (group_code) | `CourseEquivalenceRepository` | `AcademicBasicPolicy`·`RequiredCourseResolver` 사용 |
| 사용자 이수 과목(성적) | **사용자 업로드 성적표 엑셀 (SJPT)** | `GradeExcelParser` → `CompletedCourse` | 학교 엑셀 형식 종속. 파서 변경 시 실제 샘플 필요 |
| 영어/코딩/고전 인증 실적 | **외부 크롤링** (대휴칼·TOSC) | `check/cert/GraduationEnglishCertFetcher`·`GraduationCodingCertFetcher`·`GraduationClassicsCertFetcher` | 휘발성. 로그인 시 판정(EC-015) |

## 함정 정리 (신규 피쳐 시)

1. **전공 과목 = 크롤러(Subject), 교양/학문기초 과목 = 시트(required_courses).** 둘을 헷갈리면 조회가 빈다.
2. **성적표 형식은 학교(SJPT) 종속** — 파서 수정 전 실제 엑셀 샘플 확보.
3. **인증 실적은 크롤링(휘발성)** — 안정 신호(대체과목) 우선 (EC-015).
4. 시트 데이터는 사람 적재 → **적재 규칙(../conventions/loading-rules.md) 준수 필요**.

---

## 검수 상태

- [x] 시트 13탭 ↔ repository 매핑: 코드 일치
- [x] 전공 과목 = Subject 크롤러: `MajorCategoryResolver#loadMajorSubjects` 확인
- [x] 인증 fetcher 3종: `check/cert/*Fetcher` 확인
- [ ] 성적표(SJPT) 엑셀 컬럼 상세: `GradeExcelParser` 미정독(추후)
