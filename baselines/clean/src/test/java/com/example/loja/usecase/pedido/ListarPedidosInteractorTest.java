package com.example.loja.usecase.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.Pedido;
import com.example.loja.entity.StatusPedido;
import com.example.loja.usecase.gateway.PedidoGateway;

@ExtendWith(MockitoExtension.class)
class ListarPedidosInteractorTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @Test
    void retornaListaDePedidos() {
        when(pedidoGateway.listarTodos()).thenReturn(List.of(
                new Pedido(1L, "01310100", StatusPedido.CRIADO, List.of(), new BigDecimal("25.50"), 5),
                new Pedido(2L, "99999999", StatusPedido.CANCELADO, List.of(), BigDecimal.ZERO, 1)));

        var interactor = new ListarPedidosInteractor(pedidoGateway);
        List<PedidoOutput> output = interactor.executar();

        assertThat(output).hasSize(2);
        assertThat(output.get(0).id()).isEqualTo(1L);
        assertThat(output.get(1).id()).isEqualTo(2L);
    }
}
