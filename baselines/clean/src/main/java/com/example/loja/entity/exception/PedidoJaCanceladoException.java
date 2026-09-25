package com.example.loja.entity.exception;

public class PedidoJaCanceladoException extends DomainException {

    public PedidoJaCanceladoException(Long id) {
        super("PEDIDO_JA_CANCELADO", "Pedido já cancelado: " + id);
    }
}
