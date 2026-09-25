# Projeto Loja

API REST Spring Boot (Java 21, Maven). O contrato funcional está em `SPEC.md` — siga-o à risca
(rotas, códigos HTTP, `codigo` de erro, nomes de campos JSON).

## Regras de trabalho

- Você está rodando sem supervisão humana: não faça perguntas, tome decisões razoáveis e conclua a tarefa.
- A tarefa só está concluída quando `mvn -B verify` passa (testes verdes + cobertura de linhas ≥ 90% via JaCoCo).
- Não altere a configuração dos plugins `jacoco-maven-plugin` e `pitest-maven` no `pom.xml`
  (nem adicione exclusões de cobertura). Adicionar dependências é permitido.
- Escreva testes que verifiquem comportamento de verdade (asserções sobre resultado e efeitos),
  não testes que só executam código para subir cobertura.
- A aplicação roda com `spring.jpa.open-in-view=false`: nada de lazy loading fora de transação.
  Garanta que tudo o que a resposta HTTP precisa (ex.: itens do pedido) seja carregado dentro da
  transação/consulta, senão ocorre `LazyInitializationException` em produção.
- Inclua ao menos um teste de ponta a ponta por fluxo principal que suba a aplicação e faça chamadas
  HTTP reais (`@SpringBootTest(webEnvironment = RANDOM_PORT)`), **sem** `@Transactional` no teste,
  para pegar problemas que só aparecem fora de uma transação.
- Não use `git commit`; o avaliador cuida do versionamento.
- A arquitetura abaixo é obrigatória. Um verificador automático (ArchUnit) vai checar as regras
  de dependência descritas nela.

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
