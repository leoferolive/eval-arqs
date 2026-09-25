#!/usr/bin/env bash
# Promove o código de uma run T0 aprovada a baseline da arquitetura (ponto de partida de T1..T5).
#   harness/promote.sh <arch> <n>
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ARCH="${1:?arch}"; N="${2:?run}"
SRC="$ROOT/runs/$ARCH/T0/run-$N/workspace"
DST="$ROOT/baselines/$ARCH"
[[ -d "$SRC" ]] || { echo "run inexistente: $SRC"; exit 2; }
grep -q '"success": 1' "$ROOT/runs/$ARCH/T0/run-$N/metrics.json" || { echo "run não passou em todos os gates"; exit 2; }
rm -rf "$DST"; mkdir -p "$DST"
git -C "$SRC" archive HEAD | tar -x -C "$DST"
echo "baseline de $ARCH ← T0/run-$N"
