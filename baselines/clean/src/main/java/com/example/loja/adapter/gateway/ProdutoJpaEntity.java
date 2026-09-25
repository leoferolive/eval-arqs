package com.example.loja.adapter.gateway;

import java.math.BigDecimal;

import com.example.loja.entity.Produto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "produto")
public class ProdutoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private int estoque;

    @Column(nullable = false)
    private boolean ativo;

    protected ProdutoJpaEntity() {
    }

    public ProdutoJpaEntity(Long id, String sku, String nome, BigDecimal preco, int estoque, boolean ativo) {
        this.id = id;
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.ativo = ativo;
    }

    public static ProdutoJpaEntity de(Produto produto) {
        return new ProdutoJpaEntity(produto.getId(), produto.getSku(), produto.getNome(), produto.getPreco(),
                produto.getEstoque(), produto.isAtivo());
    }

    public Produto paraDominio() {
        return new Produto(id, sku, nome, preco, estoque, ativo);
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
}
