package com.example.loja.adapter.controller.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PedidoRequest(
        @NotNull @Pattern(regexp = "\\d{8}") String cep,
        @NotEmpty @Valid List<ItemPedidoRequest> itens) {

    public record ItemPedidoRequest(
            @NotNull Long produtoId,
            @NotNull @jakarta.validation.constraints.Min(1) Integer quantidade) {
    }
}
