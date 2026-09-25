package com.example.loja.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.example.loja.entity.exception.EstoqueInsuficienteException;

class ProdutoTest {

    @Test
    void criacaoDefineAtivoComoTrue() {
        Produto produto = new Produto("SKU-1", "Camiseta", new BigDecimal("59.90"), 10);

        assertThat(produto.isAtivo()).isTrue();
        assertThat(produto.getId()).isNull();
        assertThat(produto.getSku()).isEqualTo("SKU-1");
        assertThat(produto.getEstoque()).isEqualTo(10);
    }

    @Test
    void atualizarAlteraCamposMasNaoSku() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);

        produto.atualizar("Camiseta Nova", new BigDecimal("70.00"), 5, false);

        assertThat(produto.getNome()).isEqualTo("Camiseta Nova");
        assertThat(produto.getPreco()).isEqualByComparingTo("70.00");
        assertThat(produto.getEstoque()).isEqualTo(5);
        assertThat(produto.isAtivo()).isFalse();
        assertThat(produto.getSku()).isEqualTo("SKU-1");
    }

    @Test
    void decrementarEstoqueComQuantidadeSuficiente() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);

        produto.decrementarEstoque(4);

        assertThat(produto.getEstoque()).isEqualTo(6);
    }

    @Test
    void decrementarEstoqueComQuantidadeInsuficienteLancaExcecao() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 3, true);

        assertThatThrownBy(() -> produto.decrementarEstoque(4))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertThat(produto.getEstoque()).isEqualTo(3);
    }

    @Test
    void incrementarEstoqueSomaQuantidade() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 3, true);

        produto.incrementarEstoque(2);

        assertThat(produto.getEstoque()).isEqualTo(5);
    }

    @Test
    void equalsEHashCodeBaseadosNoId() {
        Produto p1 = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 3, true);
        Produto p2 = new Produto(1L, "SKU-2", "Outra", new BigDecimal("1.00"), 0, false);
        Produto p3 = new Produto(2L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 3, true);

        assertThat(p1).isEqualTo(p2);
        assertThat(p1).hasSameHashCodeAs(p2);
        assertThat(p1).isNotEqualTo(p3);
        assertThat(p1).isNotEqualTo("outra coisa");
        assertThat(p1).isEqualTo(p1);
    }
}
