package com.example.loja.usecase.produto;

import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.usecase.gateway.ProdutoGateway;

public class RemoverProdutoInteractor {

    private final ProdutoGateway produtoGateway;

    public RemoverProdutoInteractor(ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public void executar(Long id) {
        if (!produtoGateway.existePorId(id)) {
            throw new ProdutoNaoEncontradoException(id);
        }
        produtoGateway.remover(id);
    }
}
