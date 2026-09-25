package com.example.loja.usecase.produto;

import java.math.BigDecimal;

public record AtualizarProdutoInput(Long id, String nome, BigDecimal preco, int estoque, boolean ativo) {
}
