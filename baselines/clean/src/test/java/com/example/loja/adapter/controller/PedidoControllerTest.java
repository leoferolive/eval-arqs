package com.example.loja.adapter.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.loja.adapter.presenter.PedidoPresenter;
import com.example.loja.entity.StatusPedido;
import com.example.loja.entity.exception.EstoqueInsuficienteException;
import com.example.loja.entity.exception.FreteIndisponivelException;
import com.example.loja.entity.exception.PedidoJaCanceladoException;
import com.example.loja.entity.exception.PedidoNaoEncontradoException;
import com.example.loja.entity.exception.ProdutoInativoException;
import com.example.loja.entity.exception.ProdutoInexistenteException;
import com.example.loja.usecase.pedido.BuscarPedidoInteractor;
import com.example.loja.usecase.pedido.CancelarPedidoInteractor;
import com.example.loja.usecase.pedido.CriarPedidoInteractor;
import com.example.loja.usecase.pedido.ItemPedidoOutput;
import com.example.loja.usecase.pedido.ListarPedidosInteractor;
import com.example.loja.usecase.pedido.PedidoOutput;

@WebMvcTest(PedidoController.class)
@Import(PedidoPresenter.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarPedidoInteractor criarPedidoInteractor;
    @MockitoBean
    private ListarPedidosInteractor listarPedidosInteractor;
    @MockitoBean
    private BuscarPedidoInteractor buscarPedidoInteractor;
    @MockitoBean
    private CancelarPedidoInteractor cancelarPedidoInteractor;

    private PedidoOutput pedidoOutput(Long id, StatusPedido status) {
        var item = new ItemPedidoOutput(1L, 2, new BigDecimal("59.90"), new BigDecimal("119.80"));
        return new PedidoOutput(id, "01310100", status, List.of(item), new BigDecimal("119.80"),
                new BigDecimal("25.50"), 5, new BigDecimal("145.30"));
    }

    @Test
    void criarPedidoComSucessoRetorna201() throws Exception {
        when(criarPedidoInteractor.executar(any())).thenReturn(pedidoOutput(1L, StatusPedido.CRIADO));

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CRIADO")))
                .andExpect(jsonPath("$.valorTotal", is(145.30)));
    }

    @Test
    void criarPedidoComCorpoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"123","itens":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("VALIDACAO")));
    }

    @Test
    void criarPedidoComProdutoInexistenteRetorna422() throws Exception {
        when(criarPedidoInteractor.executar(any())).thenThrow(new ProdutoInexistenteException(1L));

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo", is("PRODUTO_INEXISTENTE")));
    }

    @Test
    void criarPedidoComProdutoInativoRetorna422() throws Exception {
        when(criarPedidoInteractor.executar(any())).thenThrow(new ProdutoInativoException(1L));

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo", is("PRODUTO_INATIVO")));
    }

    @Test
    void criarPedidoComEstoqueInsuficienteRetorna422() throws Exception {
        when(criarPedidoInteractor.executar(any())).thenThrow(new EstoqueInsuficienteException(1L));

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo", is("ESTOQUE_INSUFICIENTE")));
    }

    @Test
    void criarPedidoComFreteIndisponivelRetorna502() throws Exception {
        when(criarPedidoInteractor.executar(any())).thenThrow(new FreteIndisponivelException("falha"));

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo", is("FRETE_INDISPONIVEL")));
    }

    @Test
    void listarPedidosRetorna200() throws Exception {
        when(listarPedidosInteractor.executar()).thenReturn(List.of(pedidoOutput(1L, StatusPedido.CRIADO)));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void buscarPedidoExistenteRetorna200() throws Exception {
        when(buscarPedidoInteractor.executar(1L)).thenReturn(pedidoOutput(1L, StatusPedido.CRIADO));

        mockMvc.perform(get("/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void buscarPedidoInexistenteRetorna404() throws Exception {
        when(buscarPedidoInteractor.executar(99L)).thenThrow(new PedidoNaoEncontradoException(99L));

        mockMvc.perform(get("/pedidos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("PEDIDO_NAO_ENCONTRADO")));
    }

    @Test
    void cancelarPedidoComSucessoRetorna200() throws Exception {
        when(cancelarPedidoInteractor.executar(1L)).thenReturn(pedidoOutput(1L, StatusPedido.CANCELADO));

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELADO")));
    }

    @Test
    void cancelarPedidoInexistenteRetorna404() throws Exception {
        when(cancelarPedidoInteractor.executar(99L)).thenThrow(new PedidoNaoEncontradoException(99L));

        mockMvc.perform(post("/pedidos/99/cancelamento"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarPedidoJaCanceladoRetorna409() throws Exception {
        when(cancelarPedidoInteractor.executar(1L)).thenThrow(new PedidoJaCanceladoException(1L));

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo", is("PEDIDO_JA_CANCELADO")));
    }
}
