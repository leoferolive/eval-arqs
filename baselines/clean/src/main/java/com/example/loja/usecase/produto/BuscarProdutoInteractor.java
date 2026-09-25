package com.example.loja.usecase.produto;

import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.usecase.gateway.ProdutoGateway;

public class BuscarProdutoInteractor {

    private final ProdutoGateway produtoGateway;

    public BuscarProdutoInteractor(ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public ProdutoOutput executar(Long id) {
        return produtoGateway.buscarPorId(id)
                .map(ProdutoOutput::de)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }
}
