# Arquitetura: Clean Architecture

Quatro anéis concêntricos com a **regra de dependência** explícita: o código só pode depender de
anéis mais internos. Entities → Use Cases → Interface Adapters → Frameworks & Drivers.

## Pacotes

```
com.example.loja
├── entity            anel 1 — regras de negócio corporativas (classes puras, sem framework)
├── usecase           anel 2 — um caso de uso por classe (Interactor), com seus modelos
│                     de entrada/saída (records) e interfaces de gateway que ele precisa
├── adapter           anel 3 — controllers, presenters/view models, implementações de gateway
│   ├── controller    @RestController, DTOs HTTP, @RestControllerAdvice
│   ├── presenter     conversão de output model → resposta HTTP
│   └── gateway       implementações das interfaces de gateway (persistência JPA, API de frete)
└── infrastructure    anel 4 — configuração Spring (@Configuration/@Bean), clientes HTTP, detalhes de framework
```

Subpacotes por domínio dentro de cada anel são opcionais (ex.: `usecase.pedidos`).

## Regras de dependência (verificadas)

1. `entity` não depende de nenhum outro anel, nem de `org.springframework..` ou `jakarta.persistence..`.
2. `usecase` depende apenas de `entity`. Sem `org.springframework..` nem `jakarta.persistence..`.
3. `adapter` depende de `usecase` e `entity`. Não depende de `infrastructure`.
4. `infrastructure` pode depender de todos os anéis.

## Diretrizes

- Cada caso de uso é uma classe própria (ex.: `CriarPedidoInteractor`) com um método de execução,
  recebendo um input model e devolvendo um output model. Nada de "service" genérico com vários métodos.
- Casos de uso nunca recebem nem devolvem entidades JPA ou DTOs HTTP.
- Entidades JPA e Spring Data ficam em `adapter.gateway` e são mapeadas para as entidades do anel 1.
- Interactors não têm anotações Spring; são registrados como beans em `infrastructure`.
- Transações: aplique `@Transactional` fora dos anéis internos (controller ou configuração).

## Testes

- Testes unitários das entidades e interactors com gateways mockados (sem Spring).
- `@WebMvcTest` para controllers; `@DataJpaTest` para gateways de persistência.
- `@SpringBootTest` + WireMock para o gateway de frete e o fluxo completo.
