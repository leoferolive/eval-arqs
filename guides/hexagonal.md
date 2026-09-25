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
