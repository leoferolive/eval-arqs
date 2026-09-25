package com.example.loja.entity.exception;

public class ProdutoInativoException extends DomainException {

    public ProdutoInativoException(Long produtoId) {
        super("PRODUTO_INATIVO", "Produto inativo: " + produtoId);
    }
}
