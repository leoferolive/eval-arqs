package com.example.loja.application.port.in;

import com.example.loja.domain.catalogo.Produto;

import java.math.BigDecimal;

public interface CriarProdutoUseCase {

    Produto criar(NovoProdutoCommand command);

    record NovoProdutoCommand(String sku, String nome, BigDecimal preco, int estoque) {
    }
}
