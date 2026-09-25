package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String mensagem) {
        super(mensagem);
    }

    protected ApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public abstract HttpStatus getStatus();

    public abstract String getCodigo();
}
