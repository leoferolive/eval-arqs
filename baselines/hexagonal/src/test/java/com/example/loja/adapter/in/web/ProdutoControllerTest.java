package com.example.loja.adapter.in.web;

import com.example.loja.application.port.in.AtualizarProdutoUseCase;
import com.example.loja.application.port.in.BuscarProdutoUseCase;
import com.example.loja.application.port.in.CriarProdutoUseCase;
import com.example.loja.application.port.in.ExcluirProdutoUseCase;
import com.example.loja.application.port.in.ListarProdutosUseCase;
import com.example.loja.domain.catalogo.Produto;
import com.example.loja.domain.catalogo.ProdutoNaoEncontradoException;
import com.example.loja.domain.catalogo.SkuDuplicadoException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CriarProdutoUseCase criarProdutoUseCase;
    @MockitoBean
    private AtualizarProdutoUseCase atualizarProdutoUseCase;
    @MockitoBean
    private ExcluirProdutoUseCase excluirProdutoUseCase;
    @MockitoBean
    private BuscarProdutoUseCase buscarProdutoUseCase;
    @MockitoBean
    private ListarProdutosUseCase listarProdutosUseCase;

    @Test
    void criaProdutoComSucesso() throws Exception {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(criarProdutoUseCase.criar(any())).thenReturn(produto);

        mockMvc.perform(post("/produtos")
                        .contentType("application/json")
                        .content("""
                                {"sku":"CAM-001","nome":"Camiseta","preco":59.90,"estoque":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("CAM-001"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void rejeitaCriacaoComCorpoInvalido() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType("application/json")
                        .content("""
                                {"sku":"","nome":"","preco":-1,"estoque":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void rejeitaCorpoMalFormado() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType("application/json")
                        .content("{invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void retornaConflitoQuandoSkuDuplicado() throws Exception {
        when(criarProdutoUseCase.criar(any())).thenThrow(new SkuDuplicadoException("CAM-001"));

        mockMvc.perform(post("/produtos")
                        .contentType("application/json")
                        .content("""
                                {"sku":"CAM-001","nome":"Camiseta","preco":59.90,"estoque":10}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SKU_DUPLICADO"));
    }

    @Test
    void listaProdutos() throws Exception {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(listarProdutosUseCase.listarTodos()).thenReturn(List.of(produto));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscaProdutoPorId() throws Exception {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(buscarProdutoUseCase.buscarPorId(1L)).thenReturn(produto);

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("CAM-001"));
    }

    @Test
    void retorna404QuandoProdutoNaoEncontrado() throws Exception {
        when(buscarProdutoUseCase.buscarPorId(99L)).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_NAO_ENCONTRADO"));
    }

    @Test
    void atualizaProduto() throws Exception {
        Produto produto = new Produto(1L, "CAM-001", "Camiseta Nova", new BigDecimal("39.90"), 3, false);
        when(atualizarProdutoUseCase.atualizar(eq(1L), any())).thenReturn(produto);

        mockMvc.perform(put("/produtos/1")
                        .contentType("application/json")
                        .content("""
                                {"nome":"Camiseta Nova","preco":39.90,"estoque":3,"ativo":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Camiseta Nova"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void rejeitaAtualizacaoComCorpoInvalido() throws Exception {
        mockMvc.perform(put("/produtos/1")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void excluiProduto() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isNoContent());

        verify(excluirProdutoUseCase).excluir(1L);
    }

    @Test
    void retorna404AoExcluirProdutoInexistente() throws Exception {
        doThrow(new ProdutoNaoEncontradoException(99L)).when(excluirProdutoUseCase).excluir(99L);

        mockMvc.perform(delete("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_NAO_ENCONTRADO"));
    }
}
