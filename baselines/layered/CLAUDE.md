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

# Arquitetura: Layered (Controller / Service / Repository)

Divisão técnica tradicional em camadas, organizada por **tipo de componente**. Sem isolamento
forte de domínio: as entidades JPA são o modelo de domínio. Priorize simplicidade e pouca cerimônia.

## Pacotes

```
com.example.loja
├── controller   @RestController — HTTP, validação de entrada, mapeamento DTO
├── service      @Service — regras de negócio e transações
├── repository   interfaces Spring Data JPA
├── model        entidades JPA (@Entity) e enums
├── dto          records de request/response
├── client       clientes HTTP para APIs externas (frete)
├── exception    exceções de negócio e @RestControllerAdvice
└── config       beans de configuração (ex.: RestClient)
```

Todas as classes (exceto `LojaApplication`) ficam em um desses pacotes.

## Regras de dependência (verificadas)

1. Nenhuma camada depende de `controller` (é a borda de entrada).
2. `controller` pode usar `service`, `dto`, `exception`. **Não** acessa `repository` nem `client`.
3. `service` pode usar `repository`, `client`, `model`, `dto`, `exception`. Não conhece `controller`.
4. `repository` só é acessado por `service`.
5. `client` só é acessado por `service`.

## Diretrizes

- Um service por agregado é suficiente (ex.: `ProdutoService`, `PedidoService`).
- `PedidoService` pode chamar `ProdutoRepository` ou `ProdutoService` diretamente — não há
  fronteira entre domínios nesta arquitetura.
- Controllers recebem e devolvem DTOs; não exponha entidades JPA na API.
- Use `@Transactional` nos métodos de service que alteram estado.
- Trate exceções de negócio num único `@RestControllerAdvice`.

## Testes

- Testes unitários de service com Mockito.
- `@WebMvcTest` para controllers.
- `@SpringBootTest` + WireMock para o fluxo com a API de frete.
