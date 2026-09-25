package com.example.loja.pedidos.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class FreteIndisponivelException extends DomainException {

    public FreteIndisponivelException() {
        super(HttpStatus.BAD_GATEWAY, "FRETE_INDISPONIVEL", "API de frete indisponível");
    }
}
