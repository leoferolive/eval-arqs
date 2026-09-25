package com.example.loja.usecase.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.ItemPedido;
import com.example.loja.entity.Pedido;
import com.example.loja.entity.Produto;
import com.example.loja.entity.StatusPedido;
import com.example.loja.entity.exception.PedidoJaCanceladoException;
import com.example.loja.entity.exception.PedidoNaoEncontradoException;
import com.example.loja.usecase.gateway.PedidoGateway;
import com.example.loja.usecase.gateway.ProdutoGateway;

@ExtendWith(MockitoExtension.class)
class CancelarPedidoInteractorTest {

    @Mock
    private PedidoGateway pedidoGateway;
    @Mock
    private ProdutoGateway produtoGateway;

    @Test
    void cancelaPedidoEDevolveEstoque() {
        List<ItemPedido> itens = List.of(new ItemPedido(1L, 2, new BigDecimal("59.90")));
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CRIADO, itens, new BigDecimal("25.50"), 5);
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 8, true);

        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedido));
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(pedidoGateway.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        var interactor = new CancelarPedidoInteractor(pedidoGateway, produtoGateway);
        PedidoOutput output = interactor.executar(1L);

        assertThat(output.status()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10);
    }

    @Test
    void lancaExcecaoQuandoPedidoNaoExiste() {
        when(pedidoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        var interactor = new CancelarPedidoInteractor(pedidoGateway, produtoGateway);

        assertThatThrownBy(() -> interactor.executar(99L)).isInstanceOf(PedidoNaoEncontradoException.class);
    }

    @Test
    void lancaExcecaoQuandoPedidoJaCancelado() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CANCELADO, List.of(), BigDecimal.ZERO, 0);
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        var interactor = new CancelarPedidoInteractor(pedidoGateway, produtoGateway);

        assertThatThrownBy(() -> interactor.executar(1L)).isInstanceOf(PedidoJaCanceladoException.class);
    }
}
