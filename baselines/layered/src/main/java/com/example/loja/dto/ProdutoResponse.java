package com.example.loja.dto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String sku,
        String nome,
        BigDecimal preco,
        Integer estoque,
        boolean ativo
) {
}
