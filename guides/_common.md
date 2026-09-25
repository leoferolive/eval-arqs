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

