package com.example.loja.adapter.in.web.dto;

import com.example.loja.domain.catalogo.Produto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ProdutoResponse(Long id, String sku, String nome, BigDecimal preco, int estoque, boolean ativo) {

    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                produto.getPreco().setScale(2, RoundingMode.HALF_UP),
                produto.getEstoque(),
                produto.isAtivo()
        );
    }
}
