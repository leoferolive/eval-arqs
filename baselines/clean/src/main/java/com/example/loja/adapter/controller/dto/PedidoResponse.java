package com.example.loja.adapter.controller.dto;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(Long id, String cep, String status, List<ItemPedidoResponse> itens,
        BigDecimal valorItens, BigDecimal valorFrete, int prazoEntregaDias, BigDecimal valorTotal) {

    public record ItemPedidoResponse(Long produtoId, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
    }
}
