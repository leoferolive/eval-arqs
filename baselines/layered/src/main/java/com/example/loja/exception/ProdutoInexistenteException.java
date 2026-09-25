package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class ProdutoInexistenteException extends ApiException {

    public ProdutoInexistenteException(Long produtoId) {
        super("Produto inexistente: " + produtoId);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

    @Override
    public String getCodigo() {
        return "PRODUTO_INEXISTENTE";
    }
}
