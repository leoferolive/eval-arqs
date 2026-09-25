package com.example.loja.catalogo.service;

import com.example.loja.catalogo.dto.ProdutoConsultaDTO;
import com.example.loja.catalogo.dto.ProdutoRequest;
import com.example.loja.catalogo.dto.ProdutoResponse;
import com.example.loja.catalogo.dto.ProdutoUpdateRequest;
import com.example.loja.catalogo.exception.ProdutoNaoEncontradoException;
import com.example.loja.catalogo.exception.SkuDuplicadoException;
import com.example.loja.catalogo.model.Produto;
import com.example.loja.catalogo.repository.ProdutoRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto novoProduto(Long id, String sku, BigDecimal preco, int estoque, boolean ativo) throws Exception {
        Produto produto = new Produto(sku, "Produto " + sku, preco, estoque);
        if (!ativo) {
            produto.atualizar(produto.getNome(), preco, estoque, false);
        }
        Field idField = Produto.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(produto, id);
        return produto;
    }

    @Test
    void criarDeveSalvarQuandoSkuNaoExiste() throws Exception {
        when(produtoRepository.findBySku("CAM-001")).thenReturn(Optional.empty());
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> novoProduto(1L, "CAM-001", new BigDecimal("59.90"), 10, true));

        ProdutoResponse response = produtoService.criar(new ProdutoRequest("CAM-001", "Camiseta", new BigDecimal("59.90"), 10));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("CAM-001");
        assertThat(response.ativo()).isTrue();
    }

    @Test
    void criarDeveLancarSkuDuplicadoQuandoSkuJaExiste() throws Exception {
        when(produtoRepository.findBySku("CAM-001")).thenReturn(Optional.of(novoProduto(1L, "CAM-001", new BigDecimal("59.90"), 10, true)));

        assertThatThrownBy(() -> produtoService.criar(new ProdutoRequest("CAM-001", "Camiseta", new BigDecimal("59.90"), 10)))
                .isInstanceOf(SkuDuplicadoException.class);
    }

    @Test
    void listarDeveRetornarProdutosOrdenados() throws Exception {
        when(produtoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                novoProduto(1L, "A", BigDecimal.TEN, 1, true),
                novoProduto(2L, "B", BigDecimal.TEN, 1, true)
        ));

        List<ProdutoResponse> resultado = produtoService.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(1).id()).isEqualTo(2L);
    }

    @Test
    void buscarPorIdDeveLancarExcecaoQuandoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void buscarPorIdDeveRetornarProdutoQuandoEncontrado() throws Exception {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(novoProduto(1L, "A", BigDecimal.TEN, 1, true)));

        ProdutoResponse response = produtoService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void atualizarDeveAlterarCampos() throws Exception {
        Produto produto = novoProduto(1L, "A", BigDecimal.TEN, 1, true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        ProdutoResponse response = produtoService.atualizar(1L, new ProdutoUpdateRequest("Novo nome", new BigDecimal("20.00"), 5, false));

        assertThat(response.nome()).isEqualTo("Novo nome");
        assertThat(response.preco()).isEqualByComparingTo("20.00");
        assertThat(response.estoque()).isEqualTo(5);
        assertThat(response.ativo()).isFalse();
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.atualizar(99L, new ProdutoUpdateRequest("N", BigDecimal.ONE, 1, true)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void deletarDeveRemoverProdutoExistente() throws Exception {
        Produto produto = novoProduto(1L, "A", BigDecimal.TEN, 1, true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        produtoService.deletar(1L);

        verify(produtoRepository).delete(produto);
    }

    @Test
    void deletarDeveLancarExcecaoQuandoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.deletar(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void consultarDeveRetornarDtoQuandoExiste() throws Exception {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(novoProduto(1L, "A", new BigDecimal("15.00"), 3, true)));

        Optional<ProdutoConsultaDTO> resultado = produtoService.consultar(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().preco()).isEqualByComparingTo("15.00");
        assertThat(resultado.get().estoque()).isEqualTo(3);
        assertThat(resultado.get().ativo()).isTrue();
    }

    @Test
    void consultarDeveRetornarVazioQuandoNaoExiste() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(produtoService.consultar(99L)).isEmpty();
    }

    @Test
    void debitarEstoqueDeveDiminuirQuantidade() throws Exception {
        Produto produto = novoProduto(1L, "A", BigDecimal.TEN, 5, true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        produtoService.debitarEstoque(1L, 3);

        assertThat(produto.getEstoque()).isEqualTo(2);
    }

    @Test
    void creditarEstoqueDeveAumentarQuantidade() throws Exception {
        Produto produto = novoProduto(1L, "A", BigDecimal.TEN, 5, true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        produtoService.creditarEstoque(1L, 3);

        assertThat(produto.getEstoque()).isEqualTo(8);
    }
}
