# Arquitetura: Layered + Bounded Context

A mesma divisão em camadas (Controller / Service / Repository), mas organizada primeiro **por
domínio** (bounded context) e depois por camada. A comunicação entre domínios é restrita a
**Service → Service**, preparando cada contexto para virar um microsserviço no futuro.

## Pacotes

```
com.example.loja
├── catalogo
│   ├── controller  service  repository  model  dto  exception
├── pedidos
│   ├── controller  service  repository  model  dto  client  exception
└── shared          apenas infraestrutura transversal (ex.: @RestControllerAdvice, ErroResponse, config)
```

Todas as classes (exceto `LojaApplication`) ficam em um desses pacotes.

## Regras de dependência (verificadas)

Dentro de cada contexto valem as regras de camadas:

1. `controller` usa `service`, `dto`, `exception`. Não acessa `repository` nem `client`.
2. `repository` e `client` só são acessados pelo `service` do **mesmo** contexto.

Entre contextos:

3. Um contexto só pode depender de outro através de `<outro>.service` e `<outro>.dto`
   (e `<outro>.exception`). Ex.: `pedidos.service` → `catalogo.service.ProdutoService`.
4. **Proibido** acessar `controller`, `repository` ou `model` (entidades JPA) de outro contexto.
5. `shared` não depende de `catalogo` nem de `pedidos`.
6. Sem relacionamentos JPA entre contextos: `pedidos` guarda apenas o `produtoId`.

## Diretrizes

- O serviço do contexto dono expõe operações de alto nível para os outros contextos
  (ex.: `ProdutoService.reservarEstoque(produtoId, quantidade)` devolvendo um DTO),
  em vez de expor entidades.
- Controllers recebem e devolvem DTOs.
- Use `@Transactional` nos métodos de service que alteram estado.

## Testes

- Testes unitários de service com Mockito (mockando o service do outro contexto).
- `@WebMvcTest` para controllers.
- `@SpringBootTest` + WireMock para o fluxo com a API de frete.
