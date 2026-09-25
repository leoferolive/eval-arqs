package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class EstoqueInsuficienteException extends ApiException {

    public EstoqueInsuficienteException(Long produtoId) {
        super("Estoque insuficiente para o produto: " + produtoId);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

    @Override
    public String getCodigo() {
        return "ESTOQUE_INSUFICIENTE";
    }
}
