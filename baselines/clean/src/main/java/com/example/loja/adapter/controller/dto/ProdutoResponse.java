package com.example.loja.adapter.controller.dto;

import java.math.BigDecimal;

public record ProdutoResponse(Long id, String sku, String nome, BigDecimal preco, int estoque, boolean ativo) {
}
