package com.example.loja.pedidos.dto;

import com.example.loja.pedidos.model.ItemPedido;

import java.math.BigDecimal;

public record ItemResponse(
        Long produtoId,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemResponse de(ItemPedido item) {
        return new ItemResponse(item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario(), item.getSubtotal());
    }
}
