package com.example.loja.client;

import com.example.loja.exception.FreteIndisponivelException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FreteClient {

    private final RestClient freteRestClient;

    public FreteClient(RestClient freteRestClient) {
        this.freteRestClient = freteRestClient;
    }

    public FreteResponse consultarFrete(String cep) {
        try {
            FreteResponse resposta = freteRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/fretes").queryParam("cep", cep).build())
                    .retrieve()
                    .body(FreteResponse.class);
            if (resposta == null) {
                throw new FreteIndisponivelException("Resposta vazia da API de frete");
            }
            return resposta;
        } catch (RestClientException e) {
            throw new FreteIndisponivelException("Falha ao consultar a API de frete", e);
        }
    }
}
