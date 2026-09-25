package com.example.loja.catalogo.dto;

import com.example.loja.catalogo.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String sku,
        String nome,
        BigDecimal preco,
        Integer estoque,
        boolean ativo
) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.isAtivo()
        );
    }
}
