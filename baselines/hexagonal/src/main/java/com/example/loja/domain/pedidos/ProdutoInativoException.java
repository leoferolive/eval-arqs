package com.example.loja.domain.pedidos;

public class ProdutoInativoException extends RuntimeException {

    public ProdutoInativoException(long produtoId) {
        super("Produto inativo: " + produtoId);
    }
}
