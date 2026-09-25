package com.example.loja.adapter.gateway;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.loja.entity.ItemPedido;
import com.example.loja.entity.Pedido;
import com.example.loja.entity.StatusPedido;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido")
public class PedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String cep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoJpaEntity> itens = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal valorFrete;

    @Column(nullable = false)
    private int prazoEntregaDias;

    protected PedidoJpaEntity() {
    }

    public PedidoJpaEntity(Long id, String cep, StatusPedido status, BigDecimal valorFrete, int prazoEntregaDias) {
        this.id = id;
        this.cep = cep;
        this.status = status;
        this.valorFrete = valorFrete;
        this.prazoEntregaDias = prazoEntregaDias;
    }

    public static PedidoJpaEntity de(Pedido pedido) {
        PedidoJpaEntity entity = new PedidoJpaEntity(pedido.getId(), pedido.getCep(), pedido.getStatus(),
                pedido.getValorFrete(), pedido.getPrazoEntregaDias());
        List<ItemPedidoJpaEntity> itensJpa = pedido.getItens().stream()
                .map(item -> ItemPedidoJpaEntity.de(entity, item))
                .toList();
        entity.itens.addAll(itensJpa);
        return entity;
    }

    public Pedido paraDominio() {
        List<ItemPedido> itensDominio = itens.stream().map(ItemPedidoJpaEntity::paraDominio).toList();
        return new Pedido(id, cep, status, itensDominio, valorFrete, prazoEntregaDias);
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

    public List<ItemPedidoJpaEntity> getItens() {
        return itens;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public int getPrazoEntregaDias() {
        return prazoEntregaDias;
    }
}
