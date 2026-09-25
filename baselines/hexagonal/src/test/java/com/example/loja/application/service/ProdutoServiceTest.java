package com.example.loja.application.service;

import com.example.loja.application.port.in.AtualizarProdutoUseCase.AtualizarProdutoCommand;
import com.example.loja.application.port.in.CriarProdutoUseCase.NovoProdutoCommand;
import com.example.loja.application.port.out.ProdutoRepositoryPort;
import com.example.loja.domain.catalogo.Produto;
import com.example.loja.domain.catalogo.ProdutoNaoEncontradoException;
import com.example.loja.domain.catalogo.SkuDuplicadoException;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepositoryPort produtoRepositoryPort;

    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        produtoService = new ProdutoService(produtoRepositoryPort);
    }

    @Test
    void criaProdutoQuandoSkuNaoExiste() {
        when(produtoRepositoryPort.buscarPorSku("CAM-001")).thenReturn(Optional.empty());
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocacao -> {
            Produto p = invocacao.getArgument(0);
            p.setId(1L);
            return p;
        });

        Produto produto = produtoService.criar(new NovoProdutoCommand("CAM-001", "Camiseta", new BigDecimal("59.90"), 10));

        assertThat(produto.getId()).isEqualTo(1L);
        assertThat(produto.isAtivo()).isTrue();
        verify(produtoRepositoryPort).salvar(any(Produto.class));
    }

    @Test
    void rejeitaCriacaoComSkuDuplicado() {
        when(produtoRepositoryPort.buscarPorSku("CAM-001"))
                .thenReturn(Optional.of(Produto.novo("CAM-001", "Camiseta", BigDecimal.TEN, 1)));

        assertThatThrownBy(() -> produtoService.criar(new NovoProdutoCommand("CAM-001", "Camiseta", BigDecimal.TEN, 1)))
                .isInstanceOf(SkuDuplicadoException.class);
    }

    @Test
    void atualizaProdutoExistente() {
        Produto existente = new Produto(1L, "CAM-001", "Camiseta", BigDecimal.TEN, 5, true);
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Produto atualizado = produtoService.atualizar(1L, new AtualizarProdutoCommand("Novo nome", new BigDecimal("20.00"), 8, false));

        assertThat(atualizado.getNome()).isEqualTo("Novo nome");
        assertThat(atualizado.getPreco()).isEqualByComparingTo("20.00");
        assertThat(atualizado.getEstoque()).isEqualTo(8);
        assertThat(atualizado.isAtivo()).isFalse();
    }

    @Test
    void falhaAoAtualizarProdutoInexistente() {
        when(produtoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.atualizar(99L, new AtualizarProdutoCommand("x", BigDecimal.TEN, 1, true)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void excluiProdutoExistente() {
        when(produtoRepositoryPort.existePorId(1L)).thenReturn(true);

        produtoService.excluir(1L);

        verify(produtoRepositoryPort).excluir(1L);
    }

    @Test
    void falhaAoExcluirProdutoInexistente() {
        when(produtoRepositoryPort.existePorId(99L)).thenReturn(false);

        assertThatThrownBy(() -> produtoService.excluir(99L)).isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void buscaProdutoPorId() {
        Produto existente = new Produto(1L, "CAM-001", "Camiseta", BigDecimal.TEN, 5, true);
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(existente));

        assertThat(produtoService.buscarPorId(1L)).isEqualTo(existente);
    }

    @Test
    void falhaAoBuscarProdutoInexistente() {
        when(produtoRepositoryPort.buscarPorId(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(1L)).isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void listaTodosOsProdutos() {
        Produto p1 = new Produto(1L, "A", "A", BigDecimal.ONE, 1, true);
        when(produtoRepositoryPort.listarTodos()).thenReturn(List.of(p1));

        assertThat(produtoService.listarTodos()).containsExactly(p1);
    }
}
