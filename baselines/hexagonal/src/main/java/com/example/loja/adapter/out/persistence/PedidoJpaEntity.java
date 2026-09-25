package com.example.loja.adapter.out.persistence;

import com.example.loja.domain.pedidos.StatusPedido;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class PedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String cep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorFrete;

    @Column(nullable = false)
    private int prazoEntregaDias;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<ItemPedidoJpaEntity> itens = new ArrayList<>();

    protected PedidoJpaEntity() {
    }

    public PedidoJpaEntity(Long id, String cep, StatusPedido status, BigDecimal valorFrete, int prazoEntregaDias) {
        this.id = id;
        this.cep = cep;
        this.status = status;
        this.valorFrete = valorFrete;
        this.prazoEntregaDias = prazoEntregaDias;
    }

    public void adicionarItem(ItemPedidoJpaEntity item) {
        item.setPedido(this);
        itens.add(item);
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

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public int getPrazoEntregaDias() {
        return prazoEntregaDias;
    }

    public List<ItemPedidoJpaEntity> getItens() {
        return itens;
    }
}
