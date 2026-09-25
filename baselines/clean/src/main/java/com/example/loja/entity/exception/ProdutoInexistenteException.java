package com.example.loja.entity.exception;

public class ProdutoInexistenteException extends DomainException {

    public ProdutoInexistenteException(Long produtoId) {
        super("PRODUTO_INEXISTENTE", "Produto inexistente: " + produtoId);
    }
}
