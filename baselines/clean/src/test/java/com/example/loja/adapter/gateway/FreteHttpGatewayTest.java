package com.example.loja.adapter.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.http.HttpClient;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.example.loja.entity.exception.FreteIndisponivelException;
import com.example.loja.usecase.gateway.FreteInfo;
import com.github.tomakehurst.wiremock.WireMockServer;

class FreteHttpGatewayTest {

    private WireMockServer wireMockServer;
    private FreteHttpGateway gateway;

    @BeforeEach
    void iniciar() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(2));
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMockServer.port())
                .requestFactory(factory)
                .build();
        gateway = new FreteHttpGateway(restClient);
    }

    @AfterEach
    void parar() {
        wireMockServer.stop();
    }

    @Test
    void consultaComSucessoRetornaFreteInfo() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\": 25.50, \"prazoDias\": 5}")));

        FreteInfo info = gateway.consultar("01310100");

        assertThat(info.valor()).isEqualByComparingTo("25.50");
        assertThat(info.prazoDias()).isEqualTo(5);
    }

    @Test
    void respostaNao2xxLancaFreteIndisponivel() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes")).willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> gateway.consultar("01310100")).isInstanceOf(FreteIndisponivelException.class);
    }

    @Test
    void timeoutLancaFreteIndisponivel() {
        wireMockServer.stubFor(get(urlPathEqualTo("/fretes"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(3000)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valor\": 25.50, \"prazoDias\": 5}")));

        assertThatThrownBy(() -> gateway.consultar("01310100")).isInstanceOf(FreteIndisponivelException.class);
    }

    @Test
    void conexaoRecusadaLancaFreteIndisponivel() {
        wireMockServer.stop();

        assertThatThrownBy(() -> gateway.consultar("01310100")).isInstanceOf(FreteIndisponivelException.class);
    }
}
