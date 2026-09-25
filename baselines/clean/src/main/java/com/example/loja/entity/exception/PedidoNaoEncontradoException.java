package com.example.loja.entity.exception;

public class PedidoNaoEncontradoException extends DomainException {

    public PedidoNaoEncontradoException(Long id) {
        super("PEDIDO_NAO_ENCONTRADO", "Pedido não encontrado: " + id);
    }
}
