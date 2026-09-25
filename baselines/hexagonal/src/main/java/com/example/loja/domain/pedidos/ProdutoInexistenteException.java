package com.example.loja.domain.pedidos;

public class ProdutoInexistenteException extends RuntimeException {

    public ProdutoInexistenteException(long produtoId) {
        super("Produto inexistente: " + produtoId);
    }
}
