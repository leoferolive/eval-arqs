package com.example.loja.pedidos.dto;

import com.example.loja.pedidos.model.Pedido;
import com.example.loja.pedidos.model.PedidoStatus;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(
        Long id,
        String cep,
        PedidoStatus status,
        List<ItemResponse> itens,
        BigDecimal valorItens,
        BigDecimal valorFrete,
        Integer prazoEntregaDias,
        BigDecimal valorTotal
) {
    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCep(),
                pedido.getStatus(),
                pedido.getItens().stream().map(ItemResponse::de).toList(),
                pedido.getValorItens(),
                pedido.getValorFrete(),
                pedido.getPrazoEntregaDias(),
                pedido.getValorTotal()
        );
    }
}
