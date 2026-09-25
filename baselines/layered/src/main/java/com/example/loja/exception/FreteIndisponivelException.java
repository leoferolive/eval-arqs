package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class FreteIndisponivelException extends ApiException {

    public FreteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FreteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_GATEWAY;
    }

    @Override
    public String getCodigo() {
        return "FRETE_INDISPONIVEL";
    }
}
