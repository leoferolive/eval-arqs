package com.example.loja.adapter.out.persistence;

import com.example.loja.domain.pedidos.ItemPedido;
import com.example.loja.domain.pedidos.Pedido;
import com.example.loja.domain.pedidos.StatusPedido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PedidoRepositoryAdapter.class)
class PedidoRepositoryAdapterTest {

    @Autowired
    private PedidoRepositoryAdapter adapter;

    @Test
    void salvaEBuscaComItens() {
        ItemPedido item = new ItemPedido(1L, 2, new BigDecimal("59.90"));
        Pedido pedido = Pedido.criar("01310100", List.of(item), new BigDecimal("25.50"), 5);

        Pedido salvo = adapter.salvar(pedido);

        assertThat(salvo.getId()).isNotNull();
        Pedido encontrado = adapter.buscarPorId(salvo.getId()).orElseThrow();
        assertThat(encontrado.getItens()).hasSize(1);
        assertThat(encontrado.getItens().get(0).getProdutoId()).isEqualTo(1L);
        assertThat(encontrado.getValorTotal()).isEqualByComparingTo("145.30");
    }

    @Test
    void listaTodosOrdenadosPorId() {
        adapter.salvar(Pedido.criar("01310100", List.of(new ItemPedido(1L, 1, BigDecimal.TEN)), BigDecimal.ONE, 1));
        adapter.salvar(Pedido.criar("01310100", List.of(new ItemPedido(1L, 1, BigDecimal.TEN)), BigDecimal.ONE, 1));

        var lista = adapter.listarTodos();

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getId()).isLessThan(lista.get(1).getId());
    }

    @Test
    void atualizaStatusParaCancelado() {
        Pedido salvo = adapter.salvar(Pedido.criar("01310100",
                List.of(new ItemPedido(1L, 1, BigDecimal.TEN)), BigDecimal.ONE, 1));

        salvo.cancelar();
        adapter.salvar(salvo);

        Pedido encontrado = adapter.buscarPorId(salvo.getId()).orElseThrow();
        assertThat(encontrado.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }
}
