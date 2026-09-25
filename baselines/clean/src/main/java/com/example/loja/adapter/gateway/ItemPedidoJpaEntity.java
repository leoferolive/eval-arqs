package com.example.loja.adapter.gateway;

import java.math.BigDecimal;

import com.example.loja.entity.ItemPedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_pedido")
public class ItemPedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpaEntity pedido;

    @Column(nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private BigDecimal precoUnitario;

    protected ItemPedidoJpaEntity() {
    }

    public ItemPedidoJpaEntity(PedidoJpaEntity pedido, Long produtoId, int quantidade, BigDecimal precoUnitario) {
        this.pedido = pedido;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public static ItemPedidoJpaEntity de(PedidoJpaEntity pedido, ItemPedido item) {
        return new ItemPedidoJpaEntity(pedido, item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario());
    }

    public ItemPedido paraDominio() {
        return new ItemPedido(produtoId, quantidade, precoUnitario);
    }

    public Long getId() {
        return id;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
}
