package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class ProdutoNaoEncontradoException extends ApiException {

    public ProdutoNaoEncontradoException(Long id) {
        super("Produto não encontrado: " + id);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getCodigo() {
        return "PRODUTO_NAO_ENCONTRADO";
    }
}
