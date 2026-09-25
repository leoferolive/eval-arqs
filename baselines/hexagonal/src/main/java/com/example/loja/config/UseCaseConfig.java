package com.example.loja.config;

import com.example.loja.application.port.out.FreteGateway;
import com.example.loja.application.port.out.PedidoRepositoryPort;
import com.example.loja.application.port.out.ProdutoRepositoryPort;
import com.example.loja.application.service.PedidoService;
import com.example.loja.application.service.ProdutoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProdutoService produtoService(ProdutoRepositoryPort produtoRepositoryPort) {
        return new ProdutoService(produtoRepositoryPort);
    }

    @Bean
    public PedidoService pedidoService(PedidoRepositoryPort pedidoRepositoryPort,
                                        ProdutoRepositoryPort produtoRepositoryPort,
                                        FreteGateway freteGateway) {
        return new PedidoService(pedidoRepositoryPort, produtoRepositoryPort, freteGateway);
    }
}
