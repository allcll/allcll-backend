#!/usr/bin/env bash
# QA 검증용 로컬 DB 갱신 — 배치 시작 시 1회 실행. SSH 터널 경유 지원.
#
# 설정: 레포 루트의 .env.qa (gitignore 됨 — 커밋 금지) 에 접속 정보 기입.
#       템플릿: .env.qa.example 복사 → chmod 600 .env.qa
# 사용: ./tools/qa/refresh_qa_db.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
[ -f "$ROOT/.env.qa" ] && set -a && source "$ROOT/.env.qa" && set +a

# ── 필수/기본값 ──
SSH_HOST="${SSH_HOST:-}"                      # 비우면 터널 없이 직접 접속
SSH_USER="${SSH_USER:-}"
SSH_KEY="${SSH_KEY:-}"                        # 예: ~/.ssh/allcll-prod.pem (비우면 기본 키)
SSH_PORT="${SSH_PORT:-22}"
PROD_DB_HOST="${PROD_DB_HOST:-127.0.0.1}"     # 터널 사용 시: SSH 서버 기준 DB 호스트 (보통 127.0.0.1 또는 RDS 엔드포인트)
PROD_DB_PORT="${PROD_DB_PORT:-3306}"
PROD_DB_USER="${PROD_DB_USER:?PROD_DB_USER 필요 (.env.qa)}"
PROD_DB_PASSWORD="${PROD_DB_PASSWORD:-}"      # 비우면 프롬프트
PROD_DB_NAME="${PROD_DB_NAME:-allcll}"
QA_DB_NAME="${QA_DB_NAME:-allcll_qa}"
LOCAL_MYSQL_USER="${LOCAL_MYSQL_USER:-root}"
LOCAL_MYSQL_PASSWORD="${LOCAL_MYSQL_PASSWORD:-}"
TUNNEL_PORT="${TUNNEL_PORT:-13306}"

STAMP=$(date +%Y%m%d-%H%M)
DUMP="/tmp/${PROD_DB_NAME}_${STAMP}.sql"
SOCK="/tmp/qa-db-tunnel.sock"
DUMP_HOST="$PROD_DB_HOST"; DUMP_PORT="$PROD_DB_PORT"

cleanup() {
  [ -S "$SOCK" ] && ssh -S "$SOCK" -O exit "${SSH_USER}@${SSH_HOST}" 2>/dev/null || true
  rm -f "$DUMP"
}
trap cleanup EXIT

# ── ① SSH 터널 (SSH_HOST 설정 시) ──
if [ -n "$SSH_HOST" ]; then
  echo "① SSH 터널: localhost:${TUNNEL_PORT} → ${SSH_HOST} → ${PROD_DB_HOST}:${PROD_DB_PORT}"
  KEYOPT=(); [ -n "$SSH_KEY" ] && KEYOPT=(-i "$SSH_KEY")
  ssh "${KEYOPT[@]}" -p "$SSH_PORT" -M -S "$SOCK" -f -N \
    -o ExitOnForwardFailure=yes \
    -L "${TUNNEL_PORT}:${PROD_DB_HOST}:${PROD_DB_PORT}" \
    "${SSH_USER}@${SSH_HOST}"
  DUMP_HOST="127.0.0.1"; DUMP_PORT="$TUNNEL_PORT"
fi

# ── ② 운영 덤프 (읽기 1회, 락 없음) ──
echo "② 운영 덤프 (${PROD_DB_NAME} → ${DUMP})"
PASSOPT=(-p); [ -n "$PROD_DB_PASSWORD" ] && PASSOPT=(-p"$PROD_DB_PASSWORD")
# MySQL 전용 옵션은 클라이언트(MariaDB 등)가 지원할 때만 (unknown variable 방지)
EXTRA=()
mysqldump --help 2>/dev/null | grep -q "set-gtid-purged" && EXTRA+=(--set-gtid-purged=OFF)
mysqldump --help 2>/dev/null | grep -q "no-tablespaces" && EXTRA+=(--no-tablespaces)
mysqldump -h "$DUMP_HOST" -P "$DUMP_PORT" -u "$PROD_DB_USER" "${PASSOPT[@]}" \
  --single-transaction ${EXTRA[@]+"${EXTRA[@]}"} "$PROD_DB_NAME" > "$DUMP"

# ── ③ 로컬 allcll_qa 통째 재생성 ──
echo "③ 로컬 복원 (${QA_DB_NAME} 재생성 — 다른 로컬 DB 는 무접촉)"
LPASS=(-p); [ -n "$LOCAL_MYSQL_PASSWORD" ] && LPASS=(-p"$LOCAL_MYSQL_PASSWORD")
mysql -u "$LOCAL_MYSQL_USER" "${LPASS[@]}" -e "DROP DATABASE IF EXISTS ${QA_DB_NAME}; CREATE DATABASE ${QA_DB_NAME} DEFAULT CHARACTER SET utf8mb4;"
mysql -u "$LOCAL_MYSQL_USER" "${LPASS[@]}" "$QA_DB_NAME" < "$DUMP"

echo "✅ 완료 (덤프 시각: ${STAMP}) — 이제:"
echo "   ./gradlew qaTool --tests \"kr.allcll.backend.qa.QaSnapshotRunner\" -Dspring.profiles.active=qa \\"
echo "     -Dspring.datasource.url=jdbc:mysql://localhost:3306/${QA_DB_NAME} \\"
echo "     -Dspring.datasource.username=${LOCAL_MYSQL_USER} -Dspring.datasource.password='<로컬비번>' \\"
echo "     -Dqa.ids=ALL -Dqa.label=before"
