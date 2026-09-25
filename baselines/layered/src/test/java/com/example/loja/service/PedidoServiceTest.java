package com.example.loja.service;

import com.example.loja.client.FreteClient;
import com.example.loja.client.FreteResponse;
import com.example.loja.dto.ItemPedidoRequest;
import com.example.loja.dto.PedidoRequest;
import com.example.loja.dto.PedidoResponse;
import com.example.loja.exception.EstoqueInsuficienteException;
import com.example.loja.exception.FreteIndisponivelException;
import com.example.loja.exception.PedidoJaCanceladoException;
import com.example.loja.exception.PedidoNaoEncontradoException;
import com.example.loja.exception.ProdutoInativoException;
import com.example.loja.exception.ProdutoInexistenteException;
import com.example.loja.model.Pedido;
import com.example.loja.model.Produto;
import com.example.loja.repository.PedidoRepository;
import com.example.loja.repository.ProdutoRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private FreteClient freteClient;

    private PedidoService pedidoService;

    private PedidoService service() {
        return new PedidoService(pedidoRepository, produtoRepository, freteClient);
    }

    private Produto produtoComId(Long id, String sku, BigDecimal preco, int estoque, boolean ativo) {
        Produto produto = new Produto(sku, "Produto " + sku, preco, estoque);
        produto.setAtivo(ativo);
        try {
            var campoId = Produto.class.getDeclaredField("id");
            campoId.setAccessible(true);
            campoId.set(produto, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return produto;
    }

    @Test
    void criaPedidoComSucesso() {
        pedidoService = service();
        Produto produto = produtoComId(1L, "SKU-1", new BigDecimal("10.00"), 5, true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(freteClient.consultarFrete("01310100")).thenReturn(new FreteResponse(new BigDecimal("25.50"), 5));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(1L, 2)));
        PedidoResponse resposta = pedidoService.criar(request);

        assertThat(resposta.valorItens()).isEqualByComparingTo("20.00");
        assertThat(resposta.valorFrete()).isEqualByComparingTo("25.50");
        assertThat(resposta.valorTotal()).isEqualByComparingTo("45.50");
        assertThat(resposta.prazoEntregaDias()).isEqualTo(5);
        assertThat(resposta.itens()).hasSize(1);
        assertThat(produto.getEstoque()).isEqualTo(3);
    }

    @Test
    void criarPedidoComProdutoInexistenteLancaExcecaoENaoConsultaFrete() {
        pedidoService = service();
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(1L, 2)));

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(ProdutoInexistenteException.class);
        verify(freteClient, never()).consultarFrete(anyString());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarPedidoComProdutoInativoLancaExcecao() {
        pedidoService = service();
        Produto produto = produtoComId(1L, "SKU-1", new BigDecimal("10.00"), 5, false);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(1L, 2)));

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(ProdutoInativoException.class);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarPedidoComEstoqueInsuficienteLancaExcecao() {
        pedidoService = service();
        Produto produto = produtoComId(1L, "SKU-1", new BigDecimal("10.00"), 1, true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(1L, 2)));

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(EstoqueInsuficienteException.class);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarPedidoComFreteIndisponivelNaoAlteraEstoqueNemSalva() {
        pedidoService = service();
        Produto produto = produtoComId(1L, "SKU-1", new BigDecimal("10.00"), 5, true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(freteClient.consultarFrete("01310100")).thenThrow(new FreteIndisponivelException("timeout"));

        PedidoRequest request = new PedidoRequest("01310100", List.of(new ItemPedidoRequest(1L, 2)));

        assertThatThrownBy(() -> pedidoService.criar(request))
                .isInstanceOf(FreteIndisponivelException.class);
        assertThat(produto.getEstoque()).isEqualTo(5);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void listaPedidos() {
        pedidoService = service();
        Pedido pedido = new Pedido("01310100", new BigDecimal("10.00"), new BigDecimal("5.00"), 3, new BigDecimal("15.00"));
        when(pedidoRepository.findAllComItensOrderByIdAsc()).thenReturn(List.of(pedido));

        List<PedidoResponse> resultado = pedidoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).cep()).isEqualTo("01310100");
    }

    @Test
    void buscarPedidoInexistenteLancaExcecao() {
        pedidoService = service();
        when(pedidoRepository.findByIdComItens(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPorId(99L))
                .isInstanceOf(PedidoNaoEncontradoException.class);
    }

    @Test
    void cancelaPedidoCriadoDevolveEstoque() {
        pedidoService = service();
        Produto produto = produtoComId(1L, "SKU-1", new BigDecimal("10.00"), 3, true);
        Pedido pedido = new Pedido("01310100", new BigDecimal("20.00"), new BigDecimal("5.00"), 3, new BigDecimal("25.00"));
        pedido.adicionarItem(new com.example.loja.model.ItemPedido(1L, 2, new BigDecimal("10.00"), new BigDecimal("20.00")));
        when(pedidoRepository.findByIdComItens(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        PedidoResponse resposta = pedidoService.cancelar(1L);

        assertThat(resposta.status()).isEqualTo(com.example.loja.model.StatusPedido.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(5);
    }

    @Test
    void cancelarPedidoJaCanceladoLancaExcecao() {
        pedidoService = service();
        Pedido pedido = new Pedido("01310100", new BigDecimal("20.00"), new BigDecimal("5.00"), 3, new BigDecimal("25.00"));
        pedido.cancelar();
        when(pedidoRepository.findByIdComItens(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelar(1L))
                .isInstanceOf(PedidoJaCanceladoException.class);
    }
}
