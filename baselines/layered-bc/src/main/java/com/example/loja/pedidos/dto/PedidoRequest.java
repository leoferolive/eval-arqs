package com.example.loja.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record PedidoRequest(
        @Pattern(regexp = "\\d{8}", message = "cep deve conter exatamente 8 dígitos") String cep,
        @NotEmpty @Valid List<ItemRequest> itens
) {
}
