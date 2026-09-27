#!/usr/bin/env bash
# Executa a fila harness/campaign.txt respeitando orçamento em USD e limites do plano.
# Eventos (uma linha cada) vão para runs/campaign.log com prefixos:
#   OK | PAUSA | RETOMADA | ALERTA | PRECISA_PATCH | ERRO | FIM
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
set -a; source "$ROOT/harness/config.env"; set +a
# CAMPAIGN=<nome> usa harness/campaign-<nome>.txt e runs/campaign-<nome>.done (ex.: fase C de repetições)
QUEUE="$ROOT/harness/campaign${CAMPAIGN:+-$CAMPAIGN}.txt"
LOG="$ROOT/runs/campaign.log"; DONE="$ROOT/runs/campaign${CAMPAIGN:+-$CAMPAIGN}.done"
mkdir -p "$ROOT/runs"; touch "$DONE"
ev() { echo "$(date '+%d/%m %H:%M') $*" >> "$LOG"; }
commit() { git -C "$ROOT" add results baselines >/dev/null 2>&1
           git -C "$ROOT" -c user.name=eval -c user.email=eval@local commit -qm "data: $1" >/dev/null 2>&1 || true; }

wait_gate() {  # $1 = task
  while true; do
    out=$(python3 "$ROOT/harness/evalctl.py" gate --task "$1"); rc=$?
    if (( rc == 0 )); then echo "$out"; return 0; fi
    if (( rc == 4 )); then ev "ERRO sonda do plano falhou: $out"; exit 1; fi
    until_ts=$(python3 -c "import json,sys; print(json.loads(sys.argv[1])['retomar_em'])" "$out")
    ev "PAUSA limite do plano $(python3 -c "import json,sys;d=json.loads(sys.argv[1]);print(f\"{','.join(d['bloqueio'])} — 5h={d['5h']:.0%} 7d={d['7d']:.0%} (próxima run consome ~{d['esperado_5h']:.0%}/{d['esperado_7d']:.0%})\")" "$out") — retomo em $(date -d @"$until_ts" '+%d/%m %H:%M')"
    sleep $(( until_ts - $(date +%s) > 60 ? until_ts - $(date +%s) : 60 ))
    ev "RETOMADA após reset"
  done
}

guard() {  # mata o agente se a janela de 5h passar de 97% durante a run (proteção contra limite duro)
  local jsonl="$1"
  while sleep 60; do
    [[ -f "$jsonl" ]] || continue
    h=$(python3 - "$jsonl" <<'PY'
import json,sys
h=0
for l in open(sys.argv[1],errors="replace"):
    try: e=json.loads(l)
    except: continue
    if e.get("type")=="rate_limit_event":
        h=(e["rate_limit_info"].get("unifiedWindows") or {}).get("five_hour",{}).get("utilization",h)
print(h)
PY
)
    if python3 -c "import sys; sys.exit(0 if float('$h')>=0.97 else 1)"; then
      ev "ALERTA janela 5h em $h durante a run — agente interrompido"; pkill -n -f "claude -p" ; return
    fi
  done
}

ev "INÍCIO campanha ${CAMPAIGN:-principal} (limites 5h=${PLAN_5H_MAX} 7d=${PLAN_7D_MAX}, US\$ ${BUDGET_WEEKLY_USD}/semana)"
while read -r ARCH TASK REP <&3; do
  KEY="$ARCH $TASK${REP:+ $REP}"
  grep -qx "$KEY" "$DONE" && continue

  if [[ "$TASK" == "T4" && ! -f "$ROOT/tasks/T4-bugs/$ARCH.patch" ]]; then
    ev "PRECISA_PATCH tasks/T4-bugs/$ARCH.patch (aguardando o arquivo)"
    until [[ -f "$ROOT/tasks/T4-bugs/$ARCH.patch" ]]; do sleep 60; done
  fi
  if [[ "$TASK" != "T0" && ! -d "$ROOT/baselines/$ARCH" ]]; then
    ev "ERRO baseline ausente para $ARCH — não dá para rodar $TASK"; exit 1
  fi

  attempts=0
  while true; do
    attempts=$((attempts+1))
    BUDGET_USD_ONLY=1 python3 "$ROOT/harness/evalctl.py" budget > /dev/null || { ev "PAUSA orçamento semanal em USD esgotado — aguardando 1h"; sleep 3600; continue; }
    g=$(wait_gate "$TASK" < /dev/null) || exit 1
    N=1; while [[ -e "$ROOT/runs/$ARCH/$TASK/run-$N" ]]; do N=$((N+1)); done
    RUN="$ROOT/runs/$ARCH/$TASK/run-$N"
    guard "$RUN/agent.jsonl" < /dev/null & GUARD=$!
    FORCE=1 "$ROOT/harness/run.sh" "$ARCH" "$TASK" "$N" > "$ROOT/runs/campaign-$ARCH-$TASK-$N.out" 2>&1 < /dev/null; rc=$?
    kill "$GUARD" 2>/dev/null; wait "$GUARD" 2>/dev/null

    if (( rc != 0 )) || [[ ! -f "$RUN/metrics.json" ]]; then
      ev "ERRO run.sh falhou ($ARCH $TASK run-$N, exit $rc) — ver runs/campaign-$ARCH-$TASK-$N.out"; exit 1
    fi
    read -r status cost ok h s off <<<"$(python3 -c "import json;d=json.load(open('$RUN/metrics.json'));print(d.get('agent_status'),d.get('cost_usd'),d.get('success'),d.get('plan_5h_after'),d.get('plan_7d_after'),d.get('off_workspace',0))")"
    if [[ "$off" != "0" ]]; then
      commit "$ARCH $TASK run-$N (agente saiu do workspace)"
      ev "ERRO agente criou worktree/branch fora do workspace ($ARCH $TASK run-$N, US\$ $cost) — run inválida"; exit 1
    fi
    if [[ "$status" != "success" ]]; then
      commit "$ARCH $TASK run-$N (agente: $status)"
      ev "ERRO agente terminou com status '$status' ($ARCH $TASK run-$N, US\$ $cost)"; exit 1
    fi
    ev "OK $ARCH $TASK run-$N — US\$ $cost, success=$ok, plano 5h=$h 7d=$s"

    if [[ "$TASK" == "T0" && -z "${NO_PROMOTE:-}" ]]; then
      if [[ "$ok" == "1" ]]; then
        "$ROOT/harness/promote.sh" "$ARCH" "$N" > /dev/null && ev "BASELINE $ARCH ← T0 run-$N"
      elif (( attempts < 2 )); then
        commit "$ARCH T0 run-$N (falhou gates)"; ev "RETENTATIVA $ARCH T0 falhou nos gates; tentando de novo"; continue
      else
        commit "$ARCH T0 run-$N (falhou gates)"; ev "ERRO $ARCH T0 falhou nos gates 2x — sem baseline"; exit 1
      fi
    fi
    commit "$ARCH $TASK run-$N"
    echo "$KEY" >> "$DONE"
    break
  done
done 3< <(grep -vE '^\s*(#|$)' "$QUEUE")
ev "FIM fila concluída — $(python3 "$ROOT/harness/evalctl.py" report | head -1)"
