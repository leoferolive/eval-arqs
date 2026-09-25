package com.example.loja.domain.pedidos;

public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(long id) {
        super("Pedido não encontrado: " + id);
    }
}
