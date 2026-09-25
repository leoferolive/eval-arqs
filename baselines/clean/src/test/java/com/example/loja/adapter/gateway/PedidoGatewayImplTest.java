package com.example.loja.adapter.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.loja.entity.ItemPedido;
import com.example.loja.entity.Pedido;

@DataJpaTest
@Import(PedidoGatewayImpl.class)
class PedidoGatewayImplTest {

    @Autowired
    private PedidoGatewayImpl gateway;

    private Pedido novoPedido() {
        List<ItemPedido> itens = List.of(new ItemPedido(1L, 2, new BigDecimal("59.90")));
        return new Pedido("01310100", itens, new BigDecimal("25.50"), 5);
    }

    @Test
    void salvarEBuscarPorIdCarregaItensSemLazyInitializationException() {
        Pedido salvo = gateway.salvar(novoPedido());

        var encontrado = gateway.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getItens()).hasSize(1);
        assertThat(encontrado.get().getItens().get(0).getProdutoId()).isEqualTo(1L);
        assertThat(encontrado.get().getValorTotal()).isEqualByComparingTo("145.30");
    }

    @Test
    void buscarPorIdInexistenteRetornaVazio() {
        assertThat(gateway.buscarPorId(999L)).isEmpty();
    }

    @Test
    void listarTodosCarregaItensEOrdenaPorId() {
        Pedido p1 = gateway.salvar(novoPedido());
        Pedido p2 = gateway.salvar(novoPedido());

        var lista = gateway.listarTodos();

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getId()).isEqualTo(p1.getId());
        assertThat(lista.get(1).getId()).isEqualTo(p2.getId());
        assertThat(lista.get(0).getItens()).hasSize(1);
    }
}
