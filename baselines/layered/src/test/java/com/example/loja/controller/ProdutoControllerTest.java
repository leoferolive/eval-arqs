package com.example.loja.controller;

import com.example.loja.dto.ProdutoRequest;
import com.example.loja.dto.ProdutoResponse;
import com.example.loja.dto.ProdutoUpdateRequest;
import com.example.loja.exception.ProdutoNaoEncontradoException;
import com.example.loja.exception.SkuDuplicadoException;
import com.example.loja.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @MockitoBean
    private ProdutoService produtoService;

    @Test
    void criarProdutoRetorna201() throws Exception {
        ProdutoResponse resposta = new ProdutoResponse(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(produtoService.criar(any(ProdutoRequest.class))).thenReturn(resposta);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","nome":"Camiseta","preco":59.90,"estoque":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }

    @Test
    void criarProdutoComCorpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","nome":"","preco":-1,"estoque":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void criarProdutoComCorpoMalFormadoRetorna400() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ nao e json valido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void criarProdutoComSkuDuplicadoRetorna409() throws Exception {
        when(produtoService.criar(any(ProdutoRequest.class))).thenThrow(new SkuDuplicadoException("SKU-1"));

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","nome":"Camiseta","preco":59.90,"estoque":10}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SKU_DUPLICADO"));
    }

    @Test
    void listarProdutosRetorna200() throws Exception {
        ProdutoResponse resposta = new ProdutoResponse(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(produtoService.listar()).thenReturn(List.of(resposta));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscarProdutoPorIdRetorna200() throws Exception {
        ProdutoResponse resposta = new ProdutoResponse(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(produtoService.buscarPorId(1L)).thenReturn(resposta);

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }

    @Test
    void buscarProdutoInexistenteRetorna404() throws Exception {
        when(produtoService.buscarPorId(99L)).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_NAO_ENCONTRADO"));
    }

    @Test
    void atualizarProdutoRetorna200() throws Exception {
        ProdutoResponse resposta = new ProdutoResponse(1L, "SKU-1", "Novo Nome", new BigDecimal("70.00"), 20, false);
        when(produtoService.atualizar(eq(1L), any(ProdutoUpdateRequest.class))).thenReturn(resposta);

        mockMvc.perform(put("/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Novo Nome","preco":70.00,"estoque":20,"ativo":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void atualizarProdutoComCorpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(put("/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","preco":-1,"estoque":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void deletarProdutoRetorna204() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isNoContent());
        verify(produtoService).deletar(1L);
    }

    @Test
    void deletarProdutoInexistenteRetorna404() throws Exception {
        org.mockito.Mockito.doThrow(new ProdutoNaoEncontradoException(99L)).when(produtoService).deletar(99L);

        mockMvc.perform(delete("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_NAO_ENCONTRADO"));
    }
}
