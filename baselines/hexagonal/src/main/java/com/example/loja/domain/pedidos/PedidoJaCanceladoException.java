package com.example.loja.domain.pedidos;

public class PedidoJaCanceladoException extends RuntimeException {

    public PedidoJaCanceladoException(long id) {
        super("Pedido já cancelado: " + id);
    }
}
