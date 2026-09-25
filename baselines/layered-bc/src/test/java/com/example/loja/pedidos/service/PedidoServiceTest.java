package com.example.loja.pedidos.service;

import com.example.loja.catalogo.dto.ProdutoConsultaDTO;
import com.example.loja.catalogo.service.ProdutoService;
import com.example.loja.pedidos.client.FreteClient;
import com.example.loja.pedidos.client.FreteRespostaDTO;
import com.example.loja.pedidos.dto.ItemRequest;
import com.example.loja.pedidos.dto.PedidoRequest;
import com.example.loja.pedidos.dto.PedidoResponse;
import com.example.loja.pedidos.exception.EstoqueInsuficienteException;
import com.example.loja.pedidos.exception.PedidoJaCanceladoException;
import com.example.loja.pedidos.exception.PedidoNaoEncontradoException;
import com.example.loja.pedidos.exception.ProdutoInativoException;
import com.example.loja.pedidos.exception.ProdutoInexistenteException;
import com.example.loja.pedidos.model.ItemPedido;
import com.example.loja.pedidos.model.Pedido;
import com.example.loja.pedidos.model.PedidoStatus;
import com.example.loja.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private FreteClient freteClient;

    @InjectMocks
    private PedidoService pedidoService;

    private static Pedido pedidoComId(Long id, List<ItemPedido> itens, BigDecimal frete, int prazo) throws Exception {
        Pedido pedido = new Pedido("01310100", itens, frete, prazo);
        Field idField = Pedido.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(pedido, id);
        return pedido;
    }

    @Test
    void criarDeveCalcularTotaisEDebitarEstoque() {
        when(produtoService.consultar(1L)).thenReturn(Optional.of(new ProdutoConsultaDTO(1L, new BigDecimal("50.00"), 10, true)));
        when(freteClient.consultarFrete("01310100")).thenReturn(new FreteRespostaDTO(new BigDecimal("25.50"), 5));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemRequest(1L, 2)));

        PedidoResponse response = pedidoService.criar(request);

        assertThat(response.valorItens()).isEqualByComparingTo("100.00");
        assertThat(response.valorFrete()).isEqualByComparingTo("25.50");
        assertThat(response.valorTotal()).isEqualByComparingTo("125.50");
        assertThat(response.prazoEntregaDias()).isEqualTo(5);
        assertThat(response.status()).isEqualTo(PedidoStatus.CRIADO);
        verify(produtoService).debitarEstoque(1L, 2);
    }

    @Test
    void criarDeveLancarProdutoInexistenteSemChamarFreteOuDebitar() {
        when(produtoService.consultar(1L)).thenReturn(Optional.empty());

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemRequest(1L, 2)));

        assertThatThrownBy(() -> pedidoService.criar(request)).isInstanceOf(ProdutoInexistenteException.class);
        verify(freteClient, never()).consultarFrete(any());
        verify(produtoService, never()).debitarEstoque(any(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarDeveLancarProdutoInativo() {
        when(produtoService.consultar(1L)).thenReturn(Optional.of(new ProdutoConsultaDTO(1L, BigDecimal.TEN, 10, false)));

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemRequest(1L, 1)));

        assertThatThrownBy(() -> pedidoService.criar(request)).isInstanceOf(ProdutoInativoException.class);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarDeveLancarEstoqueInsuficiente() {
        when(produtoService.consultar(1L)).thenReturn(Optional.of(new ProdutoConsultaDTO(1L, BigDecimal.TEN, 1, true)));

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemRequest(1L, 5)));

        assertThatThrownBy(() -> pedidoService.criar(request)).isInstanceOf(EstoqueInsuficienteException.class);
        verify(freteClient, never()).consultarFrete(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarNaoDevePersistirNemDebitarQuandoFreteFalha() {
        when(produtoService.consultar(1L)).thenReturn(Optional.of(new ProdutoConsultaDTO(1L, BigDecimal.TEN, 10, true)));
        when(freteClient.consultarFrete("01310100")).thenThrow(new com.example.loja.pedidos.exception.FreteIndisponivelException());

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemRequest(1L, 1)));

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(com.example.loja.pedidos.exception.FreteIndisponivelException.class);
        verify(produtoService, never()).debitarEstoque(any(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void listarDeveRetornarPedidosOrdenados() throws Exception {
        Pedido p1 = pedidoComId(1L, List.of(new ItemPedido(1L, 1, BigDecimal.TEN)), BigDecimal.ONE, 1);
        when(pedidoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(p1));

        List<PedidoResponse> resultado = pedidoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
    }

    @Test
    void buscarPorIdDeveLancarExcecaoQuandoNaoEncontrado() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPorId(99L)).isInstanceOf(PedidoNaoEncontradoException.class);
    }

    @Test
    void cancelarDeveMudarStatusEDevolverEstoque() throws Exception {
        Pedido pedido = pedidoComId(1L, List.of(new ItemPedido(1L, 3, BigDecimal.TEN)), BigDecimal.ONE, 1);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoResponse response = pedidoService.cancelar(1L);

        assertThat(response.status()).isEqualTo(PedidoStatus.CANCELADO);
        verify(produtoService).creditarEstoque(1L, 3);
    }

    @Test
    void cancelarDeveLancarExcecaoQuandoJaCancelado() throws Exception {
        Pedido pedido = pedidoComId(1L, List.of(new ItemPedido(1L, 3, BigDecimal.TEN)), BigDecimal.ONE, 1);
        pedido.cancelar();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelar(1L)).isInstanceOf(PedidoJaCanceladoException.class);
        verify(produtoService, never()).creditarEstoque(any(), anyInt());
    }

    @Test
    void cancelarDeveLancarExcecaoQuandoNaoEncontrado() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.cancelar(99L)).isInstanceOf(PedidoNaoEncontradoException.class);
    }
}
