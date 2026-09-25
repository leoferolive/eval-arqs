package com.example.loja.domain.catalogo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoTest {

    @Test
    void criaProdutoNovoComoAtivo() {
        Produto produto = Produto.novo("CAM-001", "Camiseta", new BigDecimal("59.90"), 10);

        assertThat(produto.isAtivo()).isTrue();
        assertThat(produto.getId()).isNull();
        assertThat(produto.getSku()).isEqualTo("CAM-001");
        assertThat(produto.getNome()).isEqualTo("Camiseta");
        assertThat(produto.getPreco()).isEqualByComparingTo("59.90");
        assertThat(produto.getEstoque()).isEqualTo(10);
    }

    @Test
    void atualizaCampos() {
        Produto produto = Produto.novo("CAM-001", "Camiseta", new BigDecimal("59.90"), 10);

        produto.atualizar("Camiseta P", new BigDecimal("49.90"), 5, false);

        assertThat(produto.getNome()).isEqualTo("Camiseta P");
        assertThat(produto.getPreco()).isEqualByComparingTo("49.90");
        assertThat(produto.getEstoque()).isEqualTo(5);
        assertThat(produto.isAtivo()).isFalse();
    }

    @Test
    void decrementaEIncrementaEstoque() {
        Produto produto = Produto.novo("CAM-001", "Camiseta", new BigDecimal("59.90"), 10);

        produto.decrementarEstoque(3);
        assertThat(produto.getEstoque()).isEqualTo(7);

        produto.incrementarEstoque(3);
        assertThat(produto.getEstoque()).isEqualTo(10);
    }

    @Test
    void permiteAlterarId() {
        Produto produto = Produto.novo("CAM-001", "Camiseta", new BigDecimal("59.90"), 10);

        produto.setId(1L);

        assertThat(produto.getId()).isEqualTo(1L);
    }
}
