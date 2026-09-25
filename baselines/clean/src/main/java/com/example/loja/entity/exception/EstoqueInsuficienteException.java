package com.example.loja.entity.exception;

public class EstoqueInsuficienteException extends DomainException {

    public EstoqueInsuficienteException(Long produtoId) {
        super("ESTOQUE_INSUFICIENTE", "Estoque insuficiente para o produto: " + produtoId);
    }
}
