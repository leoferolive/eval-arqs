package com.example.loja.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private static final int TIMEOUT_MS = 2000;

    @Bean
    public RestClient freteRestClient(RestClient.Builder builder, @Value("${frete.api.url}") String freteApiUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_MS);
        requestFactory.setReadTimeout(TIMEOUT_MS);
        return builder
                .baseUrl(freteApiUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
