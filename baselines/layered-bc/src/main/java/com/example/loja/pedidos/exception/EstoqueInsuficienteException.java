package com.example.loja.pedidos.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class EstoqueInsuficienteException extends DomainException {

    public EstoqueInsuficienteException(Long produtoId) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "ESTOQUE_INSUFICIENTE", "Estoque insuficiente para o produto: " + produtoId);
    }
}
