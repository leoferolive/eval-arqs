# eval-arqs

Experimento controlado para comparar **quatro arquiteturas Spring Boot** sob desenvolvimento agêntico
(Claude Code, `claude-sonnet-5`, effort `medium`): custo (USD e tokens), esforço do agente,
complexidade estrutural e qualidade.

| Arquitetura | Guia |
|---|---|
| Layered (Controller/Service/Repository) | `guides/layered.md` |
| Layered + Bounded Context | `guides/layered-bc.md` |
| Hexagonal (Ports & Adapters) | `guides/hexagonal.md` |
| Clean Architecture | `guides/clean.md` |

## Princípio

Tudo fixo, só a arquitetura varia: mesma spec (`spec/SPEC.md`), mesmo esqueleto (`template/`),
mesmas tarefas (`tasks/`), mesmo modelo/effort/CLI, mesma suíte de aceitação black-box (`acceptance/`).
O `CLAUDE.md` de cada workspace = `guides/_common.md` + guia da arquitetura.

A suíte de aceitação e as regras ArchUnit (`archcheck/`) ficam **fora** do workspace do agente.

## Tarefas

| | Tarefa | O que revela |
|---|---|---|
| T0 | Construir a API do zero | Custo inicial / cerimônia |
| T1 | Campo `categoria` + filtro | Espalhamento de mudança simples |
| T2 | Trocar provedor de frete (v2) | Isolamento de infraestrutura |
| T3 | Produto em pedido aberto não pode ser removido | Acoplamento entre domínios |
| T4 | Corrigir bug plantado | Navegabilidade |
| T5 | Pedidos acessa Catálogo só via HTTP (opcional) | Preparação para microsserviço |

T1–T5 partem sempre da **baseline** da arquitetura (uma run T0 aprovada), não uma da outra.

## Isolamento do agente

Runs headless com `--setting-sources project --strict-mcp-config --disable-slash-commands`:
sem hooks, plugins, MCPs, skills ou CLAUDE.md globais de quem executa. Só o `CLAUDE.md` do workspace
é carregado. Resultado reproduzível na máquina de qualquer pessoa.

## Orçamento

`harness/config.env`: `BUDGET_WEEKLY_USD=90` (janela móvel de 7 dias somando `results/runs.csv`),
`RUN_MAX_USD` (teto duro por run, via `--max-budget-usd`) e limites de uso do plano Claude
(`PLAN_5H_MAX`, `PLAN_7D_MAX`, lidos do `rate_limit_event` da última run). O `run.sh` recusa
iniciar se não houver saldo e mostra quando a janela reseta.

> `cost_usd` é o `total_cost_usd` do Claude Code: custo equivalente em preço de lista da API.
> Numa assinatura (Pro/Max) não é cobrado, mas é a mesma régua do limite em USD do time.

## Fluxo

```bash
# fase A — T0 por arquitetura (comece pela layered: é a sonda de custo)
harness/run.sh layered T0 1
harness/evalctl.py report
harness/promote.sh layered 1          # vira baseline para T1..T5

# fase B — T1..T5 a partir das baselines
harness/run.sh hexagonal T2 1

# fase C — repetições onde as diferenças forem pequenas
harness/run.sh clean T3 2
```

Cada run fica em `runs/<arq>/<tarefa>/run-<n>/`: `agent.jsonl` (log completo do agente),
`workspace/` (git com tags `baseline` → commit `agent`), `gates/` (logs e relatórios) e `metrics.json`.

## Métricas (`results/runs.csv`)

- **Custo:** `cost_usd`, tokens (input, output, cache write, cache read, total), % do plano antes/depois
- **Esforço do agente:** turnos, tool calls, chamadas Bash, execuções de `mvn`, arquivos lidos/editados, duração
- **Qualidade:** build + cobertura de linhas (JaCoCo ≥ 90%), aceitação passados/total, violações ArchUnit,
  mutation score (PIT), `gates_tampered` (agente mexeu na config de JaCoCo/PIT)
- **Complexidade:** arquivos e LOC de main/test, interfaces, pacotes, diff vs. baseline (arquivos, +/−)
- **`success`** = build ok + aceitação 100% + 0 violações + gates intactos

## Validação do harness

`harness/reference/layered/` é uma implementação de referência (nunca entregue ao agente) usada para
validar a suíte e os gates sem gastar tokens:

```bash
RUN_PIT=0 IMPL_DIR=harness/reference/layered harness/run.sh layered T0 0 --no-agent
```

Resultado esperado: 21/21 na aceitação, 0 violações, cobertura ≥ 90%, `success=1`.
Apague `runs/layered/T0/run-0` e a linha correspondente em `results/runs.csv` depois.

## Ameaças à validade

Projeto pequeno favorece a Layered; um único modelo e effort; guias com tamanhos diferentes
(`guide_bytes` é registrado); não-determinismo do LLM (use mediana de ≥ 2–3 runs nas comparações
decisivas); o autor dos guias e tarefas também é o avaliador.
