package com.example.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cep;
    @Enumerated(EnumType.STRING)
    private StatusPedido status = StatusPedido.CRIADO;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    private List<ItemPedido> itens = new ArrayList<>();
    private BigDecimal valorFrete;
    private int prazoEntregaDias;

    protected Pedido() {}

    public Pedido(String cep, List<ItemPedido> itens, BigDecimal valorFrete, int prazoEntregaDias) {
        this.cep = cep; this.itens = new ArrayList<>(itens); this.valorFrete = valorFrete; this.prazoEntregaDias = prazoEntregaDias;
    }

    public BigDecimal valorItens() {
        return itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cancelar() { this.status = StatusPedido.CANCELADO; }

    public Long getId() { return id; }
    public String getCep() { return cep; }
    public StatusPedido getStatus() { return status; }
    public List<ItemPedido> getItens() { return itens; }
    public BigDecimal getValorFrete() { return valorFrete; }
    public int getPrazoEntregaDias() { return prazoEntregaDias; }
}
