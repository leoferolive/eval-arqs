package com.example.loja.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotNull Long produtoId,
        @NotNull @Min(1) Integer quantidade
) {
}
