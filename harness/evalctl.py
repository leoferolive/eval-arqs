#!/usr/bin/env python3
"""Controle do experimento: orçamento, coleta de métricas e relatório.

Subcomandos:
  budget   verifica se há orçamento (USD semanal + limites do plano) para mais uma run
  collect  extrai métricas de uma run concluída e acrescenta uma linha em results/runs.csv
  report   resumo de gastos e resultados por arquitetura × tarefa
"""
import argparse
import csv
import json
import os
import re
import statistics
import subprocess
import sys
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from datetime import datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RESULTS = ROOT / "results" / "runs.csv"

COLUMNS = [
    "timestamp", "protocol", "arch", "task", "run", "model", "effort",
    "agent_exit", "agent_status", "cost_usd",
    "input_tokens", "output_tokens", "cache_write_tokens", "cache_read_tokens", "total_tokens",
    "num_turns", "tool_calls", "bash_calls", "mvn_runs", "files_read", "files_edited", "duration_s",
    "plan_5h_before", "plan_5h_after", "plan_7d_before", "plan_7d_after", "plan_5h_resets_at", "plan_7d_resets_at",
    "build_ok", "line_coverage", "accept_passed", "accept_total", "arch_violations", "mutation_score",
    "main_files", "main_loc", "test_files", "test_loc", "interfaces", "packages",
    "diff_files", "diff_added", "diff_removed", "gates_tampered", "off_workspace", "guide_bytes", "success",
]


# ---------------------------------------------------------------- ledger

def read_ledger():
    if not RESULTS.exists():
        return []
    with RESULTS.open(newline="") as f:
        return list(csv.DictReader(f))


def append_ledger(row):
    RESULTS.parent.mkdir(parents=True, exist_ok=True)
    new = not RESULTS.exists()
    with RESULTS.open("a", newline="") as f:
        w = csv.DictWriter(f, fieldnames=COLUMNS)
        if new:
            w.writeheader()
        w.writerow({k: row.get(k, "") for k in COLUMNS})


def fnum(v, default=0.0):
    try:
        return float(v)
    except (TypeError, ValueError):
        return default


# ---------------------------------------------------------------- budget

def cmd_budget(args):
    weekly = float(os.environ.get("BUDGET_WEEKLY_USD", "90"))
    run_max = float(os.environ.get("RUN_MAX_USD", "15"))
    max_5h = float(os.environ.get("PLAN_5H_MAX", "0.85"))
    max_7d = float(os.environ.get("PLAN_7D_MAX", "0.95"))

    now = datetime.now(timezone.utc)
    rows = read_ledger()
    spent = sum(fnum(r["cost_usd"]) for r in rows
                if datetime.fromisoformat(r["timestamp"]) >= now - timedelta(days=7))
    remaining = weekly - spent
    ok = True
    print(f"USD (janela de 7 dias): gasto {spent:.2f} / {weekly:.2f} — restante {remaining:.2f}; teto por run {run_max:.2f}")
    if remaining < run_max:
        ok = False
        print("  BLOQUEADO: orçamento semanal restante menor que o teto de uma run.")

    if rows and not os.environ.get("BUDGET_USD_ONLY"):
        last = rows[-1]
        for label, key, limit, reset_key in (("5h", "plan_5h_after", max_5h, "plan_5h_resets_at"),
                                             ("7d", "plan_7d_after", max_7d, "plan_7d_resets_at")):
            util, reset = last.get(key), last.get(reset_key)
            if not util:
                continue
            reset_dt = datetime.fromtimestamp(int(float(reset)), timezone.utc) if reset else None
            if reset_dt and reset_dt <= now:
                print(f"Plano {label}: janela já resetou ({reset_dt.astimezone():%d/%m %H:%M}).")
                continue
            quando = f" (reseta {reset_dt.astimezone():%d/%m %H:%M})" if reset_dt else ""
            print(f"Plano {label}: {float(util):.0%} usado na última run{quando}; limite configurado {limit:.0%}")
            if float(util) >= limit:
                ok = False
                print(f"  BLOQUEADO: uso do plano {label} acima do limite configurado.")
    return 0 if ok else 3


# ---------------------------------------------------------------- métricas

def agent_metrics(jsonl: Path):
    m = {"tool_calls": 0, "bash_calls": 0, "mvn_runs": 0}
    reads, edits = set(), set()
    windows = []
    result = None
    if not jsonl.exists():
        return m
    for line in jsonl.read_text(errors="replace").splitlines():
        try:
            ev = json.loads(line)
        except json.JSONDecodeError:
            continue
        t = ev.get("type")
        if t == "assistant":
            for item in ev.get("message", {}).get("content", []) or []:
                if item.get("type") != "tool_use":
                    continue
                m["tool_calls"] += 1
                name, inp = item.get("name"), item.get("input", {}) or {}
                if name == "Bash":
                    m["bash_calls"] += 1
                    if re.search(r"\bmvnw?\b", inp.get("command", "")):
                        m["mvn_runs"] += 1
                elif name == "Read" and inp.get("file_path"):
                    reads.add(inp["file_path"])
                elif name in ("Edit", "Write", "MultiEdit") and inp.get("file_path"):
                    edits.add(inp["file_path"])
        elif t == "rate_limit_event":
            w = ev.get("rate_limit_info", {}).get("unifiedWindows")
            if w:
                windows.append(w)
        elif t == "result":
            result = ev
    m["files_read"], m["files_edited"] = len(reads), len(edits)

    if result:
        m["agent_status"] = result.get("subtype", "")
        m["cost_usd"] = round(result.get("total_cost_usd", 0.0), 4)
        m["num_turns"] = result.get("num_turns", 0)
        m["duration_s"] = round(result.get("duration_ms", 0) / 1000, 1)
        tot = Counter()
        for usage in (result.get("modelUsage") or {}).values():
            tot["input"] += usage.get("inputTokens", 0)
            tot["output"] += usage.get("outputTokens", 0)
            tot["cache_write"] += usage.get("cacheCreationInputTokens", 0)
            tot["cache_read"] += usage.get("cacheReadInputTokens", 0)
        m.update(input_tokens=tot["input"], output_tokens=tot["output"],
                 cache_write_tokens=tot["cache_write"], cache_read_tokens=tot["cache_read"],
                 total_tokens=sum(tot.values()))
    else:
        m["agent_status"] = "sem_result"

    if windows:
        first, last = windows[0], windows[-1]
        m["plan_5h_before"] = first.get("five_hour", {}).get("utilization", "")
        m["plan_7d_before"] = first.get("seven_day", {}).get("utilization", "")
        m["plan_5h_after"] = last.get("five_hour", {}).get("utilization", "")
        m["plan_7d_after"] = last.get("seven_day", {}).get("utilization", "")
        m["plan_5h_resets_at"] = last.get("five_hour", {}).get("resetsAt", "")
        m["plan_7d_resets_at"] = last.get("seven_day", {}).get("resetsAt", "")
    return m


def coverage(ws: Path):
    csv_path = ws / "target/site/jacoco/jacoco.csv"
    if not csv_path.exists():
        return ""
    missed = covered = 0
    with csv_path.open() as f:
        for r in csv.DictReader(f):
            missed += int(r["LINE_MISSED"])
            covered += int(r["LINE_COVERED"])
    return round(covered / (missed + covered), 4) if missed + covered else ""


def acceptance(reports: Path):
    passed = total = 0
    for x in reports.glob("TEST-*.xml"):
        s = ET.parse(x).getroot()
        t = int(s.get("tests", 0))
        bad = int(s.get("failures", 0)) + int(s.get("errors", 0)) + int(s.get("skipped", 0))
        total += t
        passed += t - bad
    return passed, total


def mutation_score(ws: Path):
    f = ws / "target/pit-reports/mutations.csv"
    if not f.exists():
        return ""
    status = Counter(line.split(",")[5] for line in f.read_text().splitlines() if line.count(",") >= 5)
    total = sum(status.values())
    killed = status["KILLED"] + status["TIMED_OUT"] + status["MEMORY_ERROR"]
    return round(killed / total, 4) if total else ""


COMMENT = re.compile(r"/\*.*?\*/", re.S)


def code_metrics(ws: Path):
    m = {}
    pkgs = set()
    interfaces = 0
    for kind, sub in (("main", "src/main/java"), ("test", "src/test/java")):
        files = list((ws / sub).rglob("*.java"))
        loc = 0
        for f in files:
            src = COMMENT.sub("", f.read_text(errors="replace"))
            lines = [l.strip() for l in src.splitlines()]
            loc += sum(1 for l in lines if l and not l.startswith("//"))
            if kind == "main":
                pkgs.add(f.parent)
                if re.search(r"^\s*(public\s+)?(sealed\s+)?interface\s", src, re.M):
                    interfaces += 1
        m[f"{kind}_files"], m[f"{kind}_loc"] = len(files), loc
    m["interfaces"], m["packages"] = interfaces, len(pkgs)
    return m


def git(ws: Path, *args):
    return subprocess.run(["git", "-C", str(ws), *args], capture_output=True, text=True).stdout


def diff_metrics(ws: Path):
    files = added = removed = 0
    for line in git(ws, "diff", "--numstat", "baseline", "HEAD", "--", "src").splitlines():
        a, r, _ = line.split("\t", 2)
        files += 1
        added += int(a) if a != "-" else 0
        removed += int(r) if r != "-" else 0
    return {"diff_files": files, "diff_added": added, "diff_removed": removed}


def plugin_block(pom: str, artifact: str):
    i = pom.find(f"<artifactId>{artifact}</artifactId>")
    return pom[i:pom.find("</plugin>", i)] if i >= 0 else ""


def tampered(ws: Path):
    before, after = git(ws, "show", "baseline:pom.xml"), (ws / "pom.xml").read_text()
    return any(plugin_block(before, a).split() != plugin_block(after, a).split()
               for a in ("jacoco-maven-plugin", "pitest-maven"))


def cmd_collect(args):
    run = Path(args.run)
    ws = run / "workspace"
    row = {
        "timestamp": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "arch": args.arch, "task": args.task, "run": args.n,
        "protocol": os.environ.get("PROTOCOL", ""), "model": os.environ.get("MODEL", ""), "effort": os.environ.get("EFFORT", ""),
        "agent_exit": args.agent_exit,
        "build_ok": int((run / "gates/verify.exit").read_text().strip() == "0") if (run / "gates/verify.exit").exists() else 0,
        "guide_bytes": (ws / "CLAUDE.md").stat().st_size if (ws / "CLAUDE.md").exists() else "",
    }
    row.update(agent_metrics(run / "agent.jsonl"))
    row["line_coverage"] = coverage(ws)
    row["accept_passed"], row["accept_total"] = acceptance(run / "gates/acceptance-reports")
    arch_json = run / "gates/archcheck.json"
    try:
        row["arch_violations"] = json.loads(arch_json.read_text())["totalViolations"]
    except (OSError, ValueError, KeyError):
        row["arch_violations"] = ""
    row["mutation_score"] = mutation_score(ws)
    row.update(code_metrics(ws))
    row.update(diff_metrics(ws))
    row["gates_tampered"] = int(tampered(ws))
    off = run / "gates/off_workspace"
    row["off_workspace"] = int(off.read_text().strip() or 0) if off.exists() else 0
    row["success"] = int(row["build_ok"] == 1 and row["accept_total"] > 0
                         and row["accept_passed"] == row["accept_total"]
                         and row["arch_violations"] == 0 and row["gates_tampered"] == 0
                         and row["off_workspace"] == 0)
    (run / "metrics.json").write_text(json.dumps(row, indent=2, ensure_ascii=False))
    append_ledger(row)
    for k in ("agent_status", "cost_usd", "total_tokens", "num_turns", "build_ok", "line_coverage",
              "accept_passed", "accept_total", "arch_violations", "mutation_score", "success"):
        print(f"  {k:16} {row.get(k, '')}")
    return 0



# ---------------------------------------------------------------- gate preditivo (campanha)

def probe_plan():
    """Chamada mínima e isolada ao Claude só para ler o uso atual do plano (rate_limit_event)."""
    import tempfile
    with tempfile.TemporaryDirectory() as d:
        out = subprocess.run(
            ["claude", "-p", "ok", "--model", os.environ.get("MODEL", "claude-sonnet-5"),
             "--setting-sources", "project", "--strict-mcp-config", "--disable-slash-commands",
             "--output-format", "stream-json", "--verbose"],
            cwd=d, capture_output=True, text=True, timeout=180, stdin=subprocess.DEVNULL).stdout
    windows = None
    for line in out.splitlines():
        try:
            ev = json.loads(line)
        except json.JSONDecodeError:
            continue
        if ev.get("type") == "rate_limit_event":
            windows = ev["rate_limit_info"].get("unifiedWindows") or windows
    return windows


def expected_delta(task, key_before, key_after, default):
    deltas = defaultdict(list)
    for r in read_ledger():
        if r.get(key_before) not in ("", None) and r.get(key_after) not in ("", None):
            d = fnum(r[key_after]) - fnum(r[key_before])
            if d >= 0:
                deltas[r["task"]].append(d)
    pool = deltas.get(task) or [d for ds in deltas.values() for d in ds]
    return (max(pool) if pool else default) + 0.01


def cmd_gate(args):
    max_5h = float(os.environ.get("PLAN_5H_MAX", "0.85"))
    max_7d = float(os.environ.get("PLAN_7D_MAX", "0.92"))
    w = probe_plan()
    if not w:
        print(json.dumps({"ok": False, "erro": "sonda sem rate_limit_event"}))
        return 4
    h, s = w["five_hour"]["utilization"], w["seven_day"]["utilization"]
    eh = expected_delta(args.task, "plan_5h_before", "plan_5h_after", 0.20)
    es = expected_delta(args.task, "plan_7d_before", "plan_7d_after", 0.03)
    blocked = []
    if h + eh > max_5h:
        blocked.append(("5h", h, w["five_hour"]["resetsAt"]))
    if s + es > max_7d:
        blocked.append(("7d", s, w["seven_day"]["resetsAt"]))
    res = {"ok": not blocked, "5h": h, "7d": s, "esperado_5h": round(eh, 3), "esperado_7d": round(es, 3)}
    if blocked:
        res["bloqueio"] = [b[0] for b in blocked]
        res["retomar_em"] = max(int(b[2]) for b in blocked) + 300
    print(json.dumps(res))
    return 0 if not blocked else 3

# ---------------------------------------------------------------- relatório

def cmd_report(args):
    all_rows = read_ledger()
    if not all_rows:
        print("Nenhuma run registrada.")
        return 0
    now = datetime.now(timezone.utc)
    week = [r for r in all_rows if datetime.fromisoformat(r["timestamp"]) >= now - timedelta(days=7)]
    protocol = os.environ.get("PROTOCOL", "")
    rows = [r for r in all_rows if not protocol or r.get("protocol") == protocol]
    print(f"Runs: {len(all_rows)} | gasto total US$ {sum(fnum(r['cost_usd']) for r in all_rows):.2f} "
          f"| últimos 7 dias US$ {sum(fnum(r['cost_usd']) for r in week):.2f}")
    print(f"Comparando protocolo {protocol or '(todos)'}: {len(rows)} runs\n")

    groups = defaultdict(list)
    for r in rows:
        groups[(r["task"], r["arch"])].append(r)
    hdr = f"{'tarefa':6} {'arquitetura':11} {'n':>2} {'ok':>4} {'US$':>7} {'tokens':>10} {'turnos':>6} {'cob':>5} {'aceite':>7} {'viol':>4} {'mut':>5} {'LOC main':>8} {'diff':>6}"
    print(hdr)
    print("-" * len(hdr))
    med = lambda rs, k: statistics.median([fnum(r[k]) for r in rs if r[k] != ""] or [0])
    for (task, arch), rs in sorted(groups.items()):
        ok = sum(int(r["success"] or 0) for r in rs)
        print(f"{task:6} {arch:11} {len(rs):>2} {ok:>2}/{len(rs):<1} {med(rs, 'cost_usd'):>7.2f} "
              f"{med(rs, 'total_tokens'):>10.0f} {med(rs, 'num_turns'):>6.0f} {med(rs, 'line_coverage'):>5.0%} "
              f"{med(rs, 'accept_passed'):>3.0f}/{med(rs, 'accept_total'):<3.0f} {med(rs, 'arch_violations'):>4.0f} "
              f"{med(rs, 'mutation_score'):>5.0%} {med(rs, 'main_loc'):>8.0f} {med(rs, 'diff_added') + med(rs, 'diff_removed'):>6.0f}")
    print("\n(valores = mediana das runs; diff = linhas adicionadas + removidas em src/)")
    return 0


def main():
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest="cmd", required=True)
    sub.add_parser("budget")
    c = sub.add_parser("collect")
    c.add_argument("--run", required=True)
    c.add_argument("--arch", required=True)
    c.add_argument("--task", required=True)
    c.add_argument("--n", required=True)
    c.add_argument("--agent-exit", default="")
    sub.add_parser("report")
    g = sub.add_parser("gate")
    g.add_argument("--task", required=True)
    args = ap.parse_args()
    return {"budget": cmd_budget, "collect": cmd_collect, "report": cmd_report, "gate": cmd_gate}[args.cmd](args)


if __name__ == "__main__":
    sys.exit(main())
