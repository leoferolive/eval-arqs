# Bugs plantados (T4)

Depois de promover as baselines T0 (`harness/promote.sh`), crie aqui um patch por arquitetura
(`layered.patch`, `layered-bc.patch`, `hexagonal.patch`, `clean.patch`) que plante **o mesmo bug
comportamental**: o cancelamento de pedido não devolve o estoque.

O patch deve remover apenas a devolução de estoque (e o que for estritamente necessário para compilar),
sem apagar testes. Gere com `git diff` a partir de uma cópia da baseline. O `run.sh` aplica o patch e
commita como "bug plantado" antes de chamar o agente.
