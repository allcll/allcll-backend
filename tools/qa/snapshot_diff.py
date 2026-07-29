#!/usr/bin/env python3
"""
A3: 판정 스냅샷 diff — 수정 전/후 전수 비교 + 배치 지문 분류.

사용:
  python3 tools/qa/snapshot_diff.py qa-snapshots/before qa-snapshots/after \
      --expect-students 22010137,23012154 \
      --expect-cohorts 2022,2023 \
      --expect-categories BALANCE_REQUIRED

분류:
  ✅ 기대 변경   — 변경된 사용자가 배치 지문(--expect-*)에 부합
  ⚠️ 예상 밖 변경 — 지문 밖 사용자/카테고리가 바뀜 = 부작용 용의 (머지 차단 사유)
  ✅ 불변       — 나머지 전부 (무변경 증명)

머지 조건: --expect-students 전원이 "기대 변경"에 포함 && "예상 밖 변경" 0건.
"""
import argparse, json, os, sys
from pathlib import Path


def load_dir(d):
    out = {}
    for f in Path(d).glob("*.json"):
        with open(f, encoding="utf-8") as fp:
            out[f.stem] = json.load(fp)
    return out


def flatten(obj, prefix=""):
    """dict → {dotted.path: value} (리스트는 값으로 취급)"""
    flat = {}
    if isinstance(obj, dict):
        for k, v in obj.items():
            if isinstance(v, dict):
                flat.update(flatten(v, f"{prefix}{k}."))
            else:
                flat[f"{prefix}{k}"] = v
    return flat


def diff_one(before, after):
    fb, fa = flatten(before), flatten(after)
    changes = []
    for k in sorted(set(fb) | set(fa)):
        b, a = fb.get(k), fa.get(k)
        if b != a:
            changes.append((k, b, a))
    return changes


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("before")
    ap.add_argument("after")
    ap.add_argument("--expect-students", default="", help="신고 학번 (반드시 변경돼야 함)")
    ap.add_argument("--expect-cohorts", default="", help="변경 허용 입학년도 (예: 2022,2023)")
    ap.add_argument("--expect-categories", default="", help="변경 허용 카테고리 (예: BALANCE_REQUIRED)")
    ap.add_argument("-v", "--verbose", action="store_true")
    args = ap.parse_args()

    before, after = load_dir(args.before), load_dir(args.after)
    exp_students = set(filter(None, args.expect_students.split(",")))
    exp_cohorts = set(filter(None, args.expect_cohorts.split(",")))
    exp_cats = set(filter(None, args.expect_categories.split(",")))

    only_b = sorted(set(before) - set(after))
    only_a = sorted(set(after) - set(before))
    expected, unexpected, unchanged = [], [], []

    for sid in sorted(set(before) & set(after)):
        changes = diff_one(before[sid], after[sid])
        if not changes:
            unchanged.append(sid)
            continue
        cohort = str(before[sid].get("admissionYear", ""))
        cohort_ok = not exp_cohorts or cohort in exp_cohorts
        cats_ok = not exp_cats or all(
            any(c in path for c in exp_cats) or not path.startswith("result.categories.")
            for path, _, _ in changes
        )
        # 카테고리 외 파생 필드(총학점·graduatable 등)는 지문 카테고리 변경의 정당한 파급으로 허용
        if cohort_ok and cats_ok:
            expected.append((sid, changes))
        else:
            unexpected.append((sid, changes))

    print(f"== snapshot diff: {args.before} → {args.after} ==")
    print(f"대상 {len(set(before) & set(after))}명 | 변경 {len(expected) + len(unexpected)} | 불변 {len(unchanged)}")
    if only_b or only_a:
        print(f"!! 스냅샷 불일치: before-only {only_b[:5]} / after-only {only_a[:5]}")

    print(f"\n✅ 기대 변경: {len(expected)}명")
    for sid, changes in expected:
        mark = " ← 신고자" if sid in exp_students else ""
        print(f"  {sid}{mark}")
        if args.verbose or sid in exp_students:
            for p, b, a in changes[:8]:
                print(f"      {p}: {b} → {a}")

    print(f"\n⚠️ 예상 밖 변경: {len(unexpected)}명")
    for sid, changes in unexpected:
        print(f"  {sid} (cohort={before[sid].get('admissionYear')})")
        for p, b, a in changes[:8]:
            print(f"      {p}: {b} → {a}")

    missing = [s for s in exp_students if s not in {sid for sid, _ in expected}]
    verdict_ok = not unexpected and not missing
    print()
    if missing:
        print(f"❌ 신고자 미변경 (수정이 안 먹음): {missing}")
    print("== 판정:", "PASS — 머지 가능" if verdict_ok else "FAIL — 머지 차단", "==")
    sys.exit(0 if verdict_ok else 1)


if __name__ == "__main__":
    main()
