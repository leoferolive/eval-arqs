package com.example.loja.usecase.produto;

import java.util.List;

import com.example.loja.usecase.gateway.ProdutoGateway;

public class ListarProdutosInteractor {

    private final ProdutoGateway produtoGateway;

    public ListarProdutosInteractor(ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public List<ProdutoOutput> executar() {
        return produtoGateway.listarTodos().stream().map(ProdutoOutput::de).toList();
    }
}
