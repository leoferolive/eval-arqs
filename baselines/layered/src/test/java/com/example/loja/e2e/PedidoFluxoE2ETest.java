package com.example.loja.e2e;

import com.example.loja.dto.ErroResponse;
import com.example.loja.dto.ItemPedidoRequest;
import com.example.loja.dto.PedidoRequest;
import com.example.loja.dto.PedidoResponse;
import com.example.loja.dto.ProdutoRequest;
import com.example.loja.dto.ProdutoResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PedidoFluxoE2ETest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void iniciarWireMock() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void pararWireMock() {
        wireMockServer.stop();
    }

    @AfterEach
    void limparStubs() {
        wireMockServer.resetAll();
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("frete.api.url", () -> "http://localhost:" + wireMockServer.port());
    }

    private String url(String caminho) {
        return "http://localhost:" + port + caminho;
    }

    private ProdutoResponse criarProduto(String sku, BigDecimal preco, int estoque) {
        ProdutoRequest request = new ProdutoRequest(sku, "Produto " + sku, preco, estoque);
        ResponseEntity<ProdutoResponse> resposta = restTemplate.postForEntity(url("/produtos"), request, ProdutoResponse.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resposta.getBody();
    }

    @Test
    void fluxoCompletoDeCriacaoConsultaECancelamentoDePedido() {
        ProdutoResponse produto = criarProduto("SKU-E2E-1", new BigDecimal("50.00"), 10);

        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\":15.00,\"prazoDias\":4}")));

        PedidoRequest pedidoRequest = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(produto.id(), 3)));
        ResponseEntity<PedidoResponse> criacao = restTemplate.postForEntity(url("/pedidos"), pedidoRequest, PedidoResponse.class);

        assertThat(criacao.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PedidoResponse pedido = criacao.getBody();
        assertThat(pedido).isNotNull();
        assertThat(pedido.valorItens()).isEqualByComparingTo("150.00");
        assertThat(pedido.valorFrete()).isEqualByComparingTo("15.00");
        assertThat(pedido.valorTotal()).isEqualByComparingTo("165.00");
        assertThat(pedido.itens()).hasSize(1);

        ResponseEntity<ProdutoResponse> produtoAtualizado = restTemplate.getForEntity(url("/produtos/" + produto.id()), ProdutoResponse.class);
        assertThat(produtoAtualizado.getBody().estoque()).isEqualTo(7);

        ResponseEntity<PedidoResponse> consulta = restTemplate.getForEntity(url("/pedidos/" + pedido.id()), PedidoResponse.class);
        assertThat(consulta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consulta.getBody().itens()).hasSize(1);

        ResponseEntity<PedidoResponse[]> lista = restTemplate.getForEntity(url("/pedidos"), PedidoResponse[].class);
        assertThat(lista.getBody()).isNotEmpty();

        ResponseEntity<PedidoResponse> cancelamento = restTemplate.postForEntity(url("/pedidos/" + pedido.id() + "/cancelamento"), null, PedidoResponse.class);
        assertThat(cancelamento.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelamento.getBody().status().name()).isEqualTo("CANCELADO");

        ResponseEntity<ProdutoResponse> produtoAposCancelamento = restTemplate.getForEntity(url("/produtos/" + produto.id()), ProdutoResponse.class);
        assertThat(produtoAposCancelamento.getBody().estoque()).isEqualTo(10);

        ResponseEntity<ErroResponse> cancelamentoDuplicado = restTemplate.postForEntity(url("/pedidos/" + pedido.id() + "/cancelamento"), null, ErroResponse.class);
        assertThat(cancelamentoDuplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(cancelamentoDuplicado.getBody().codigo()).isEqualTo("PEDIDO_JA_CANCELADO");
    }

    @Test
    void criarPedidoComFreteIndisponivelNaoAlteraEstoque() {
        ProdutoResponse produto = criarProduto("SKU-E2E-2", new BigDecimal("20.00"), 5);

        wireMockServer.stubFor(get(urlPathEqualTo("/fretes")).willReturn(aResponse().withStatus(500)));

        PedidoRequest pedidoRequest = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(produto.id(), 2)));
        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/pedidos"), pedidoRequest, ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(resposta.getBody().codigo()).isEqualTo("FRETE_INDISPONIVEL");

        ResponseEntity<ProdutoResponse> produtoAposFalha = restTemplate.getForEntity(url("/produtos/" + produto.id()), ProdutoResponse.class);
        assertThat(produtoAposFalha.getBody().estoque()).isEqualTo(5);
    }

    @Test
    void criarPedidoComProdutoInexistenteRetorna422() {
        PedidoRequest pedidoRequest = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(999999L, 1)));
        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/pedidos"), pedidoRequest, ErroResponse.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
        assertThat(resposta.getBody().codigo()).isEqualTo("PRODUTO_INEXISTENTE");
    }

    @Test
    void buscarPedidoInexistenteRetorna404() {
        ResponseEntity<ErroResponse> resposta = restTemplate.getForEntity(url("/pedidos/999999"), ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().codigo()).isEqualTo("PEDIDO_NAO_ENCONTRADO");
    }
}
