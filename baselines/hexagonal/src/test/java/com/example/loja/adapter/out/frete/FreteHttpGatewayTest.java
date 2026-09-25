package com.example.loja.adapter.out.frete;

import com.example.loja.application.port.out.FreteGateway;
import com.example.loja.domain.pedidos.FreteIndisponivelException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FreteHttpGatewayTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private FreteGateway freteGateway;

    @DynamicPropertySource
    static void configurarFreteUrl(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        registry.add("frete.api.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void resetStubs() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void stopServer() {
        wireMockServer.stop();
    }

    @Test
    void consultaFreteComSucesso() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\":25.50,\"prazoDias\":5}")));

        var frete = freteGateway.consultar("01310100");

        assertThat(frete.valor()).isEqualByComparingTo("25.50");
        assertThat(frete.prazoDias()).isEqualTo(5);
    }

    @Test
    void lancaExcecaoQuandoRespostaNao2xx() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes")).willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> freteGateway.consultar("01310100"))
                .isInstanceOf(FreteIndisponivelException.class);
    }

    @Test
    void lancaExcecaoQuandoDemoraMaisQue2Segundos() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(2500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\":25.50,\"prazoDias\":5}")));

        assertThatThrownBy(() -> freteGateway.consultar("01310100"))
                .isInstanceOf(FreteIndisponivelException.class);
    }
}
