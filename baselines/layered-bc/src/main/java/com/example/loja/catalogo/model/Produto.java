package com.example.loja.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "produto", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = "sku"))
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, updatable = false)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque;

    @Column(nullable = false)
    private boolean ativo;

    protected Produto() {
    }

    public Produto(String sku, String nome, BigDecimal preco, Integer estoque) {
        this.sku = sku;
        this.nome = nome;
        this.preco = normalizar(preco);
        this.estoque = estoque;
        this.ativo = true;
    }

    private static BigDecimal normalizar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public void atualizar(String nome, BigDecimal preco, Integer estoque, boolean ativo) {
        this.nome = nome;
        this.preco = normalizar(preco);
        this.estoque = estoque;
        this.ativo = ativo;
    }

    public void debitarEstoque(int quantidade) {
        if (quantidade > this.estoque) {
            throw new IllegalStateException("Estoque insuficiente para o produto " + id);
        }
        this.estoque -= quantidade;
    }

    public void creditarEstoque(int quantidade) {
        this.estoque += quantidade;
    }

    public Long getId() {
        return id;
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

    public Integer getEstoque() {
        return estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produto produto)) return false;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
