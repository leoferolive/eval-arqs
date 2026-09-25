package com.example.loja.application.port.in;

import com.example.loja.domain.catalogo.Produto;

import java.math.BigDecimal;

public interface AtualizarProdutoUseCase {

    Produto atualizar(long id, AtualizarProdutoCommand command);

    record AtualizarProdutoCommand(String nome, BigDecimal preco, int estoque, boolean ativo) {
    }
}
