#!/usr/bin/env bash
set -u

ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
FAILURES=0

fail() {
  printf '[FAIL] %s\n' "$1"
  FAILURES=$((FAILURES + 1))
}

pass() {
  printf '[ OK ] %s\n' "$1"
}

first_line() {
  sed -n '1p' "$1"
}

frontmatter_value() {
  local file="$1"
  local key="$2"
  awk -F': *' -v key="$key" '
    NR == 1 && $0 != "---" { exit }
    NR > 1 && $0 == "---" { exit }
    $1 == key { print $2; exit }
  ' "$file"
}

check_surface() {
  local surface="$1"
  local dir="$ROOT/$surface/skills"
  local names_file
  names_file="$(mktemp)"

  if [ ! -d "$dir" ]; then
    fail "$surface/skills directory is missing"
    rm -f "$names_file"
    return
  fi

  while IFS= read -r file; do
    local rel="${file#$ROOT/}"
    local name
    local description

    if [ "$(first_line "$file")" != "---" ]; then
      fail "$rel does not start with YAML frontmatter"
      continue
    fi

    name="$(frontmatter_value "$file" "name")"
    description="$(frontmatter_value "$file" "description")"

    if [ -z "$name" ]; then
      fail "$rel is missing frontmatter name"
    else
      printf '%s\n' "$name" >> "$names_file"
    fi

    if [ -z "$description" ]; then
      fail "$rel is missing frontmatter description"
    fi

    while IFS= read -r target; do
      [ -z "$target" ] && continue
      if (cd "$(dirname "$file")" && [ -f "$target" ]); then
        pass "$rel target exists: $target"
      else
        fail "$rel target missing: $target"
      fi
    done <<EOF
$(awk '/^@/ { print substr($0, 2) }' "$file")
EOF
  done <<EOF
$(find "$dir" -mindepth 2 -maxdepth 2 -name SKILL.md | sort)
EOF

  while IFS= read -r duplicate; do
    [ -z "$duplicate" ] && continue
    fail "$surface duplicate skill name: $duplicate"
  done <<EOF
$(sort "$names_file" | uniq -d)
EOF

  rm -f "$names_file"
}

check_canonical_bodies() {
  local dir="$ROOT/docs/agent-harness/skills"

  while IFS= read -r file; do
    local rel="${file#$ROOT/}"
    if [ "$(first_line "$file")" = "---" ]; then
      fail "$rel must not have skill frontmatter"
    fi
  done <<EOF
$(find "$dir" -mindepth 2 -maxdepth 2 -name SKILL.md | sort)
EOF
}

check_benchmark_references() {
  local refs
  refs="$(grep -Eoh 'docs/agent-harness/[^`[:space:])]+benchmark\.json' "$ROOT"/docs/agent-harness/skills/*/SKILL.md 2>/dev/null || true)"

  if [ -z "$refs" ]; then
    pass "no benchmark references found in canonical skill bodies"
    return
  fi

  while IFS= read -r ref; do
    [ -z "$ref" ] && continue
    if [ -f "$ROOT/$ref" ]; then
      pass "benchmark reference exists: $ref"
    else
      fail "benchmark reference missing: $ref"
    fi
  done <<EOF
$refs
EOF
}

check_skill_reference_links() {
  local dir="$ROOT/docs/agent-harness/skills"

  while IFS= read -r file; do
    local rel="${file#$ROOT/}"
    local refs
    refs="$(grep -Eoh 'references/[^`[:space:])]+\.md' "$file" 2>/dev/null || true)"

    if [ -z "$refs" ]; then
      continue
    fi

    while IFS= read -r ref; do
      [ -z "$ref" ] && continue
      if [ -f "$(dirname "$file")/$ref" ]; then
        pass "$rel reference exists: $ref"
      else
        fail "$rel reference missing: $ref"
      fi
    done <<EOF
$refs
EOF
  done <<EOF
$(find "$dir" -mindepth 2 -maxdepth 2 -name SKILL.md | sort)
EOF
}

check_surface ".agents"
check_surface ".claude"
check_canonical_bodies
check_skill_reference_links
check_benchmark_references

if [ "$FAILURES" -eq 0 ]; then
  printf 'Discovery static audit passed.\n'
  exit 0
fi

printf 'Discovery static audit failed with %s issue(s).\n' "$FAILURES"
exit 1
