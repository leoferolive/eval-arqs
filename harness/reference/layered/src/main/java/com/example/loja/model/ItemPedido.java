package com.example.loja.model;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class ItemPedido {
    private Long produtoId;
    private int quantidade;
    private BigDecimal precoUnitario;

    protected ItemPedido() {}

    public ItemPedido(Long produtoId, int quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId; this.quantidade = quantidade; this.precoUnitario = precoUnitario;
    }

    public BigDecimal subtotal() { return precoUnitario.multiply(BigDecimal.valueOf(quantidade)); }

    public Long getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
