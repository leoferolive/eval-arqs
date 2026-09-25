package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class PedidoNaoEncontradoException extends ApiException {

    public PedidoNaoEncontradoException(Long id) {
        super("Pedido não encontrado: " + id);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getCodigo() {
        return "PEDIDO_NAO_ENCONTRADO";
    }
}
