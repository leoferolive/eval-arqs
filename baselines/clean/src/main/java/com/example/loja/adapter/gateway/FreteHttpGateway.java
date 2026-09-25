package com.example.loja.adapter.gateway;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.loja.entity.exception.FreteIndisponivelException;
import com.example.loja.usecase.gateway.FreteGateway;
import com.example.loja.usecase.gateway.FreteInfo;

@Component
public class FreteHttpGateway implements FreteGateway {

    private final RestClient restClient;

    public FreteHttpGateway(RestClient freteRestClient) {
        this.restClient = freteRestClient;
    }

    @Override
    public FreteInfo consultar(String cep) {
        try {
            FreteResponse resposta = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/fretes").queryParam("cep", cep).build())
                    .retrieve()
                    .body(FreteResponse.class);
            if (resposta == null) {
                throw new FreteIndisponivelException("Resposta vazia da API de frete");
            }
            return new FreteInfo(resposta.valor(), resposta.prazoDias());
        } catch (RestClientException e) {
            throw new FreteIndisponivelException("Falha ao consultar a API de frete", e);
        }
    }
}
