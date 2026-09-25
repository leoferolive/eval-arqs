package com.example.loja.adapter.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.loja.adapter.presenter.ProdutoPresenter;
import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.entity.exception.SkuDuplicadoException;
import com.example.loja.usecase.produto.AtualizarProdutoInteractor;
import com.example.loja.usecase.produto.BuscarProdutoInteractor;
import com.example.loja.usecase.produto.CriarProdutoInteractor;
import com.example.loja.usecase.produto.ListarProdutosInteractor;
import com.example.loja.usecase.produto.ProdutoOutput;
import com.example.loja.usecase.produto.RemoverProdutoInteractor;

@WebMvcTest(ProdutoController.class)
@Import(ProdutoPresenter.class)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarProdutoInteractor criarProdutoInteractor;
    @MockitoBean
    private ListarProdutosInteractor listarProdutosInteractor;
    @MockitoBean
    private BuscarProdutoInteractor buscarProdutoInteractor;
    @MockitoBean
    private AtualizarProdutoInteractor atualizarProdutoInteractor;
    @MockitoBean
    private RemoverProdutoInteractor removerProdutoInteractor;

    @Test
    void criarProdutoComSucessoRetorna201() throws Exception {
        when(criarProdutoInteractor.executar(any()))
                .thenReturn(new ProdutoOutput(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true));

        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","nome":"Camiseta","preco":59.90,"estoque":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sku", is("SKU-1")))
                .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void criarProdutoComCorpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","nome":"Camiseta","preco":-1,"estoque":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("VALIDACAO")));
    }

    @Test
    void criarProdutoComSkuDuplicadoRetorna409() throws Exception {
        when(criarProdutoInteractor.executar(any())).thenThrow(new SkuDuplicadoException("SKU-1"));

        mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","nome":"Camiseta","preco":59.90,"estoque":10}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo", is("SKU_DUPLICADO")));
    }

    @Test
    void listarProdutosRetorna200() throws Exception {
        when(listarProdutosInteractor.executar())
                .thenReturn(java.util.List.of(new ProdutoOutput(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true)));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].sku", is("SKU-1")));
    }

    @Test
    void buscarProdutoExistenteRetorna200() throws Exception {
        when(buscarProdutoInteractor.executar(1L))
                .thenReturn(new ProdutoOutput(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true));

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void buscarProdutoInexistenteRetorna404() throws Exception {
        when(buscarProdutoInteractor.executar(99L)).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("PRODUTO_NAO_ENCONTRADO")));
    }

    @Test
    void atualizarProdutoComSucessoRetorna200() throws Exception {
        when(atualizarProdutoInteractor.executar(any()))
                .thenReturn(new ProdutoOutput(1L, "SKU-1", "Nova", new BigDecimal("10.00"), 3, false));

        mockMvc.perform(put("/produtos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Nova","preco":10.00,"estoque":3,"ativo":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Nova")))
                .andExpect(jsonPath("$.ativo", is(false)));
    }

    @Test
    void atualizarProdutoComCorpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(put("/produtos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("VALIDACAO")));
    }

    @Test
    void atualizarProdutoInexistenteRetorna404() throws Exception {
        when(atualizarProdutoInteractor.executar(any())).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(put("/produtos/99").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Nova","preco":10.00,"estoque":3,"ativo":false}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerProdutoComSucessoRetorna204() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isNoContent());

        verify(removerProdutoInteractor).executar(1L);
    }

    @Test
    void removerProdutoInexistenteRetorna404() throws Exception {
        doThrow(new ProdutoNaoEncontradoException(99L)).when(removerProdutoInteractor).executar(eq(99L));

        mockMvc.perform(delete("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("PRODUTO_NAO_ENCONTRADO")));
    }
}
