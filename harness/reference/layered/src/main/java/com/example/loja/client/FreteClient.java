package com.example.loja.client;

import com.example.loja.dto.Dtos.Frete;
import com.example.loja.exception.NegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FreteClient {
    private final RestClient freteRestClient;

    public FreteClient(RestClient freteRestClient) { this.freteRestClient = freteRestClient; }

    public Frete cotar(String cep) {
        try {
            Frete frete = freteRestClient.get().uri("/fretes?cep={cep}", cep).retrieve().body(Frete.class);
            if (frete == null || frete.valor() == null) throw new RestClientException("resposta vazia");
            return frete;
        } catch (RestClientException e) {
            throw new NegocioException(HttpStatus.BAD_GATEWAY, "FRETE_INDISPONIVEL", "API de frete indisponível");
        }
    }
}
