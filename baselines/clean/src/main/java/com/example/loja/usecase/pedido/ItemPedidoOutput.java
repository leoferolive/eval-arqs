package com.example.loja.usecase.pedido;

import java.math.BigDecimal;

import com.example.loja.entity.ItemPedido;

public record ItemPedidoOutput(Long produtoId, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {

    public static ItemPedidoOutput de(ItemPedido item) {
        return new ItemPedidoOutput(item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario(),
                item.subtotal());
    }
}
