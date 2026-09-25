package com.example.loja.client;

import com.example.loja.exception.FreteIndisponivelException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FreteClientTest {

    private WireMockServer wireMockServer;
    private FreteClient freteClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(2000);

        RestClient restClient = RestClient.builder()
                .baseUrl(wireMockServer.baseUrl())
                .requestFactory(requestFactory)
                .build();
        freteClient = new FreteClient(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void consultaFreteComSucesso() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\":25.50,\"prazoDias\":5}")));

        FreteResponse resposta = freteClient.consultarFrete("01310100");

        assertThat(resposta.valor()).isEqualByComparingTo("25.50");
        assertThat(resposta.prazoDias()).isEqualTo(5);
    }

    @Test
    void consultaFreteComRespostaNao2xxLancaExcecao() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes")).willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> freteClient.consultarFrete("01310100"))
                .isInstanceOf(FreteIndisponivelException.class);
    }

    @Test
    void consultaFreteComTimeoutLancaExcecao() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(3000)));

        assertThatThrownBy(() -> freteClient.consultarFrete("01310100"))
                .isInstanceOf(FreteIndisponivelException.class);
    }

    @Test
    void consultaFreteComErroDeConexaoLancaExcecao() {
        wireMockServer.stop();

        assertThatThrownBy(() -> freteClient.consultarFrete("01310100"))
                .isInstanceOf(FreteIndisponivelException.class);
    }
}
