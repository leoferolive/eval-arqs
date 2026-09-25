package com.example.loja.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ItemPedidoTest {

    @Test
    void subtotalMultiplicaPrecoPorQuantidadeComEscalaDuasCasas() {
        ItemPedido item = new ItemPedido(1L, 3, new BigDecimal("19.999"));

        assertThat(item.subtotal()).isEqualByComparingTo("60.00");
        assertThat(item.getProdutoId()).isEqualTo(1L);
        assertThat(item.getQuantidade()).isEqualTo(3);
        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("19.999");
    }
}
