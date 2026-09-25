package com.example.loja.usecase.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.Pedido;
import com.example.loja.entity.Produto;
import com.example.loja.entity.exception.EstoqueInsuficienteException;
import com.example.loja.entity.exception.FreteIndisponivelException;
import com.example.loja.entity.exception.ProdutoInativoException;
import com.example.loja.entity.exception.ProdutoInexistenteException;
import com.example.loja.usecase.gateway.FreteGateway;
import com.example.loja.usecase.gateway.FreteInfo;
import com.example.loja.usecase.gateway.PedidoGateway;
import com.example.loja.usecase.gateway.ProdutoGateway;

@ExtendWith(MockitoExtension.class)
class CriarPedidoInteractorTest {

    @Mock
    private ProdutoGateway produtoGateway;
    @Mock
    private PedidoGateway pedidoGateway;
    @Mock
    private FreteGateway freteGateway;

    private CriarPedidoInteractor interactor() {
        return new CriarPedidoInteractor(produtoGateway, pedidoGateway, freteGateway);
    }

    @Test
    void criaPedidoDecrementandoEstoqueESalvando() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(freteGateway.consultar("01310100")).thenReturn(new FreteInfo(new BigDecimal("25.50"), 5));
        when(pedidoGateway.salvar(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            return new Pedido(1L, p.getCep(), p.getStatus(), p.getItens(), p.getValorFrete(), p.getPrazoEntregaDias());
        });

        var input = new CriarPedidoInput("01310100",
                List.of(new CriarPedidoInput.ItemPedidoInput(1L, 2)));

        PedidoOutput output = interactor().executar(input);

        assertThat(output.id()).isEqualTo(1L);
        assertThat(output.valorFrete()).isEqualByComparingTo("25.50");
        assertThat(output.itens()).hasSize(1);
        assertThat(produto.getEstoque()).isEqualTo(8);
        verify(produtoGateway).salvar(produto);
    }

    @Test
    void lancaExcecaoQuandoProdutoInexistente() {
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        var input = new CriarPedidoInput("01310100", List.of(new CriarPedidoInput.ItemPedidoInput(1L, 2)));

        assertThatThrownBy(() -> interactor().executar(input)).isInstanceOf(ProdutoInexistenteException.class);
        verify(freteGateway, never()).consultar(any());
        verify(pedidoGateway, never()).salvar(any());
    }

    @Test
    void lancaExcecaoQuandoProdutoInativo() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, false);
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto));

        var input = new CriarPedidoInput("01310100", List.of(new CriarPedidoInput.ItemPedidoInput(1L, 2)));

        assertThatThrownBy(() -> interactor().executar(input)).isInstanceOf(ProdutoInativoException.class);
        verify(freteGateway, never()).consultar(any());
    }

    @Test
    void lancaExcecaoQuandoEstoqueInsuficiente() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 1, true);
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto));

        var input = new CriarPedidoInput("01310100", List.of(new CriarPedidoInput.ItemPedidoInput(1L, 2)));

        assertThatThrownBy(() -> interactor().executar(input)).isInstanceOf(EstoqueInsuficienteException.class);
        verify(freteGateway, never()).consultar(any());
        verify(pedidoGateway, never()).salvar(any());
    }

    @Test
    void lancaExcecaoQuandoFreteIndisponivelENaoPersisteNada() {
        Produto produto = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(freteGateway.consultar("01310100")).thenThrow(new FreteIndisponivelException("timeout"));

        var input = new CriarPedidoInput("01310100", List.of(new CriarPedidoInput.ItemPedidoInput(1L, 2)));

        assertThatThrownBy(() -> interactor().executar(input)).isInstanceOf(FreteIndisponivelException.class);
        verify(pedidoGateway, never()).salvar(any());
        verify(produtoGateway, never()).salvar(any());
    }
}
