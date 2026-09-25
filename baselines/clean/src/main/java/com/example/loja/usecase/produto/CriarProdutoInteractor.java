package com.example.loja.usecase.produto;

import com.example.loja.entity.Produto;
import com.example.loja.entity.exception.SkuDuplicadoException;
import com.example.loja.usecase.gateway.ProdutoGateway;

public class CriarProdutoInteractor {

    private final ProdutoGateway produtoGateway;

    public CriarProdutoInteractor(ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public ProdutoOutput executar(CriarProdutoInput input) {
        if (produtoGateway.existePorSku(input.sku())) {
            throw new SkuDuplicadoException(input.sku());
        }
        Produto produto = new Produto(input.sku(), input.nome(), input.preco(), input.estoque());
        Produto salvo = produtoGateway.salvar(produto);
        return ProdutoOutput.de(salvo);
    }
}
