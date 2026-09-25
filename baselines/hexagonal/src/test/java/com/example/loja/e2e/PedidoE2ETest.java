package com.example.loja.e2e;

import com.example.loja.adapter.in.web.dto.PedidoResponse;
import com.example.loja.adapter.in.web.dto.ProdutoResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PedidoE2ETest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @BeforeAll
    static void iniciarWireMock() {
        wireMockServer = new WireMockServer(options().port(8089));
        wireMockServer.start();
    }

    @AfterAll
    static void pararWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetarStubs() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\":25.50,\"prazoDias\":5}")));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Long criarProduto(String sku, String nome, double preco, int estoque) {
        Map<String, Object> corpo = Map.of("sku", sku, "nome", nome, "preco", preco, "estoque", estoque);
        ResponseEntity<ProdutoResponse> resposta = restTemplate.postForEntity(url("/produtos"), corpo, ProdutoResponse.class);
        return resposta.getBody().id();
    }

    @Test
    void fluxoCompletoDeCriacaoECancelamentoDePedido() {
        Long produtoId = criarProduto("CAM-PED-1", "Camiseta Pedido", 59.90, 10);

        Map<String, Object> corpoPedido = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 2)));
        ResponseEntity<PedidoResponse> criado = restTemplate.postForEntity(url("/pedidos"), corpoPedido, PedidoResponse.class);

        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PedidoResponse pedido = criado.getBody();
        assertThat(pedido.status()).isEqualTo("CRIADO");
        assertThat(pedido.valorItens()).isEqualByComparingTo("119.80");
        assertThat(pedido.valorFrete()).isEqualByComparingTo("25.50");
        assertThat(pedido.valorTotal()).isEqualByComparingTo("145.30");
        assertThat(pedido.itens()).hasSize(1);

        ProdutoResponse produtoAposCompra = restTemplate.getForEntity(url("/produtos/" + produtoId), ProdutoResponse.class).getBody();
        assertThat(produtoAposCompra.estoque()).isEqualTo(8);

        ResponseEntity<PedidoResponse> buscado = restTemplate.getForEntity(url("/pedidos/" + pedido.id()), PedidoResponse.class);
        assertThat(buscado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(buscado.getBody().itens()).hasSize(1);

        ResponseEntity<PedidoResponse[]> lista = restTemplate.getForEntity(url("/pedidos"), PedidoResponse[].class);
        assertThat(lista.getBody()).extracting(PedidoResponse::id).contains(pedido.id());

        ResponseEntity<PedidoResponse> cancelado = restTemplate.postForEntity(
                url("/pedidos/" + pedido.id() + "/cancelamento"), null, PedidoResponse.class);
        assertThat(cancelado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelado.getBody().status()).isEqualTo("CANCELADO");

        ProdutoResponse produtoAposCancelamento = restTemplate.getForEntity(url("/produtos/" + produtoId), ProdutoResponse.class).getBody();
        assertThat(produtoAposCancelamento.estoque()).isEqualTo(10);

        ResponseEntity<String> segundoCancelamento = restTemplate.postForEntity(
                url("/pedidos/" + pedido.id() + "/cancelamento"), null, String.class);
        assertThat(segundoCancelamento.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(segundoCancelamento.getBody()).contains("PEDIDO_JA_CANCELADO");
    }

    @Test
    void retorna422QuandoEstoqueInsuficiente() {
        Long produtoId = criarProduto("CAM-PED-2", "Camiseta Pedido 2", 10.00, 1);

        Map<String, Object> corpoPedido = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 5)));
        ResponseEntity<String> resposta = restTemplate.postForEntity(url("/pedidos"), corpoPedido, String.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
        assertThat(resposta.getBody()).contains("ESTOQUE_INSUFICIENTE");

        ProdutoResponse produto = restTemplate.getForEntity(url("/produtos/" + produtoId), ProdutoResponse.class).getBody();
        assertThat(produto.estoque()).isEqualTo(1);
    }

    @Test
    void retorna502QuandoFreteFalha() {
        Long produtoId = criarProduto("CAM-PED-3", "Camiseta Pedido 3", 10.00, 5);
        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes")).willReturn(aResponse().withStatus(500)));

        Map<String, Object> corpoPedido = Map.of(
                "cep", "01310100",
                "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 1)));
        ResponseEntity<String> resposta = restTemplate.postForEntity(url("/pedidos"), corpoPedido, String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resposta.getBody()).contains("FRETE_INDISPONIVEL");

        ProdutoResponse produto = restTemplate.getForEntity(url("/produtos/" + produtoId), ProdutoResponse.class).getBody();
        assertThat(produto.estoque()).isEqualTo(5);
    }
}
