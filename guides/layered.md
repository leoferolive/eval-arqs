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
