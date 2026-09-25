package com.example.loja.pedidos.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class PedidoJaCanceladoException extends DomainException {

    public PedidoJaCanceladoException(Long id) {
        super(HttpStatus.CONFLICT, "PEDIDO_JA_CANCELADO", "Pedido já cancelado: " + id);
    }
}
