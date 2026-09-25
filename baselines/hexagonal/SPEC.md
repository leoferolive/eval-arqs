# SPEC — Loja (Catálogo + Pedidos)

API REST de uma loja simples com dois domínios — **Catálogo** e **Pedidos** — e uma integração
com uma **API externa de frete**. Este documento é o contrato funcional; ele é idêntico para todas
as arquiteturas avaliadas.

## Convenções

- Pacote raiz: `com.example.loja`. Classe principal: `com.example.loja.LojaApplication` (já existe).
- JSON em `camelCase`. Valores monetários são números JSON com 2 casas decimais (`BigDecimal`, escala 2, `HALF_UP`).
- Banco: H2 em memória (já configurado). Não há autenticação.
- A URL base da API de frete vem da propriedade `frete.api.url`.
- Todo erro devolve o corpo:

```json
{ "codigo": "PRODUTO_NAO_ENCONTRADO", "mensagem": "texto livre" }
```

| HTTP | `codigo` | Quando |
|---|---|---|
| 400 | `VALIDACAO` | Corpo inválido (campos ausentes, fora de faixa, formato errado) |
| 404 | `PRODUTO_NAO_ENCONTRADO` / `PEDIDO_NAO_ENCONTRADO` | Recurso inexistente |
| 409 | `SKU_DUPLICADO` | SKU já cadastrado |
| 409 | `PEDIDO_JA_CANCELADO` | Cancelar pedido já cancelado |
| 422 | `PRODUTO_INEXISTENTE` / `PRODUTO_INATIVO` / `ESTOQUE_INSUFICIENTE` | Regra de negócio na criação de pedido |
| 502 | `FRETE_INDISPONIVEL` | API de frete falhou, respondeu não-2xx ou demorou mais de 2s |

---

## Domínio Catálogo — Produto

| Campo | Tipo | Regras |
|---|---|---|
| `id` | long | gerado |
| `sku` | string | obrigatório, não vazio, até 30 caracteres, **único**, imutável |
| `nome` | string | obrigatório, não vazio, até 120 caracteres |
| `preco` | decimal | obrigatório, `> 0` |
| `estoque` | int | obrigatório, `>= 0` |
| `ativo` | boolean | `true` na criação |

### Endpoints

| Método | Rota | Corpo | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/produtos` | `{sku, nome, preco, estoque}` | 201 + Produto | 400, 409 `SKU_DUPLICADO` |
| GET | `/produtos` | — | 200 + lista de Produto ordenada por `id` | — |
| GET | `/produtos/{id}` | — | 200 + Produto | 404 |
| PUT | `/produtos/{id}` | `{nome, preco, estoque, ativo}` (todos obrigatórios) | 200 + Produto | 400, 404 |
| DELETE | `/produtos/{id}` | — | 204 | 404 |

Exemplo de Produto:

```json
{ "id": 1, "sku": "CAM-001", "nome": "Camiseta", "preco": 59.90, "estoque": 10, "ativo": true }
```

---

## Domínio Pedidos — Pedido

| Campo | Tipo | Regras |
|---|---|---|
| `id` | long | gerado |
| `cep` | string | obrigatório, exatamente 8 dígitos |
| `status` | enum | `CRIADO` ou `CANCELADO` |
| `itens` | lista | obrigatória, ao menos 1 item |
| `itens[].produtoId` | long | obrigatório |
| `itens[].quantidade` | int | obrigatório, `>= 1` |
| `itens[].precoUnitario` | decimal | preço do produto **no momento da criação** |
| `itens[].subtotal` | decimal | `precoUnitario × quantidade` |
| `valorItens` | decimal | soma dos subtotais |
| `valorFrete` | decimal | vindo da API de frete |
| `prazoEntregaDias` | int | vindo da API de frete |
| `valorTotal` | decimal | `valorItens + valorFrete` |

### Endpoints

| Método | Rota | Corpo | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/pedidos` | `{cep, itens:[{produtoId, quantidade}]}` | 201 + Pedido | 400, 422, 502 |
| GET | `/pedidos` | — | 200 + lista de Pedido ordenada por `id` | — |
| GET | `/pedidos/{id}` | — | 200 + Pedido | 404 |
| POST | `/pedidos/{id}/cancelamento` | — | 200 + Pedido com `status: CANCELADO` | 404, 409 |

### Regras de criação (`POST /pedidos`)

1. Para cada item: o produto deve existir (`422 PRODUTO_INEXISTENTE`), estar ativo (`422 PRODUTO_INATIVO`)
   e ter estoque `>= quantidade` (`422 ESTOQUE_INSUFICIENTE`).
2. Consulta o frete na API externa (ver abaixo). Falha → `502 FRETE_INDISPONIVEL`.
3. Só se tudo acima passar: o estoque de cada produto é **decrementado** e o pedido é salvo com status `CRIADO`.
   Em qualquer erro, **nada** é persistido e nenhum estoque muda.

### Regras de cancelamento

- Pedido `CRIADO` → `CANCELADO`, e a quantidade de cada item **volta** ao estoque do produto.
- Pedido já `CANCELADO` → `409 PEDIDO_JA_CANCELADO`, sem efeitos.

Exemplo de Pedido:

```json
{
  "id": 1, "cep": "01310100", "status": "CRIADO",
  "itens": [ { "produtoId": 1, "quantidade": 2, "precoUnitario": 59.90, "subtotal": 119.80 } ],
  "valorItens": 119.80, "valorFrete": 25.50, "prazoEntregaDias": 5, "valorTotal": 145.30
}
```

---

## Integração — API externa de frete

```
GET {frete.api.url}/fretes?cep=01310100

200 OK
{ "valor": 25.50, "prazoDias": 5 }
```

- Timeout total de 2 segundos.
- Qualquer resposta não-2xx, timeout ou erro de conexão → `502 FRETE_INDISPONIVEL`.
- Nos testes, simule a API com WireMock (dependência já presente no `pom.xml`).
