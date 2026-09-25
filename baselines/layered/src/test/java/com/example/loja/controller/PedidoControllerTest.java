package com.example.loja.controller;

import com.example.loja.dto.ItemPedidoResponse;
import com.example.loja.dto.PedidoRequest;
import com.example.loja.dto.PedidoResponse;
import com.example.loja.exception.EstoqueInsuficienteException;
import com.example.loja.exception.FreteIndisponivelException;
import com.example.loja.exception.PedidoJaCanceladoException;
import com.example.loja.exception.PedidoNaoEncontradoException;
import com.example.loja.exception.ProdutoInativoException;
import com.example.loja.exception.ProdutoInexistenteException;
import com.example.loja.model.StatusPedido;
import com.example.loja.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    private PedidoResponse respostaPadrao() {
        return new PedidoResponse(1L, "01310100", StatusPedido.CRIADO,
                List.of(new ItemPedidoResponse(1L, 2, new BigDecimal("59.90"), new BigDecimal("119.80"))),
                new BigDecimal("119.80"), new BigDecimal("25.50"), 5, new BigDecimal("145.30"));
    }

    @Test
    void criarPedidoRetorna201() throws Exception {
        when(pedidoService.criar(any(PedidoRequest.class))).thenReturn(respostaPadrao());

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.valorTotal").value(145.30));
    }

    @Test
    void criarPedidoComCorpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"123","itens":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void criarPedidoComProdutoInexistenteRetorna422() throws Exception {
        when(pedidoService.criar(any(PedidoRequest.class))).thenThrow(new ProdutoInexistenteException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_INEXISTENTE"));
    }

    @Test
    void criarPedidoComProdutoInativoRetorna422() throws Exception {
        when(pedidoService.criar(any(PedidoRequest.class))).thenThrow(new ProdutoInativoException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_INATIVO"));
    }

    @Test
    void criarPedidoComEstoqueInsuficienteRetorna422() throws Exception {
        when(pedidoService.criar(any(PedidoRequest.class))).thenThrow(new EstoqueInsuficienteException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ESTOQUE_INSUFICIENTE"));
    }

    @Test
    void criarPedidoComFreteIndisponivelRetorna502() throws Exception {
        when(pedidoService.criar(any(PedidoRequest.class))).thenThrow(new FreteIndisponivelException("timeout"));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value("FRETE_INDISPONIVEL"));
    }

    @Test
    void listarPedidosRetorna200() throws Exception {
        when(pedidoService.listar()).thenReturn(List.of(respostaPadrao()));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscarPedidoPorIdRetorna200() throws Exception {
        when(pedidoService.buscarPorId(1L)).thenReturn(respostaPadrao());

        mockMvc.perform(get("/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01310100"));
    }

    @Test
    void buscarPedidoInexistenteRetorna404() throws Exception {
        when(pedidoService.buscarPorId(99L)).thenThrow(new PedidoNaoEncontradoException(99L));

        mockMvc.perform(get("/pedidos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_NAO_ENCONTRADO"));
    }

    @Test
    void cancelarPedidoRetorna200() throws Exception {
        PedidoResponse cancelado = new PedidoResponse(1L, "01310100", StatusPedido.CANCELADO,
                List.of(), new BigDecimal("0.00"), new BigDecimal("0.00"), 0, new BigDecimal("0.00"));
        when(pedidoService.cancelar(1L)).thenReturn(cancelado);

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void cancelarPedidoJaCanceladoRetorna409() throws Exception {
        when(pedidoService.cancelar(1L)).thenThrow(new PedidoJaCanceladoException(1L));

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_JA_CANCELADO"));
    }

    @Test
    void cancelarPedidoInexistenteRetorna404() throws Exception {
        when(pedidoService.cancelar(99L)).thenThrow(new PedidoNaoEncontradoException(99L));

        mockMvc.perform(post("/pedidos/99/cancelamento"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_NAO_ENCONTRADO"));
    }
}
