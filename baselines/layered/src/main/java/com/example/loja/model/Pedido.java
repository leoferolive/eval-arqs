package com.example.loja.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String cep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorItens;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorFrete;

    @Column(nullable = false)
    private Integer prazoEntregaDias;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;

    protected Pedido() {
    }

    public Pedido(String cep, BigDecimal valorItens, BigDecimal valorFrete, Integer prazoEntregaDias,
                  BigDecimal valorTotal) {
        this.cep = cep;
        this.status = StatusPedido.CRIADO;
        this.valorItens = valorItens;
        this.valorFrete = valorFrete;
        this.prazoEntregaDias = prazoEntregaDias;
        this.valorTotal = valorTotal;
    }

    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
    }

    public Long getId() {
        return id;
    }

    public String getCep() {
        return cep;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public BigDecimal getValorItens() {
        return valorItens;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public Integer getPrazoEntregaDias() {
        return prazoEntregaDias;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void cancelar() {
        this.status = StatusPedido.CANCELADO;
    }
}
