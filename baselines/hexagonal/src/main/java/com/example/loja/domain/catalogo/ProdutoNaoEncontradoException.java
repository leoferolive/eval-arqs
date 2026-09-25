package com.example.loja.domain.catalogo;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(long id) {
        super("Produto não encontrado: " + id);
    }
}
