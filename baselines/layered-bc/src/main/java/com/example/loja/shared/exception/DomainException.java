package com.example.loja.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;

    protected DomainException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
