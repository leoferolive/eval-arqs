package com.example.loja.domain.pedidos;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTest {

    @Test
    void calculaValoresAoCriar() {
        ItemPedido item1 = new ItemPedido(1L, 2, new BigDecimal("59.90"));
        ItemPedido item2 = new ItemPedido(2L, 1, new BigDecimal("10.00"));
        Pedido pedido = Pedido.criar("01310100", List.of(item1, item2), new BigDecimal("25.50"), 5);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.getValorItens()).isEqualByComparingTo("129.80");
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("155.30");
    }

    @Test
    void cancelaPedidoCriado() {
        Pedido pedido = Pedido.criar("01310100", List.of(new ItemPedido(1L, 1, BigDecimal.TEN)),
                BigDecimal.ONE, 3);

        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void naoPermiteCancelarPedidoJaCancelado() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CANCELADO,
                List.of(new ItemPedido(1L, 1, BigDecimal.TEN)), BigDecimal.ONE, 3);

        assertThatThrownBy(pedido::cancelar).isInstanceOf(PedidoJaCanceladoException.class);
    }

    @Test
    void calculaSubtotalDoItem() {
        ItemPedido item = new ItemPedido(1L, 3, new BigDecimal("10.005"));

        assertThat(item.getSubtotal()).isEqualByComparingTo("30.02");
        assertThat(item.getProdutoId()).isEqualTo(1L);
        assertThat(item.getQuantidade()).isEqualTo(3);
        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("10.005");
    }
}
