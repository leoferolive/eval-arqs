package com.example.loja.application.service;

import com.example.loja.application.port.in.CriarPedidoUseCase.ItemCommand;
import com.example.loja.application.port.in.CriarPedidoUseCase.NovoPedidoCommand;
import com.example.loja.application.port.out.FreteGateway;
import com.example.loja.application.port.out.FreteInfo;
import com.example.loja.application.port.out.PedidoRepositoryPort;
import com.example.loja.application.port.out.ProdutoRepositoryPort;
import com.example.loja.domain.catalogo.Produto;
import com.example.loja.domain.pedidos.EstoqueInsuficienteException;
import com.example.loja.domain.pedidos.FreteIndisponivelException;
import com.example.loja.domain.pedidos.ItemPedido;
import com.example.loja.domain.pedidos.Pedido;
import com.example.loja.domain.pedidos.PedidoJaCanceladoException;
import com.example.loja.domain.pedidos.PedidoNaoEncontradoException;
import com.example.loja.domain.pedidos.ProdutoInativoException;
import com.example.loja.domain.pedidos.ProdutoInexistenteException;
import com.example.loja.domain.pedidos.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepositoryPort pedidoRepositoryPort;
    @Mock
    private ProdutoRepositoryPort produtoRepositoryPort;
    @Mock
    private FreteGateway freteGateway;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoRepositoryPort, produtoRepositoryPort, freteGateway);
    }

    @Test
    void criaPedidoDecrementandoEstoqueEConsultandoFrete() {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(freteGateway.consultar("01310100")).thenReturn(new FreteInfo(new BigDecimal("25.50"), 5));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepositoryPort.salvar(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Pedido pedido = pedidoService.criar(new NovoPedidoCommand("01310100", List.of(new ItemCommand(1L, 2))));

        assertThat(pedido.getId()).isEqualTo(1L);
        assertThat(pedido.getValorItens()).isEqualByComparingTo("119.80");
        assertThat(pedido.getValorFrete()).isEqualByComparingTo("25.50");
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("145.30");
        assertThat(produto.getEstoque()).isEqualTo(8);
        verify(produtoRepositoryPort).salvar(produto);
    }

    @Test
    void falhaQuandoProdutoNaoExiste() {
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(new NovoPedidoCommand("01310100", List.of(new ItemCommand(1L, 1)))))
                .isInstanceOf(ProdutoInexistenteException.class);
        verify(freteGateway, never()).consultar(any());
    }

    @Test
    void falhaQuandoProdutoInativo() {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", BigDecimal.TEN, 10, false);
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> pedidoService.criar(new NovoPedidoCommand("01310100", List.of(new ItemCommand(1L, 1)))))
                .isInstanceOf(ProdutoInativoException.class);
        verify(freteGateway, never()).consultar(any());
    }

    @Test
    void falhaQuandoEstoqueInsuficiente() {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", BigDecimal.TEN, 1, true);
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> pedidoService.criar(new NovoPedidoCommand("01310100", List.of(new ItemCommand(1L, 5)))))
                .isInstanceOf(EstoqueInsuficienteException.class);
        verify(freteGateway, never()).consultar(any());
    }

    @Test
    void falhaQuandoFreteIndisponivelENaoDecrementaEstoque() {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", BigDecimal.TEN, 10, true);
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(freteGateway.consultar("01310100")).thenThrow(new FreteIndisponivelException("timeout"));

        assertThatThrownBy(() -> pedidoService.criar(new NovoPedidoCommand("01310100", List.of(new ItemCommand(1L, 2)))))
                .isInstanceOf(FreteIndisponivelException.class);

        assertThat(produto.getEstoque()).isEqualTo(10);
        verify(produtoRepositoryPort, never()).salvar(any());
        verify(pedidoRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscaPedidoPorId() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CRIADO, List.of(), BigDecimal.ONE, 1);
        when(pedidoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        assertThat(pedidoService.buscarPorId(1L)).isEqualTo(pedido);
    }

    @Test
    void falhaAoBuscarPedidoInexistente() {
        when(pedidoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPorId(1L)).isInstanceOf(PedidoNaoEncontradoException.class);
    }

    @Test
    void listaTodosOsPedidos() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CRIADO, List.of(), BigDecimal.ONE, 1);
        when(pedidoRepositoryPort.listarTodos()).thenReturn(List.of(pedido));

        assertThat(pedidoService.listarTodos()).containsExactly(pedido);
    }

    @Test
    void cancelaPedidoDevolvendoEstoque() {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", BigDecimal.TEN, 3, true);
        ItemPedido item = new ItemPedido(1L, 2, BigDecimal.TEN);
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CRIADO, List.of(item), BigDecimal.ONE, 1);
        when(pedidoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepositoryPort.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = pedidoService.cancelar(1L);

        assertThat(cancelado.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(5);
        verify(pedidoRepositoryPort, times(1)).salvar(pedido);
    }

    @Test
    void falhaAoCancelarPedidoJaCancelado() {
        Pedido pedido = new Pedido(1L, "01310100", StatusPedido.CANCELADO, List.of(), BigDecimal.ONE, 1);
        when(pedidoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelar(1L)).isInstanceOf(PedidoJaCanceladoException.class);
        verify(produtoRepositoryPort, never()).salvar(any());
    }
}
