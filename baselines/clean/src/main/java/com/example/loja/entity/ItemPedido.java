package com.example.loja.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ItemPedido {

    private final Long produtoId;
    private final int quantidade;
    private final BigDecimal precoUnitario;

    public ItemPedido(Long produtoId, int quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
}
