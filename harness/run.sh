#!/usr/bin/env bash
# Executa uma run do experimento: prepara workspace → roda o agente → roda os gates → registra métricas.
#
#   harness/run.sh <arch> <task> <n>            ex.: harness/run.sh layered T0 1
#   harness/run.sh <arch> <task> <n> --no-agent pula o agente (valida gates com o conteúdo do workspace/baseline)
#   FORCE=1 harness/run.sh ...                  ignora o gate de orçamento
#   IMPL_DIR=dir harness/run.sh <arch> <task> <n> --no-agent
#                                               copia uma implementação pronta no lugar do agente (validação do harness)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
source "$ROOT/harness/config.env"
export MODEL EFFORT BUDGET_WEEKLY_USD RUN_MAX_USD PLAN_5H_MAX PLAN_7D_MAX

ARCH="${1:?arch: layered|layered-bc|hexagonal|clean}"
TASK="${2:?task: T0..T5}"
N="${3:?número da run}"
NO_AGENT="${4:-}"

case "$ARCH" in layered|layered-bc|hexagonal|clean) ;; *) echo "arch inválida: $ARCH"; exit 2 ;; esac
[[ -f "$ROOT/tasks/$TASK.md" ]] || { echo "tarefa inexistente: $TASK"; exit 2; }

RUN="$ROOT/runs/$ARCH/$TASK/run-$N"
WS="$RUN/workspace"
[[ -e "$RUN" ]] && { echo "run já existe: $RUN"; exit 2; }

log() { echo "[$(date +%H:%M:%S)] $*"; }

# ---------------------------------------------------------------- 1. orçamento
if [[ -z "${FORCE:-}" && "$NO_AGENT" != "--no-agent" ]]; then
  python3 "$ROOT/harness/evalctl.py" budget || { log "Sem orçamento para rodar agora."; exit 3; }
fi

# ---------------------------------------------------------------- 2. workspace
mkdir -p "$RUN/gates"
if [[ "$TASK" == "T0" ]]; then
  cp -r "$ROOT/template" "$WS"
  rm -rf "$WS/target"
  cp "$ROOT/spec/SPEC.md" "$WS/SPEC.md"
  cat "$ROOT/guides/_common.md" "$ROOT/guides/$ARCH.md" > "$WS/CLAUDE.md"
else
  BASE="$ROOT/baselines/$ARCH"
  [[ -d "$BASE" ]] || { echo "baseline ausente: $BASE (promova uma run T0 com harness/promote.sh)"; exit 2; }
  cp -r "$BASE" "$WS"
fi
git -C "$WS" init -q -b main
git -C "$WS" add -A
git -C "$WS" -c user.name=eval -c user.email=eval@local commit -qm baseline
if [[ "$TASK" == "T4" ]]; then
  PATCH="$ROOT/tasks/T4-bugs/$ARCH.patch"
  [[ -f "$PATCH" ]] || { echo "patch do bug ausente: $PATCH"; exit 2; }
  git -C "$WS" apply "$PATCH"
  git -C "$WS" -c user.name=eval -c user.email=eval@local commit -qam "bug plantado"
fi
git -C "$WS" tag baseline

# ---------------------------------------------------------------- 3. agente
AGENT_EXIT=""
if [[ "$NO_AGENT" != "--no-agent" ]]; then
  log "Agente: $ARCH $TASK run-$N ($MODEL, effort $EFFORT, teto US\$ $RUN_MAX_USD)"
  set +e
  (cd "$WS" && timeout "$AGENT_TIMEOUT" claude -p "$(cat "$ROOT/tasks/$TASK.md")" \
      --model "$MODEL" --effort "$EFFORT" \
      --setting-sources project --strict-mcp-config --disable-slash-commands \
      --permission-mode bypassPermissions \
      --max-budget-usd "$RUN_MAX_USD" \
      --output-format stream-json --verbose) > "$RUN/agent.jsonl" 2> "$RUN/agent.stderr"
  AGENT_EXIT=$?
  set -e
  log "Agente terminou (exit $AGENT_EXIT)"
elif [[ -n "${IMPL_DIR:-}" ]]; then
  cp -r "$IMPL_DIR"/. "$WS"/
fi
git -C "$WS" add -A
git -C "$WS" -c user.name=eval -c user.email=eval@local commit -qm agent --allow-empty

# ---------------------------------------------------------------- 4. gates
log "Gate: mvn verify (testes do agente + cobertura)"
set +e
(cd "$WS" && mvn -B verify) > "$RUN/gates/verify.log" 2>&1
echo $? > "$RUN/gates/verify.exit"
JAR=$(ls "$WS"/target/loja-*.jar 2>/dev/null | grep -v original | head -1)
if [[ -z "$JAR" ]]; then
  (cd "$WS" && mvn -B -q package -Dmaven.test.skip=true -Djacoco.skip=true) >> "$RUN/gates/verify.log" 2>&1
  JAR=$(ls "$WS"/target/loja-*.jar 2>/dev/null | grep -v original | head -1)
fi

TAGS="base"; FRETE=1
case "$TASK" in T1) TAGS="base,t1" ;; T2) FRETE=2 ;; T3) TAGS="base,t3" ;; esac

if [[ -n "$JAR" ]]; then
  log "Gate: aceitação ($TAGS, frete v$FRETE)"
  java -jar "$JAR" --server.port="$APP_PORT" --frete.api.url="http://localhost:$WIREMOCK_PORT" \
       > "$RUN/gates/app.log" 2>&1 &
  APP_PID=$!
  for _ in $(seq 60); do curl -s -o /dev/null "http://localhost:$APP_PORT/produtos" && break; sleep 1; done
  rm -rf "$ROOT/acceptance/target/surefire-reports"
  mvn -B -f "$ROOT/acceptance/pom.xml" test -Dgroups="$TAGS" \
      -Dbase.url="http://localhost:$APP_PORT" -Dwiremock.port="$WIREMOCK_PORT" -Dfrete.versao="$FRETE" \
      > "$RUN/gates/acceptance.log" 2>&1
  cp -r "$ROOT/acceptance/target/surefire-reports" "$RUN/gates/acceptance-reports" 2>/dev/null
  kill "$APP_PID" 2>/dev/null; wait "$APP_PID" 2>/dev/null

  log "Gate: ArchUnit"
  [[ -f "$ROOT/archcheck/target/archcheck.jar" ]] || mvn -q -B -f "$ROOT/archcheck/pom.xml" package > /dev/null
  java -jar "$ROOT/archcheck/target/archcheck.jar" "$ARCH" "$WS/target/classes" > "$RUN/gates/archcheck.json"

  if [[ "$RUN_PIT" == "1" && "$(cat "$RUN/gates/verify.exit")" == "0" ]]; then
    log "Gate: mutation testing (PIT)"
    (cd "$WS" && mvn -B org.pitest:pitest-maven:mutationCoverage) > "$RUN/gates/pit.log" 2>&1
  fi
else
  log "Sem jar: código não compila — gates de aceitação/arquitetura ficam zerados"
fi
set -e

# ---------------------------------------------------------------- 5. métricas
log "Métricas"
python3 "$ROOT/harness/evalctl.py" collect --run "$RUN" --arch "$ARCH" --task "$TASK" --n "$N" --agent-exit "$AGENT_EXIT"
