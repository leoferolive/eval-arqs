package com.example.loja.pedidos.controller;

import com.example.loja.pedidos.dto.ItemResponse;
import com.example.loja.pedidos.dto.PedidoResponse;
import com.example.loja.pedidos.exception.EstoqueInsuficienteException;
import com.example.loja.pedidos.exception.FreteIndisponivelException;
import com.example.loja.pedidos.exception.PedidoJaCanceladoException;
import com.example.loja.pedidos.exception.PedidoNaoEncontradoException;
import com.example.loja.pedidos.exception.ProdutoInativoException;
import com.example.loja.pedidos.exception.ProdutoInexistenteException;
import com.example.loja.pedidos.model.PedidoStatus;
import com.example.loja.pedidos.service.PedidoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    private PedidoResponse pedidoResponse() {
        return new PedidoResponse(1L, "01310100", PedidoStatus.CRIADO,
                List.of(new ItemResponse(1L, 2, new BigDecimal("59.90"), new BigDecimal("119.80"))),
                new BigDecimal("119.80"), new BigDecimal("25.50"), 5, new BigDecimal("145.30"));
    }

    @Test
    void criarDeveRetornar201() throws Exception {
        when(pedidoService.criar(any())).thenReturn(pedidoResponse());

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":1,\"quantidade\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valorTotal").value(145.30));
    }

    @Test
    void criarDeveRetornar400QuandoCepInvalido() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("{\"cep\":\"123\",\"itens\":[{\"produtoId\":1,\"quantidade\":2}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void criarDeveRetornar400QuandoSemItens() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("{\"cep\":\"01310100\",\"itens\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarDeveRetornar422QuandoProdutoInexistente() throws Exception {
        when(pedidoService.criar(any())).thenThrow(new ProdutoInexistenteException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":1,\"quantidade\":2}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_INEXISTENTE"));
    }

    @Test
    void criarDeveRetornar422QuandoProdutoInativo() throws Exception {
        when(pedidoService.criar(any())).thenThrow(new ProdutoInativoException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":1,\"quantidade\":2}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_INATIVO"));
    }

    @Test
    void criarDeveRetornar422QuandoEstoqueInsuficiente() throws Exception {
        when(pedidoService.criar(any())).thenThrow(new EstoqueInsuficienteException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":1,\"quantidade\":2}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ESTOQUE_INSUFICIENTE"));
    }

    @Test
    void criarDeveRetornar502QuandoFreteIndisponivel() throws Exception {
        when(pedidoService.criar(any())).thenThrow(new FreteIndisponivelException());

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":1,\"quantidade\":2}]}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value("FRETE_INDISPONIVEL"));
    }

    @Test
    void listarDeveRetornar200() throws Exception {
        when(pedidoService.listar()).thenReturn(List.of(pedidoResponse()));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscarPorIdDeveRetornar200() throws Exception {
        when(pedidoService.buscarPorId(1L)).thenReturn(pedidoResponse());

        mockMvc.perform(get("/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].produtoId").value(1));
    }

    @Test
    void buscarPorIdDeveRetornar404QuandoNaoEncontrado() throws Exception {
        when(pedidoService.buscarPorId(99L)).thenThrow(new PedidoNaoEncontradoException(99L));

        mockMvc.perform(get("/pedidos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarDeveRetornar200() throws Exception {
        PedidoResponse cancelado = new PedidoResponse(1L, "01310100", PedidoStatus.CANCELADO,
                List.of(), new BigDecimal("0.00"), new BigDecimal("0.00"), 0, new BigDecimal("0.00"));
        when(pedidoService.cancelar(eq(1L))).thenReturn(cancelado);

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void cancelarDeveRetornar404QuandoNaoEncontrado() throws Exception {
        when(pedidoService.cancelar(eq(99L))).thenThrow(new PedidoNaoEncontradoException(99L));

        mockMvc.perform(post("/pedidos/99/cancelamento"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarDeveRetornar409QuandoJaCancelado() throws Exception {
        when(pedidoService.cancelar(eq(1L))).thenThrow(new PedidoJaCanceladoException(1L));

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_JA_CANCELADO"));
    }
}
