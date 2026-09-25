package com.example.loja.adapter.out.persistence;

import com.example.loja.domain.catalogo.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProdutoRepositoryAdapter.class)
class ProdutoRepositoryAdapterTest {

    @org.springframework.beans.factory.annotation.Autowired
    private ProdutoRepositoryAdapter adapter;

    @Test
    void salvaEBuscaPorId() {
        Produto salvo = adapter.salvar(Produto.novo("CAM-001", "Camiseta", new BigDecimal("59.90"), 10));

        assertThat(salvo.getId()).isNotNull();
        assertThat(adapter.buscarPorId(salvo.getId())).isPresent();
    }

    @Test
    void buscaPorSku() {
        adapter.salvar(Produto.novo("CAM-002", "Camiseta 2", BigDecimal.TEN, 5));

        assertThat(adapter.buscarPorSku("CAM-002")).isPresent();
        assertThat(adapter.buscarPorSku("INEXISTENTE")).isEmpty();
    }

    @Test
    void listaTodosOrdenadosPorId() {
        adapter.salvar(Produto.novo("A", "A", BigDecimal.ONE, 1));
        adapter.salvar(Produto.novo("B", "B", BigDecimal.ONE, 1));

        var lista = adapter.listarTodos();

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getId()).isLessThan(lista.get(1).getId());
    }

    @Test
    void existePorIdEExcluir() {
        Produto salvo = adapter.salvar(Produto.novo("CAM-003", "Camiseta 3", BigDecimal.ONE, 1));

        assertThat(adapter.existePorId(salvo.getId())).isTrue();

        adapter.excluir(salvo.getId());

        assertThat(adapter.existePorId(salvo.getId())).isFalse();
    }

    @Test
    void atualizaProdutoExistente() {
        Produto salvo = adapter.salvar(Produto.novo("CAM-004", "Camiseta 4", BigDecimal.ONE, 1));
        salvo.atualizar("Novo nome", new BigDecimal("2.00"), 3, false);

        Produto atualizado = adapter.salvar(salvo);

        assertThat(atualizado.getNome()).isEqualTo("Novo nome");
        assertThat(adapter.buscarPorId(salvo.getId()).orElseThrow().isAtivo()).isFalse();
    }
}
