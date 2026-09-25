package com.example.loja.domain.pedidos;

public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(long produtoId) {
        super("Estoque insuficiente para o produto: " + produtoId);
    }
}
