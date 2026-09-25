package com.example.loja.entity.exception;

public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
