package com.example.loja.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.loja.entity.exception.PedidoJaCanceladoException;

class PedidoTest {

    private List<ItemPedido> itens() {
        return List.of(
                new ItemPedido(1L, 2, new BigDecimal("59.90")),
                new ItemPedido(2L, 1, new BigDecimal("10.00")));
    }

    @Test
    void criacaoDefineStatusCriado() {
        Pedido pedido = new Pedido("01310100", itens(), new BigDecimal("25.50"), 5);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.getId()).isNull();
    }

    @Test
    void valorItensSomaSubtotais() {
        Pedido pedido = new Pedido("01310100", itens(), new BigDecimal("25.50"), 5);

        assertThat(pedido.getValorItens()).isEqualByComparingTo("129.80");
    }

    @Test
    void valorTotalSomaItensEFrete() {
        Pedido pedido = new Pedido("01310100", itens(), new BigDecimal("25.50"), 5);

        assertThat(pedido.getValorTotal()).isEqualByComparingTo("155.30");
    }

    @Test
    void cancelarAlteraStatusParaCancelado() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CRIADO, itens(), new BigDecimal("25.50"), 5);

        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void cancelarPedidoJaCanceladoLancaExcecao() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CANCELADO, itens(), new BigDecimal("25.50"), 5);

        assertThatThrownBy(pedido::cancelar).isInstanceOf(PedidoJaCanceladoException.class);
    }

    @Test
    void equalsEHashCodeBaseadosNoId() {
        Pedido p1 = new Pedido(1L, "01310100", StatusPedido.CRIADO, itens(), new BigDecimal("25.50"), 5);
        Pedido p2 = new Pedido(1L, "99999999", StatusPedido.CANCELADO, List.of(), BigDecimal.ZERO, 1);
        Pedido p3 = new Pedido(2L, "01310100", StatusPedido.CRIADO, itens(), new BigDecimal("25.50"), 5);

        assertThat(p1).isEqualTo(p2);
        assertThat(p1).hasSameHashCodeAs(p2);
        assertThat(p1).isNotEqualTo(p3);
        assertThat(p1).isNotEqualTo("outra coisa");
    }
}
