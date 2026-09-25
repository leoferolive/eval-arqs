package com.example.loja.entity.exception;

public class ProdutoNaoEncontradoException extends DomainException {

    public ProdutoNaoEncontradoException(Long id) {
        super("PRODUTO_NAO_ENCONTRADO", "Produto não encontrado: " + id);
    }
}
