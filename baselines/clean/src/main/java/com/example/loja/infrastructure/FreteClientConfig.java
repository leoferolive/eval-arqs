package com.example.loja.infrastructure;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class FreteClientConfig {

    @Value("${frete.api.url}")
    private String freteApiUrl;

    @Bean
    public RestClient freteRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(2));
        return RestClient.builder()
                .baseUrl(freteApiUrl)
                .requestFactory(factory)
                .build();
    }
}
