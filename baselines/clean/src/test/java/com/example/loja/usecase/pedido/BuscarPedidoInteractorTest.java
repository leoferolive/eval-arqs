package com.example.loja.usecase.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.Pedido;
import com.example.loja.entity.StatusPedido;
import com.example.loja.entity.exception.PedidoNaoEncontradoException;
import com.example.loja.usecase.gateway.PedidoGateway;

@ExtendWith(MockitoExtension.class)
class BuscarPedidoInteractorTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @Test
    void retornaPedidoQuandoExiste() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CRIADO, List.of(), new BigDecimal("25.50"), 5);
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        var interactor = new BuscarPedidoInteractor(pedidoGateway);
        PedidoOutput output = interactor.executar(1L);

        assertThat(output.id()).isEqualTo(1L);
    }

    @Test
    void lancaExcecaoQuandoNaoExiste() {
        when(pedidoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        var interactor = new BuscarPedidoInteractor(pedidoGateway);

        assertThatThrownBy(() -> interactor.executar(99L)).isInstanceOf(PedidoNaoEncontradoException.class);
    }
}
