package com.example.loja.adapter.in.web.dto;

import com.example.loja.domain.pedidos.ItemPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ItemPedidoResponse(Long produtoId, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {

    public static ItemPedidoResponse from(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getProdutoId(),
                item.getQuantidade(),
                item.getPrecoUnitario().setScale(2, RoundingMode.HALF_UP),
                item.getSubtotal()
        );
    }
}
