package com.example.loja.pedidos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private PedidoStatus status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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

    public Pedido(String cep, List<ItemPedido> itens, BigDecimal valorFrete, Integer prazoEntregaDias) {
        this.cep = cep;
        this.status = PedidoStatus.CRIADO;
        itens.forEach(this::adicionarItem);
        this.valorItens = this.itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        this.valorFrete = valorFrete.setScale(2, RoundingMode.HALF_UP);
        this.prazoEntregaDias = prazoEntregaDias;
        this.valorTotal = this.valorItens.add(this.valorFrete).setScale(2, RoundingMode.HALF_UP);
    }

    private void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
    }

    public void cancelar() {
        this.status = PedidoStatus.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public String getCep() {
        return cep;
    }

    public PedidoStatus getStatus() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido pedido)) return false;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
