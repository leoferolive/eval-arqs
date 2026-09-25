package com.example.loja.catalogo.controller;

import com.example.loja.catalogo.dto.ProdutoResponse;
import com.example.loja.catalogo.exception.ProdutoNaoEncontradoException;
import com.example.loja.catalogo.exception.SkuDuplicadoException;
import com.example.loja.catalogo.service.ProdutoService;
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
    private ProdutoService produtoService;

    private ProdutoResponse produtoResponse() {
        return new ProdutoResponse(1L, "CAM-001", "Camiseta", new BigDecimal("59.90"), 10, true);
    }

    @Test
    void criarDeveRetornar201() throws Exception {
        when(produtoService.criar(any())).thenReturn(produtoResponse());

        mockMvc.perform(post("/produtos")
                        .contentType("application/json")
                        .content("{\"sku\":\"CAM-001\",\"nome\":\"Camiseta\",\"preco\":59.90,\"estoque\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("CAM-001"));
    }

    @Test
    void criarDeveRetornar400QuandoInvalido() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType("application/json")
                        .content("{\"sku\":\"\",\"nome\":\"\",\"preco\":-1,\"estoque\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void criarDeveRetornar409QuandoSkuDuplicado() throws Exception {
        when(produtoService.criar(any())).thenThrow(new SkuDuplicadoException("CAM-001"));

        mockMvc.perform(post("/produtos")
                        .contentType("application/json")
                        .content("{\"sku\":\"CAM-001\",\"nome\":\"Camiseta\",\"preco\":59.90,\"estoque\":10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SKU_DUPLICADO"));
    }

    @Test
    void listarDeveRetornar200() throws Exception {
        when(produtoService.listar()).thenReturn(List.of(produtoResponse()));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscarPorIdDeveRetornar200() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(produtoResponse());

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("CAM-001"));
    }

    @Test
    void buscarPorIdDeveRetornar404QuandoNaoEncontrado() throws Exception {
        when(produtoService.buscarPorId(99L)).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_NAO_ENCONTRADO"));
    }

    @Test
    void atualizarDeveRetornar200() throws Exception {
        when(produtoService.atualizar(eq(1L), any())).thenReturn(produtoResponse());

        mockMvc.perform(put("/produtos/1")
                        .contentType("application/json")
                        .content("{\"nome\":\"Camiseta\",\"preco\":59.90,\"estoque\":10,\"ativo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void atualizarDeveRetornar400QuandoInvalido() throws Exception {
        mockMvc.perform(put("/produtos/1")
                        .contentType("application/json")
                        .content("{\"nome\":\"\",\"preco\":-1,\"estoque\":-1,\"ativo\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarDeveRetornar404QuandoNaoEncontrado() throws Exception {
        when(produtoService.atualizar(eq(99L), any())).thenThrow(new ProdutoNaoEncontradoException(99L));

        mockMvc.perform(put("/produtos/99")
                        .contentType("application/json")
                        .content("{\"nome\":\"Camiseta\",\"preco\":59.90,\"estoque\":10,\"ativo\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletarDeveRetornar204() throws Exception {
        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletarDeveRetornar404QuandoNaoEncontrado() throws Exception {
        org.mockito.Mockito.doThrow(new ProdutoNaoEncontradoException(99L)).when(produtoService).deletar(99L);

        mockMvc.perform(delete("/produtos/99"))
                .andExpect(status().isNotFound());
    }
}
