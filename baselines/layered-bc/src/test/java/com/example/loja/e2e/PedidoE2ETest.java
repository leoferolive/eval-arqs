package com.example.loja.e2e;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PedidoE2ETest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void freteApiUrl(DynamicPropertyRegistry registry) {
        registry.add("frete.api.url", () -> "http://localhost:" + wireMock.getPort());
    }

    @BeforeEach
    void limparStubs() {
        wireMock.resetAll();
    }

    private Long criarProduto(String sku, double preco, int estoque) {
        Map<String, Object> corpo = Map.of("sku", sku, "nome", "Produto " + sku, "preco", preco, "estoque", estoque);
        ResponseEntity<Map> resposta = restTemplate.postForEntity("/produtos", corpo, Map.class);
        return ((Number) resposta.getBody().get("id")).longValue();
    }

    private void stubFreteSucesso() {
        wireMock.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\":25.50,\"prazoDias\":5}")));
    }

    @Test
    void fluxoCompletoDeCriacaoEcancelamentoDePedido() {
        Long produtoId = criarProduto("CAM-PED-1", 59.90, 10);
        stubFreteSucesso();

        Map<String, Object> pedidoRequest = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 2))
        );

        ResponseEntity<Map> criado = restTemplate.postForEntity("/pedidos", pedidoRequest, Map.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(criado.getBody().get("status")).isEqualTo("CRIADO");
        assertThat(((Number) criado.getBody().get("valorTotal")).doubleValue()).isEqualTo(145.30);
        Long pedidoId = ((Number) criado.getBody().get("id")).longValue();

        ResponseEntity<Map> produtoAposPedido = restTemplate.getForEntity("/produtos/" + produtoId, Map.class);
        assertThat(produtoAposPedido.getBody().get("estoque")).isEqualTo(8);

        ResponseEntity<Map> buscado = restTemplate.getForEntity("/pedidos/" + pedidoId, Map.class);
        assertThat(buscado.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> itens = (List<?>) buscado.getBody().get("itens");
        assertThat(itens).hasSize(1);

        ResponseEntity<Map> cancelado = restTemplate.postForEntity("/pedidos/" + pedidoId + "/cancelamento", null, Map.class);
        assertThat(cancelado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelado.getBody().get("status")).isEqualTo("CANCELADO");

        ResponseEntity<Map> produtoAposCancelamento = restTemplate.getForEntity("/produtos/" + produtoId, Map.class);
        assertThat(produtoAposCancelamento.getBody().get("estoque")).isEqualTo(10);

        ResponseEntity<Map> cancelarNovamente = restTemplate.postForEntity("/pedidos/" + pedidoId + "/cancelamento", null, Map.class);
        assertThat(cancelarNovamente.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(cancelarNovamente.getBody().get("codigo")).isEqualTo("PEDIDO_JA_CANCELADO");
    }

    @Test
    void criarPedidoComProdutoInexistenteDeveRetornar422SemAlterarEstoque() {
        Map<String, Object> pedidoRequest = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", 999999L, "quantidade", 1))
        );

        ResponseEntity<Map> resposta = restTemplate.postForEntity("/pedidos", pedidoRequest, Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getBody().get("codigo")).isEqualTo("PRODUTO_INEXISTENTE");
    }

    @Test
    void criarPedidoComEstoqueInsuficienteDeveRetornar422SemAlterarEstoque() {
        Long produtoId = criarProduto("CAM-PED-2", 10.00, 1);

        Map<String, Object> pedidoRequest = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 5))
        );

        ResponseEntity<Map> resposta = restTemplate.postForEntity("/pedidos", pedidoRequest, Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getBody().get("codigo")).isEqualTo("ESTOQUE_INSUFICIENTE");

        ResponseEntity<Map> produtoInalterado = restTemplate.getForEntity("/produtos/" + produtoId, Map.class);
        assertThat(produtoInalterado.getBody().get("estoque")).isEqualTo(1);
    }

    @Test
    void criarPedidoComProdutoInativoDeveRetornar422() {
        Long produtoId = criarProduto("CAM-PED-3", 10.00, 5);
        Map<String, Object> desativar = Map.of("nome", "Produto CAM-PED-3", "preco", 10.00, "estoque", 5, "ativo", false);
        restTemplate.put("/produtos/" + produtoId, desativar);

        Map<String, Object> pedidoRequest = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 1))
        );

        ResponseEntity<Map> resposta = restTemplate.postForEntity("/pedidos", pedidoRequest, Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getBody().get("codigo")).isEqualTo("PRODUTO_INATIVO");
    }

    @Test
    void criarPedidoComFreteIndisponivelDeveRetornar502SemAlterarEstoque() {
        Long produtoId = criarProduto("CAM-PED-4", 10.00, 5);
        wireMock.stubFor(get(urlPathEqualTo("/fretes")).willReturn(aResponse().withStatus(500)));

        Map<String, Object> pedidoRequest = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 1))
        );

        ResponseEntity<Map> resposta = restTemplate.postForEntity("/pedidos", pedidoRequest, Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resposta.getBody().get("codigo")).isEqualTo("FRETE_INDISPONIVEL");

        ResponseEntity<Map> produtoInalterado = restTemplate.getForEntity("/produtos/" + produtoId, Map.class);
        assertThat(produtoInalterado.getBody().get("estoque")).isEqualTo(5);
    }

    @Test
    void criarPedidoComFreteLentoDeveRetornar502() {
        Long produtoId = criarProduto("CAM-PED-5", 10.00, 5);
        wireMock.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(2500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\":10.00,\"prazoDias\":3}")));

        Map<String, Object> pedidoRequest = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 1))
        );

        ResponseEntity<Map> resposta = restTemplate.postForEntity("/pedidos", pedidoRequest, Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resposta.getBody().get("codigo")).isEqualTo("FRETE_INDISPONIVEL");
    }

    @Test
    void buscarPedidoInexistenteDeveRetornar404() {
        ResponseEntity<Map> resposta = restTemplate.getForEntity("/pedidos/999999", Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().get("codigo")).isEqualTo("PEDIDO_NAO_ENCONTRADO");
    }
}
