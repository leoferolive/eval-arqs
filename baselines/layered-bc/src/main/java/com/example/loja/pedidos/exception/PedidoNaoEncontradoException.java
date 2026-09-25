package com.example.loja.pedidos.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PedidoNaoEncontradoException extends DomainException {

    public PedidoNaoEncontradoException(Long id) {
        super(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO", "Pedido não encontrado: " + id);
    }
}
