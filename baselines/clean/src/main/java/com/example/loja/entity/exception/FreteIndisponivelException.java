package com.example.loja.entity.exception;

public class FreteIndisponivelException extends DomainException {

    public FreteIndisponivelException(String mensagem) {
        super("FRETE_INDISPONIVEL", mensagem);
    }

    public FreteIndisponivelException(String mensagem, Throwable causa) {
        super("FRETE_INDISPONIVEL", mensagem);
        initCause(causa);
    }
}
