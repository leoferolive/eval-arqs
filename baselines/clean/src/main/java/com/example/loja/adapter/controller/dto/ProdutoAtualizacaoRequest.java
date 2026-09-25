package com.example.loja.adapter.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoAtualizacaoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal preco,
        @NotNull @Min(0) Integer estoque,
        @NotNull Boolean ativo) {
}
