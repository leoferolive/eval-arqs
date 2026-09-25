package com.example.loja.entity;

import java.math.BigDecimal;
import java.util.Objects;

import com.example.loja.entity.exception.EstoqueInsuficienteException;

public class Produto {

    private final Long id;
    private final String sku;
    private String nome;
    private BigDecimal preco;
    private int estoque;
    private boolean ativo;

    public Produto(String sku, String nome, BigDecimal preco, int estoque) {
        this(null, sku, nome, preco, estoque, true);
    }

    public Produto(Long id, String sku, String nome, BigDecimal preco, int estoque, boolean ativo) {
        this.id = id;
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
    }

    public void atualizar(String nome, BigDecimal preco, int estoque, boolean ativo) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
    }

    public void decrementarEstoque(int quantidade) {
        if (estoque < quantidade) {
            throw new EstoqueInsuficienteException(id);
        }
        estoque -= quantidade;
    }

    public void incrementarEstoque(int quantidade) {
        estoque += quantidade;
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

    public int getEstoque() {
        return estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produto produto)) {
            return false;
        }
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
