package com.example.loja.domain.catalogo;

import java.math.BigDecimal;

public class Produto {

    private Long id;
    private final String sku;
    private String nome;
    private BigDecimal preco;
    private int estoque;
    private boolean ativo;

    public Produto(Long id, String sku, String nome, BigDecimal preco, int estoque, boolean ativo) {
        this.id = id;
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
    }

    public static Produto novo(String sku, String nome, BigDecimal preco, int estoque) {
        return new Produto(null, sku, nome, preco, estoque, true);
    }

    public void atualizar(String nome, BigDecimal preco, int estoque, boolean ativo) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
    }

    public void decrementarEstoque(int quantidade) {
        this.estoque -= quantidade;
    }

    public void incrementarEstoque(int quantidade) {
        this.estoque += quantidade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
