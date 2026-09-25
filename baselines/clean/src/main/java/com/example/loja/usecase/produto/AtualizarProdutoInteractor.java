package com.example.loja.usecase.produto;

import com.example.loja.entity.Produto;
import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.usecase.gateway.ProdutoGateway;

public class AtualizarProdutoInteractor {

    private final ProdutoGateway produtoGateway;

    public AtualizarProdutoInteractor(ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }

    public ProdutoOutput executar(AtualizarProdutoInput input) {
        Produto produto = produtoGateway.buscarPorId(input.id())
                .orElseThrow(() -> new ProdutoNaoEncontradoException(input.id()));
        produto.atualizar(input.nome(), input.preco(), input.estoque(), input.ativo());
        Produto salvo = produtoGateway.salvar(produto);
        return ProdutoOutput.de(salvo);
    }
}
