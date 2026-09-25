package com.example.loja.usecase.pedido;

import java.math.BigDecimal;
import java.util.List;

import com.example.loja.entity.Pedido;
import com.example.loja.entity.StatusPedido;

public record PedidoOutput(Long id, String cep, StatusPedido status, List<ItemPedidoOutput> itens,
        BigDecimal valorItens, BigDecimal valorFrete, int prazoEntregaDias, BigDecimal valorTotal) {

    public static PedidoOutput de(Pedido pedido) {
        List<ItemPedidoOutput> itens = pedido.getItens().stream().map(ItemPedidoOutput::de).toList();
        return new PedidoOutput(pedido.getId(), pedido.getCep(), pedido.getStatus(), itens, pedido.getValorItens(),
                pedido.getValorFrete(), pedido.getPrazoEntregaDias(), pedido.getValorTotal());
    }
}
