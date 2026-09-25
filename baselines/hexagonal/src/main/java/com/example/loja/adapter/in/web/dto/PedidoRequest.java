package com.example.loja.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record PedidoRequest(
        @NotNull @Pattern(regexp = "\\d{8}", message = "cep deve conter exatamente 8 dígitos") String cep,
        @NotEmpty @Valid List<ItemPedidoRequest> itens
) {
}
