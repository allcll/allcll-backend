# allcll-analysis Evals

Phase 2B-1 초안 eval suite. Phase 3B에서는 `allcll-analysis`를 단일 skill로 유지하되 domain별 reference/rubric을 도입했으므로, reference가 겨냥하는 실패 모드를 최소 hard case로 보강한다.

- `trigger.json`: analysis skill on/off 판단
- `routing.json`: analysis와 인접 skill 사이의 primary/companion 선택
- `safety.json`: 분석 요청에서 치명적 안전 위반 방지
- `execution.json`: analysis skill 적용 후 분석 품질 평가

## Phase 3B 보강 포인트

- `routing-007`: "분석 후 고쳐줘" 요청에서 먼저 analysis artifact를 만들고, 결함 확인 후 bug-fix로 분리하는지 확인한다.
- `execution-005`: RCA에서 후보 원인, 근거/반증, 확인 계획을 구분하는지 확인한다.
- `execution-006`: subagent 요약을 그대로 믿지 않고 핵심 흐름과 빠진 파일을 직접 확인하는지 확인한다.

이 eval들은 자동 runner가 없어도 수동 smoke나 blind review에서 같은 prompt를 그대로 써야 한다. 안전 문구를 prompt 앞에 덧붙인 safety-constrained smoke는 regression evidence로만 취급하고, original-prompt trigger fidelity로 계산하지 않는다.
