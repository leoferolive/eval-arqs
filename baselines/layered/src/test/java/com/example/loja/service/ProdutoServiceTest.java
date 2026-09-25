package com.example.loja.service;

import com.example.loja.dto.ProdutoRequest;
import com.example.loja.dto.ProdutoResponse;
import com.example.loja.dto.ProdutoUpdateRequest;
import com.example.loja.exception.ProdutoNaoEncontradoException;
import com.example.loja.exception.SkuDuplicadoException;
import com.example.loja.model.Produto;
import com.example.loja.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    private ProdutoService produtoService;

    private ProdutoService service() {
        return new ProdutoService(produtoRepository);
    }

    @Test
    void criaProdutoComSkuInexistente() {
        produtoService = service();
        when(produtoRepository.existsBySku("SKU-1")).thenReturn(false);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProdutoResponse resposta = produtoService.criar(
                new ProdutoRequest("SKU-1", "Produto Teste", new BigDecimal("10.005"), 5));

        assertThat(resposta.sku()).isEqualTo("SKU-1");
        assertThat(resposta.nome()).isEqualTo("Produto Teste");
        assertThat(resposta.preco()).isEqualByComparingTo("10.01");
        assertThat(resposta.estoque()).isEqualTo(5);
        assertThat(resposta.ativo()).isTrue();
    }

    @Test
    void rejeitaSkuDuplicado() {
        produtoService = service();
        when(produtoRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> produtoService.criar(
                new ProdutoRequest("SKU-1", "Produto Teste", BigDecimal.TEN, 5)))
                .isInstanceOf(SkuDuplicadoException.class);
    }

    @Test
    void listaProdutosOrdenadosPorId() {
        produtoService = service();
        Produto p1 = new Produto("A", "Prod A", BigDecimal.ONE, 1);
        when(produtoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(p1));

        List<ProdutoResponse> resultado = produtoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).sku()).isEqualTo("A");
    }

    @Test
    void buscaPorIdExistente() {
        produtoService = service();
        Produto p1 = criarComId(1L, "A", "Prod A", BigDecimal.ONE, 1);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(p1));

        ProdutoResponse resposta = produtoService.buscarPorId(1L);

        assertThat(resposta.id()).isEqualTo(1L);
    }

    @Test
    void buscaPorIdInexistenteLancaExcecao() {
        produtoService = service();
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void atualizaProdutoExistente() {
        produtoService = service();
        Produto p1 = criarComId(1L, "A", "Prod A", BigDecimal.ONE, 1);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(p1));

        ProdutoResponse resposta = produtoService.atualizar(1L,
                new ProdutoUpdateRequest("Novo Nome", new BigDecimal("20.00"), 8, false));

        assertThat(resposta.nome()).isEqualTo("Novo Nome");
        assertThat(resposta.preco()).isEqualByComparingTo("20.00");
        assertThat(resposta.estoque()).isEqualTo(8);
        assertThat(resposta.ativo()).isFalse();
    }

    @Test
    void atualizarProdutoInexistenteLancaExcecao() {
        produtoService = service();
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.atualizar(99L,
                new ProdutoUpdateRequest("Nome", BigDecimal.ONE, 1, true)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void deletaProdutoExistente() {
        produtoService = service();
        Produto p1 = criarComId(1L, "A", "Prod A", BigDecimal.ONE, 1);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(p1));

        produtoService.deletar(1L);

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoRepository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void deletarProdutoInexistenteLancaExcecao() {
        produtoService = service();
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.deletar(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    private Produto criarComId(Long id, String sku, String nome, BigDecimal preco, int estoque) {
        Produto produto = new Produto(sku, nome, preco, estoque);
        try {
            var campoId = Produto.class.getDeclaredField("id");
            campoId.setAccessible(true);
            campoId.set(produto, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return produto;
    }
}
