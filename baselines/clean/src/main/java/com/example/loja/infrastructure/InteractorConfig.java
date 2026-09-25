package com.example.loja.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.loja.usecase.gateway.FreteGateway;
import com.example.loja.usecase.gateway.PedidoGateway;
import com.example.loja.usecase.gateway.ProdutoGateway;
import com.example.loja.usecase.pedido.BuscarPedidoInteractor;
import com.example.loja.usecase.pedido.CancelarPedidoInteractor;
import com.example.loja.usecase.pedido.CriarPedidoInteractor;
import com.example.loja.usecase.pedido.ListarPedidosInteractor;
import com.example.loja.usecase.produto.AtualizarProdutoInteractor;
import com.example.loja.usecase.produto.BuscarProdutoInteractor;
import com.example.loja.usecase.produto.CriarProdutoInteractor;
import com.example.loja.usecase.produto.ListarProdutosInteractor;
import com.example.loja.usecase.produto.RemoverProdutoInteractor;

@Configuration
public class InteractorConfig {

    @Bean
    public CriarProdutoInteractor criarProdutoInteractor(ProdutoGateway produtoGateway) {
        return new CriarProdutoInteractor(produtoGateway);
    }

    @Bean
    public ListarProdutosInteractor listarProdutosInteractor(ProdutoGateway produtoGateway) {
        return new ListarProdutosInteractor(produtoGateway);
    }

    @Bean
    public BuscarProdutoInteractor buscarProdutoInteractor(ProdutoGateway produtoGateway) {
        return new BuscarProdutoInteractor(produtoGateway);
    }

    @Bean
    public AtualizarProdutoInteractor atualizarProdutoInteractor(ProdutoGateway produtoGateway) {
        return new AtualizarProdutoInteractor(produtoGateway);
    }

    @Bean
    public RemoverProdutoInteractor removerProdutoInteractor(ProdutoGateway produtoGateway) {
        return new RemoverProdutoInteractor(produtoGateway);
    }

    @Bean
    public CriarPedidoInteractor criarPedidoInteractor(ProdutoGateway produtoGateway, PedidoGateway pedidoGateway,
            FreteGateway freteGateway) {
        return new CriarPedidoInteractor(produtoGateway, pedidoGateway, freteGateway);
    }

    @Bean
    public ListarPedidosInteractor listarPedidosInteractor(PedidoGateway pedidoGateway) {
        return new ListarPedidosInteractor(pedidoGateway);
    }

    @Bean
    public BuscarPedidoInteractor buscarPedidoInteractor(PedidoGateway pedidoGateway) {
        return new BuscarPedidoInteractor(pedidoGateway);
    }

    @Bean
    public CancelarPedidoInteractor cancelarPedidoInteractor(PedidoGateway pedidoGateway,
            ProdutoGateway produtoGateway) {
        return new CancelarPedidoInteractor(pedidoGateway, produtoGateway);
    }
}
