package com.example.loja.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.example.loja.adapter.controller.dto.ErroResponse;
import com.example.loja.adapter.controller.dto.PedidoResponse;
import com.example.loja.adapter.controller.dto.ProdutoResponse;
import com.github.tomakehurst.wiremock.WireMockServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PedidosE2ETest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void iniciarWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    @AfterEach
    void limparStubs() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void pararWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configurarProperties(DynamicPropertyRegistry registry) {
        registry.add("frete.api.url", () -> "http://localhost:8089");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Long criarProduto(String sku, double preco, int estoque) {
        Map<String, Object> corpo = Map.of("sku", sku, "nome", "Produto " + sku, "preco", preco, "estoque", estoque);
        ResponseEntity<ProdutoResponse> resposta = restTemplate.postForEntity(url("/produtos"), corpo,
                ProdutoResponse.class);
        return resposta.getBody().id();
    }

    private void estubarFreteComSucesso() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\": 25.50, \"prazoDias\": 5}")));
    }

    @Test
    void fluxoCompletoDeCriacaoEConsulta() {
        Long produtoId = criarProduto("CAM-100", 59.90, 10);
        estubarFreteComSucesso();

        Map<String, Object> pedidoRequest = Map.of("cep", "01310100", "itens",
                List.of(Map.of("produtoId", produtoId, "quantidade", 2)));

        ResponseEntity<PedidoResponse> criado = restTemplate.postForEntity(url("/pedidos"), pedidoRequest,
                PedidoResponse.class);

        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PedidoResponse pedido = criado.getBody();
        assertThat(pedido.status()).isEqualTo("CRIADO");
        assertThat(pedido.valorItens()).isEqualByComparingTo("119.80");
        assertThat(pedido.valorFrete()).isEqualByComparingTo("25.50");
        assertThat(pedido.valorTotal()).isEqualByComparingTo("145.30");
        assertThat(pedido.itens()).hasSize(1);

        ResponseEntity<ProdutoResponse> produtoAposPedido = restTemplate.getForEntity(url("/produtos/" + produtoId),
                ProdutoResponse.class);
        assertThat(produtoAposPedido.getBody().estoque()).isEqualTo(8);

        ResponseEntity<PedidoResponse> buscado = restTemplate.getForEntity(url("/pedidos/" + pedido.id()),
                PedidoResponse.class);
        assertThat(buscado.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<PedidoResponse[]> listado = restTemplate.getForEntity(url("/pedidos"), PedidoResponse[].class);
        assertThat(listado.getBody()).extracting(PedidoResponse::id).contains(pedido.id());
    }

    @Test
    void cancelarPedidoDevolveEstoqueENaoPodeSerCanceladoNovamente() {
        Long produtoId = criarProduto("CAM-101", 20.00, 5);
        estubarFreteComSucesso();

        Map<String, Object> pedidoRequest = Map.of("cep", "01310100", "itens",
                List.of(Map.of("produtoId", produtoId, "quantidade", 3)));
        ResponseEntity<PedidoResponse> criado = restTemplate.postForEntity(url("/pedidos"), pedidoRequest,
                PedidoResponse.class);
        Long pedidoId = criado.getBody().id();

        ResponseEntity<PedidoResponse> cancelado = restTemplate.postForEntity(
                url("/pedidos/" + pedidoId + "/cancelamento"), null, PedidoResponse.class);
        assertThat(cancelado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelado.getBody().status()).isEqualTo("CANCELADO");

        ResponseEntity<ProdutoResponse> produtoAposCancelamento = restTemplate.getForEntity(
                url("/produtos/" + produtoId), ProdutoResponse.class);
        assertThat(produtoAposCancelamento.getBody().estoque()).isEqualTo(5);

        ResponseEntity<ErroResponse> segundoCancelamento = restTemplate.postForEntity(
                url("/pedidos/" + pedidoId + "/cancelamento"), null, ErroResponse.class);
        assertThat(segundoCancelamento.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(segundoCancelamento.getBody().codigo()).isEqualTo("PEDIDO_JA_CANCELADO");
    }

    @Test
    void criarPedidoComProdutoInexistenteRetorna422ENaoAlteraEstoque() {
        Map<String, Object> pedidoRequest = Map.of("cep", "01310100", "itens",
                List.of(Map.of("produtoId", 999999, "quantidade", 1)));

        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/pedidos"), pedidoRequest,
                ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.valueOf(422));
        assertThat(resposta.getBody().codigo()).isEqualTo("PRODUTO_INEXISTENTE");
    }

    @Test
    void criarPedidoComEstoqueInsuficienteRetorna422ENaoAlteraEstoque() {
        Long produtoId = criarProduto("CAM-102", 20.00, 1);

        Map<String, Object> pedidoRequest = Map.of("cep", "01310100", "itens",
                List.of(Map.of("produtoId", produtoId, "quantidade", 5)));

        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/pedidos"), pedidoRequest,
                ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.valueOf(422));
        assertThat(resposta.getBody().codigo()).isEqualTo("ESTOQUE_INSUFICIENTE");

        ResponseEntity<ProdutoResponse> produtoInalterado = restTemplate.getForEntity(url("/produtos/" + produtoId),
                ProdutoResponse.class);
        assertThat(produtoInalterado.getBody().estoque()).isEqualTo(1);
    }

    @Test
    void criarPedidoComProdutoInativoRetorna422() {
        Long produtoId = criarProduto("CAM-103", 20.00, 5);
        Map<String, Object> desativar = Map.of("nome", "Produto CAM-103", "preco", 20.00, "estoque", 5, "ativo",
                false);
        restTemplate.exchange(url("/produtos/" + produtoId), HttpMethod.PUT, new HttpEntity<>(desativar),
                ProdutoResponse.class);

        Map<String, Object> pedidoRequest = Map.of("cep", "01310100", "itens",
                List.of(Map.of("produtoId", produtoId, "quantidade", 1)));

        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/pedidos"), pedidoRequest,
                ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.valueOf(422));
        assertThat(resposta.getBody().codigo()).isEqualTo("PRODUTO_INATIVO");
    }

    @Test
    void criarPedidoComFreteIndisponivelRetorna502ENaoDecrementaEstoque() {
        Long produtoId = criarProduto("CAM-104", 20.00, 5);
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes")).willReturn(aResponse().withStatus(500)));

        Map<String, Object> pedidoRequest = Map.of("cep", "01310100", "itens",
                List.of(Map.of("produtoId", produtoId, "quantidade", 2)));

        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/pedidos"), pedidoRequest,
                ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resposta.getBody().codigo()).isEqualTo("FRETE_INDISPONIVEL");

        ResponseEntity<ProdutoResponse> produtoInalterado = restTemplate.getForEntity(url("/produtos/" + produtoId),
                ProdutoResponse.class);
        assertThat(produtoInalterado.getBody().estoque()).isEqualTo(5);
    }

    @Test
    void criarPedidoComCorpoInvalidoRetorna400() {
        Map<String, Object> pedidoRequest = Map.of("cep", "123", "itens", List.of());

        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/pedidos"), pedidoRequest,
                ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().codigo()).isEqualTo("VALIDACAO");
    }

    @Test
    void buscarPedidoInexistenteRetorna404() {
        ResponseEntity<ErroResponse> resposta = restTemplate.getForEntity(url("/pedidos/999999"),
                ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().codigo()).isEqualTo("PEDIDO_NAO_ENCONTRADO");
    }

    @Test
    void cancelarPedidoInexistenteRetorna404() {
        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(
                url("/pedidos/999999/cancelamento"), null, ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().codigo()).isEqualTo("PEDIDO_NAO_ENCONTRADO");
    }
}
