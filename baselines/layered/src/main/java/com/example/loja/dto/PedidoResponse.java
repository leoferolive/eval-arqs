package com.example.loja.dto;

import com.example.loja.model.StatusPedido;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(
        Long id,
        String cep,
        StatusPedido status,
        List<ItemPedidoResponse> itens,
        BigDecimal valorItens,
        BigDecimal valorFrete,
        Integer prazoEntregaDias,
        BigDecimal valorTotal
) {
}
