package com.example.loja.catalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoUpdateRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal preco,
        @NotNull @PositiveOrZero Integer estoque,
        @NotNull Boolean ativo
) {
}
