# 졸업요건 QA 처리 가이드 (graduation-qa)

사용자 후기(QA 신고)를 **위키 정본 기반**으로 해결하는 워크플로. 코드 작업 규칙은
`graduation-backend` skill 이 담당하고, 이 skill 은 그 앞뒤의 **판정·검증·기록 루프**를 강제한다.

핵심 전제: **후기 N건 ≠ 버그 N건.** 신고는 원인(root cause) 단위 배치로 묶어 처리하며,
기대 동작은 코드에서 역산하지 않고 위키(graduation-llm-wiki)의 인용 달린 정본에서 확정한다.

## 발동 조건

- 사용자 후기·QA 신고·트리아지 항목을 언급하며 수정을 요청할 때 ("이 신고 해결하자", "균형교양 버그 잡자")
- 특정 학번의 졸업요건 오판 재현·분석 요청

## Step 0: 전제 확인 (없으면 중단하고 요청)

1. **위키 접근**: 세션에 `allcll-graduation-wiki` 폴더가 열려 있는가 (`--add-dir ~/allcll-graduation-wiki`).
   없으면 사용자에게 추가 요청. 위키 없이 코드만 보고 정책을 추정하는 것 금지.
2. **백로그**: `graduation-wiki/reports/qa/<시기>/triage.md` (신고→원인별 판정표) 존재 확인.

## Step 1: 신고 → 배치 매핑

1. 트리아지 표에서 해당 신고의 배치·판정을 찾는다. **같은 뿌리의 다른 신고를 모두 모아** 한 번에 처리.
2. 표에 없는 신규 신고면: 학번·증상으로 분류해 트리아지에 행 추가부터 (append — 기존 행 소급 수정 금지).

## Step 2: 기대 동작 확정 (위키 = 정답지)

1. 신고 학번 → 위키 `INDEX.md` era 사전 → `cohort/<학번>/<카테고리>.md` 에서 기대 동작 + **편람 페이지 인용** 확보.
2. 위키에 없으면 `catalog/` PDF 직접 확인 (표는 pdfplumber 병합셀 기준 — 위키 CLAUDE.md 규칙).
3. 그래도 없으면 **"확인 불가"로 중단** → 트리아지 CONTRA 태그 + `확인 필요` 등록, 학사팀 문의 제안. 추측으로 수정 금지.
4. 신고가 편람을 직접 인용하는데 우리 catalog 와 다르면 → **개정판 편람 가능성**. `grad-wiki-ingest` 선행 (실사례: 2026-1 p.57 '라' 항목).

## Step 3: 판정 분기

| 판정 | 조건 | 처리 |
|---|---|---|
| **데이터** | 위키 정본 ≠ 시트 적재값 | 위키 `tools/*_drift_check.py` 실행 (**`sheets/` 최신 스냅샷으로 즉시 — 새 export 요구하지 않음**, 스냅샷 날짜 고지. 스냅샷 없거나 시트가 최근 수정됐을 때만 export 요청) → diff 리포트까지만. **시트 수정은 사람** |
| **코드** | 시트는 맞는데 동작이 틀림 | `graduation-backend` skill 게이트(분류·knowledge 점검·resolver 규칙) 준수 + `bug-fix` skill 절차로 수정 |
| **모순** | 신고 ↔ 현행 편람 배치 | 코드 손대지 않음. 트리아지 CONTRA 태그로 등록 + 학사팀 확인 요청 |
| **정책 해석 변경** | 규칙 자체를 바꿔야 함 | **AI 단독 금지** (graduation-backend 분류 A) — 사람 결정 + ADR |

균형교양·학점류 신고는 데이터 버그 비율이 높다 — **코드를 열기 전에 drift 부터.**

## Step 4: 검증 (의무) — 스냅샷 diff 게이팅

fixture 수작업 대신 **실 사용자 DB 리플레이 + 전수 diff** 로 검증한다. 도구는 `src/test/java/kr/allcll/backend/qa/` + `tools/qa/`.

**DB 준비 (1회)**: 운영 DB 를 mysqldump → 로컬 복원 (`allcll_qa` 등). **운영 DB 직결 절대 금지**
(기본 테스트 설정이 create-drop). 모든 qaTool 명령에 `-Dspring.profiles.active=qa` 필수
(+ 필요 시 `-Dspring.datasource.url/username/password` 또는 `QA_DB_URL` 등 env) — `src/test/resources/application-qa.yml` 참조.
러너에 H2 가드가 있어 프로필 없이 돌리면 즉시 실패한다.

1. **수정 전 스냅샷** (코드 고치기 전에!):
   ```
   ./gradlew qaTool --tests "kr.allcll.backend.qa.QaSnapshotRunner" -Dspring.profiles.active=qa -Dqa.ids=ALL -Dqa.label=before
   ```
2. **케이스 export + 기대값 작성**: 신고 학번들을
   ```
   ./gradlew qaTool --tests "kr.allcll.backend.qa.QaCaseExportRunner" -Dspring.profiles.active=qa -Dqa.ids=<학번들> -Dqa.batch=<배치ID>
   ```
   로 `qa-cases/*.json` 생성 후, 각 파일의 `expect` 를 **wiki 정본 근거로** 채운다
   (예: `{"path":"result.categories.PRIMARY:BALANCE_REQUIRED.earnedAreasCnt","value":2,"근거":"cohort/2022/balance.md §1"}`).
   신고 학번 목록은 wiki `reports/qa/<시기>/cases.csv` 에서.
3. **수정 → 리플레이**: `./gradlew qaTool --tests "kr.allcll.backend.qa.QaReplayTest" -Dspring.profiles.active=qa -Dqa.batch=<배치ID>` 전부 PASS.
4. **전수 diff** (부작용 검사):
   ```
   ./gradlew qaTool --tests "kr.allcll.backend.qa.QaSnapshotRunner" -Dspring.profiles.active=qa -Dqa.ids=ALL -Dqa.label=after
   python3 tools/qa/snapshot_diff.py qa-snapshots/before qa-snapshots/after \
     --expect-students <신고학번들> --expect-cohorts <배치 era> --expect-categories <카테고리>
   ```
   **머지 조건 = 신고자 전원 기대 변경 + 예상 밖 변경 0명.** diff 요약을 PR 에 첨부.

주의: `qa-cases/`·`qa-snapshots/` 는 성적 데이터(개인정보) — 커밋 금지 (.gitignore 등록됨).
성적 미업로드 신고자만 예외적으로 합성 fixture (신고된 과목 조합) 작성.

## Step 5: PR

- `/create-pr` 사용. 본문 근거 라인에 **위키 포인터 필수**:
  ```
  - 근거: graduation-wiki@v2026-1 cohort/2022/balance.md §2 (수강편람 2026-1 p.52 §4)
  ```
- 데이터 판정 건은 PR 대신 drift 리포트 경로를 결과로 제시.

## Step 6: 기록 (루프 닫기)

- 위키 `reports/qa/<시기>/triage.md` 해당 행에 결과 append: 판정 확정 / PR 번호 / drift 리포트 경로 / 해결일.
- 새로 확인된 코드 분기·학번 보정은 `docs/agent-harness/knowledge/graduation/edge-cases.md` 박제 여부 확인.

## 안티패턴

1. 후기 목록을 통째로 읽고 즉흥 수정 — 반드시 배치 단위
2. 코드 동작을 보고 "정책이 이런가 보다" 역산
3. 시트를 에이전트가 직접 수정
4. 신고 학번 fixture 없이 머지
5. 위키에 근거 없는 값을 PR 에 박기 (확인 불가는 확인 불가라고)
