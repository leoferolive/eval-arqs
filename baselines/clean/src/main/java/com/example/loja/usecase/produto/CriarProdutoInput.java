package com.example.loja.usecase.produto;

import java.math.BigDecimal;

public record CriarProdutoInput(String sku, String nome, BigDecimal preco, int estoque) {
}
