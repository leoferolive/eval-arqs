package com.example.loja.adapter.in.web.dto;

import com.example.loja.domain.pedidos.Pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record PedidoResponse(
        Long id,
        String cep,
        String status,
        List<ItemPedidoResponse> itens,
        BigDecimal valorItens,
        BigDecimal valorFrete,
        int prazoEntregaDias,
        BigDecimal valorTotal
) {

    public static PedidoResponse from(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(ItemPedidoResponse::from)
                .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCep(),
                pedido.getStatus().name(),
                itens,
                pedido.getValorItens(),
                pedido.getValorFrete().setScale(2, RoundingMode.HALF_UP),
                pedido.getPrazoEntregaDias(),
                pedido.getValorTotal()
        );
    }
}
