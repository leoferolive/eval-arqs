package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class PedidoJaCanceladoException extends ApiException {

    public PedidoJaCanceladoException(Long id) {
        super("Pedido já cancelado: " + id);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getCodigo() {
        return "PEDIDO_JA_CANCELADO";
    }
}
