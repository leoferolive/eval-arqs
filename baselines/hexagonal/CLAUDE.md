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

# Arquitetura: Hexagonal (Ports & Adapters)

O domínio fica isolado no centro, sem conhecer frameworks. Ele se comunica com o mundo externo
por **portas** (interfaces) implementadas por **adaptadores**. Dois lados: dentro (domínio +
aplicação) e fora (adaptadores de entrada e saída).

## Pacotes

```
com.example.loja
├── domain                 modelo de domínio puro (classes/records, regras, exceções de domínio)
├── application
│   ├── port/in            portas de entrada: interfaces de casos de uso (ex.: CriarPedidoUseCase)
│   ├── port/out           portas de saída: interfaces (ex.: ProdutoRepositoryPort, FreteGateway)
│   └── service            implementações das portas de entrada
├── adapter
│   ├── in/web             @RestController, DTOs web, @RestControllerAdvice
│   └── out
│       ├── persistence    entidades JPA, Spring Data, implementações das portas de persistência
│       └── frete          cliente HTTP que implementa FreteGateway
└── config                 wiring Spring (@Configuration/@Bean) dos services da aplicação
```

Subpacotes por domínio dentro de cada camada são opcionais (ex.: `domain.catalogo`, `domain.pedidos`).

## Regras de dependência (verificadas)

1. `domain` não depende de `application`, `adapter`, `config`, nem de `org.springframework..`
   ou `jakarta.persistence..`.
2. `application` depende apenas de `domain` (e de si mesma). Não depende de `adapter`, `config`,
   `org.springframework..` nem `jakarta.persistence..`.
3. `adapter.in` não depende de `adapter.out`, e vice-versa.
4. Adaptadores falam com o núcleo apenas pelas portas (`application.port..`) e pelo `domain`.

## Diretrizes

- Entidades JPA ficam só em `adapter.out.persistence` e são mapeadas de/para o modelo de domínio.
- Regras de negócio (validar estoque, calcular totais, cancelar) moram no `domain` ou nos
  services da aplicação, nunca nos adaptadores.
- Services da aplicação não têm anotações Spring; são registrados como beans em `config`.
- Transações: aplique `@Transactional` no adaptador de entrada ou via configuração, fora do núcleo.

## Testes

- Testes unitários do domínio e dos services com portas mockadas (sem Spring).
- `@WebMvcTest` para o adaptador web; testes de adaptador de persistência com `@DataJpaTest`.
- `@SpringBootTest` + WireMock para o adaptador de frete e o fluxo completo.
