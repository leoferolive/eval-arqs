package com.example.loja.pedidos.client;

import com.example.loja.pedidos.exception.FreteIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class FreteClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final RestClient restClient;

    public FreteClient(RestClient.Builder builder, @Value("${frete.api.url}") String freteApiUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(TIMEOUT);
        this.restClient = builder.baseUrl(freteApiUrl).requestFactory(requestFactory).build();
    }

    public FreteRespostaDTO consultarFrete(String cep) {
        try {
            FreteRespostaDTO resposta = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/fretes").queryParam("cep", cep).build())
                    .retrieve()
                    .body(FreteRespostaDTO.class);
            if (resposta == null) {
                throw new FreteIndisponivelException();
            }
            return resposta;
        } catch (RestClientException e) {
            throw new FreteIndisponivelException();
        }
    }
}
