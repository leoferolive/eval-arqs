package com.example.loja.adapter.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.loja.entity.Produto;

@DataJpaTest
@Import(ProdutoGatewayImpl.class)
class ProdutoGatewayImplTest {

    @Autowired
    private ProdutoGatewayImpl gateway;

    @Test
    void salvarEBuscarPorId() {
        Produto salvo = gateway.salvar(new Produto("SKU-1", "Camiseta", new BigDecimal("59.90"), 10));

        assertThat(salvo.getId()).isNotNull();
        assertThat(gateway.buscarPorId(salvo.getId())).isPresent();
    }

    @Test
    void buscarPorIdInexistenteRetornaVazio() {
        assertThat(gateway.buscarPorId(999L)).isEmpty();
    }

    @Test
    void listarTodosOrdenaPorId() {
        Produto p1 = gateway.salvar(new Produto("SKU-1", "A", new BigDecimal("1.00"), 1));
        Produto p2 = gateway.salvar(new Produto("SKU-2", "B", new BigDecimal("2.00"), 2));

        var lista = gateway.listarTodos();

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getId()).isEqualTo(p1.getId());
        assertThat(lista.get(1).getId()).isEqualTo(p2.getId());
    }

    @Test
    void existePorSkuEExistePorId() {
        Produto salvo = gateway.salvar(new Produto("SKU-1", "Camiseta", new BigDecimal("59.90"), 10));

        assertThat(gateway.existePorSku("SKU-1")).isTrue();
        assertThat(gateway.existePorSku("OUTRO")).isFalse();
        assertThat(gateway.existePorId(salvo.getId())).isTrue();
        assertThat(gateway.existePorId(999L)).isFalse();
    }

    @Test
    void remover() {
        Produto salvo = gateway.salvar(new Produto("SKU-1", "Camiseta", new BigDecimal("59.90"), 10));

        gateway.remover(salvo.getId());

        assertThat(gateway.buscarPorId(salvo.getId())).isEmpty();
    }
}
