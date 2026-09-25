package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class ProdutoInativoException extends ApiException {

    public ProdutoInativoException(Long produtoId) {
        super("Produto inativo: " + produtoId);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

    @Override
    public String getCodigo() {
        return "PRODUTO_INATIVO";
    }
}
