package com.example.loja.domain.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Pedido {

    private Long id;
    private final String cep;
    private StatusPedido status;
    private final List<ItemPedido> itens;
    private final BigDecimal valorFrete;
    private final int prazoEntregaDias;

    public Pedido(Long id, String cep, StatusPedido status, List<ItemPedido> itens,
                  BigDecimal valorFrete, int prazoEntregaDias) {
        this.id = id;
        this.cep = cep;
        this.status = status;
        this.itens = itens;
        this.valorFrete = valorFrete;
        this.prazoEntregaDias = prazoEntregaDias;
    }

    public static Pedido criar(String cep, List<ItemPedido> itens, BigDecimal valorFrete, int prazoEntregaDias) {
        return new Pedido(null, cep, StatusPedido.CRIADO, itens, valorFrete, prazoEntregaDias);
    }

    public void cancelar() {
        if (status == StatusPedido.CANCELADO) {
            throw new PedidoJaCanceladoException(id);
        }
        status = StatusPedido.CANCELADO;
    }

    public BigDecimal getValorItens() {
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValorTotal() {
        return getValorItens().add(valorFrete).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public int getPrazoEntregaDias() {
        return prazoEntregaDias;
    }
}
