# graduation 코드 가이드 (타입 1 지식)

**여긴 지식 창고가 아니라 코드 가이드다.** 정책 지식(편람 값·학번별 규칙 = 타입 2)의 정본은
별도 private 레포 **graduation-wiki** (`cohort/`) — 여기 문서들은 *코드가 그 정책을 어떻게 집행하는지*만 담는다.
경계 판정은 한 질문: **"코드를 바꾸면 이 문서가 거짓이 되는가?"** → yes 면 여기, "편람이 바뀌면"이면 위키.

## 어디로 들어갈까 (작업별 라우팅)

| 하려는 일 | 먼저 볼 것 |
|---|---|
| 이 도메인이 처음이다 / 전체 구조 | [map/architecture.md](map/architecture.md) — 파이프라인 5단계 + 컴포넌트 매트릭스 + Cross-Reference Index |
| "이 값 어디서 오지?" (시트 vs 크롤러 vs 업로드) | [map/data-dependency-map.md](map/data-dependency-map.md) — **전공과목은 시트 아님(크롤러)** |
| 조회/검사 쿼리 추가·수정 | [conventions/resolver-layering.md](conventions/resolver-layering.md) — resolver 경유 의무 |
| 시트 값·적재 규칙이 이상하다 | [conventions/loading-rules.md](conventions/loading-rules.md) — 사람이 적재한 임의 규칙 (계약학과 보정·rename 등) |
| sync/적재 코드 | [contracts/data-loading-policy.md](contracts/data-loading-policy.md) — delete-all+save-all·검증 2단계 |
| 검사 파이프라인·트랜잭션·재계산 | [contracts/data-usage-policy.md](contracts/data-usage-policy.md) |
| 인증제 (영어·코딩·고전) | [contracts/certification-policy.md](contracts/certification-policy.md) — 값 정본은 wiki |
| 이상한 분기·매직값을 만났다 | [edge-cases.md](edge-cases.md) — **EC-001~024 등록부. 새 발견은 여기 박제** |
| enum·별칭이 헷갈린다 | [glossary.md](glossary.md) |
| "왜 이렇게 짰지?" (설계 결정의 근거) | [decisions/](decisions/) — ADR. **정착된 결정을 되돌리기 전에 반드시 확인** |

## 디렉토리 규약

| 경로 | 성격 | 갱신 시점 |
|---|---|---|
| `map/` | 지도 (구조·데이터 출처) | 컴포넌트/출처 구조 변경 시 |
| `contracts/` | 코드가 지키는 계약 (적재·검사·인증) | 해당 파이프라인 코드 변경 PR 에서 함께 |
| `conventions/` | 우리가 지키는 규칙 (계층·적재 컨벤션) | 규칙 자체가 바뀔 때만 |
| `edge-cases.md` `glossary.md` | 등록부 (append 위주) | 새 EC/enum 발견 즉시 |
| `decisions/` | ADR (불변 + 결과 추기만) | 새 결정 시 추가 |

## 부패 방지 규칙

1. 코드 변경 PR 이 이 문서들을 거짓으로 만들면 **같은 PR 에서** 갱신한다 (같은 레포에 두는 이유).
2. "해결됨" 갱신은 **참조하는 모든 문서에** — 한 파일만 고치면 문서 간 모순 (2026-07-12 EC-015 사례).
3. 실존하지 않는 EC 번호·decision 파일을 참조로 적지 않는다 (죽은 포인터 4건 정리한 이력 있음).
4. 위키 값을 여기 복사하지 않는다 — 태그 포인터로 인용 (`graduation-wiki@v2026-1 cohort/... §N`).

## 검증 이력

- 2026-07-12: 전 파일 3축 검증 (코드·wiki·시트 스냅샷) — 시트 13키/고전 4영역 10권/코딩 alt1·alt2/enum 3종/EC 포인터/트랜잭션·delete-all 패턴 일치 확인. 죽은 포인터 5건 정리 (EC-015 관련 3, decisions 미작성 2), stale 서술 5곳 현행화.
