package com.example.loja.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank @Size(max = 30) String sku,
        @NotBlank @Size(max = 120) String nome,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal preco,
        @NotNull @Min(0) Integer estoque
) {
}
