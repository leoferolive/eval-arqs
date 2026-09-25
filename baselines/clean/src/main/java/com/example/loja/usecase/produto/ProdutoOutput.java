package com.example.loja.usecase.produto;

import java.math.BigDecimal;

import com.example.loja.entity.Produto;

public record ProdutoOutput(Long id, String sku, String nome, BigDecimal preco, int estoque, boolean ativo) {

    public static ProdutoOutput de(Produto produto) {
        return new ProdutoOutput(produto.getId(), produto.getSku(), produto.getNome(), produto.getPreco(),
                produto.getEstoque(), produto.isAtivo());
    }
}
