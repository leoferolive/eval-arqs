package com.example.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 30)
    private String sku;
    private String nome;
    private BigDecimal preco;
    private int estoque;
    private boolean ativo = true;

    protected Produto() {}

    public Produto(String sku, String nome, BigDecimal preco, int estoque) {
        this.sku = sku; this.nome = nome; this.preco = preco; this.estoque = estoque;
    }

    public void atualizar(String nome, BigDecimal preco, int estoque, boolean ativo) {
        this.nome = nome; this.preco = preco; this.estoque = estoque; this.ativo = ativo;
    }

    public void ajustarEstoque(int delta) { this.estoque += delta; }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public int getEstoque() { return estoque; }
    public boolean isAtivo() { return ativo; }
}
