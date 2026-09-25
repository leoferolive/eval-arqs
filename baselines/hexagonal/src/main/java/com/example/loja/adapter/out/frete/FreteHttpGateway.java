package com.example.loja.adapter.out.frete;

import com.example.loja.application.port.out.FreteGateway;
import com.example.loja.application.port.out.FreteInfo;
import com.example.loja.domain.pedidos.FreteIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class FreteHttpGateway implements FreteGateway {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final RestClient restClient;
    private final String baseUrl;

    public FreteHttpGateway(RestClient.Builder builder, @Value("${frete.api.url}") String baseUrl) {
        this.baseUrl = baseUrl;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(TIMEOUT);
        this.restClient = builder.requestFactory(requestFactory).build();
    }

    @Override
    public FreteInfo consultar(String cep) {
        try {
            FreteRespostaDto resposta = restClient.get()
                    .uri(baseUrl + "/fretes?cep={cep}", cep)
                    .retrieve()
                    .body(FreteRespostaDto.class);
            if (resposta == null) {
                throw new FreteIndisponivelException("resposta vazia da API de frete");
            }
            return new FreteInfo(resposta.valor(), resposta.prazoDias());
        } catch (RestClientException e) {
            throw new FreteIndisponivelException(e.getMessage());
        }
    }
}
