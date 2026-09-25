package com.example.loja.adapter.in.web;

import com.example.loja.application.port.in.BuscarPedidoUseCase;
import com.example.loja.application.port.in.CancelarPedidoUseCase;
import com.example.loja.application.port.in.CriarPedidoUseCase;
import com.example.loja.application.port.in.ListarPedidosUseCase;
import com.example.loja.domain.pedidos.EstoqueInsuficienteException;
import com.example.loja.domain.pedidos.FreteIndisponivelException;
import com.example.loja.domain.pedidos.ItemPedido;
import com.example.loja.domain.pedidos.Pedido;
import com.example.loja.domain.pedidos.PedidoJaCanceladoException;
import com.example.loja.domain.pedidos.PedidoNaoEncontradoException;
import com.example.loja.domain.pedidos.ProdutoInativoException;
import com.example.loja.domain.pedidos.ProdutoInexistenteException;
import com.example.loja.domain.pedidos.StatusPedido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
    private CriarPedidoUseCase criarPedidoUseCase;
    @MockitoBean
    private BuscarPedidoUseCase buscarPedidoUseCase;
    @MockitoBean
    private ListarPedidosUseCase listarPedidosUseCase;
    @MockitoBean
    private CancelarPedidoUseCase cancelarPedidoUseCase;

    private Pedido pedidoExemplo() {
        ItemPedido item = new ItemPedido(1L, 2, new BigDecimal("59.90"));
        return new Pedido(1L, "01310100", StatusPedido.CRIADO, List.of(item), new BigDecimal("25.50"), 5);
    }

    @Test
    void criaPedidoComSucesso() throws Exception {
        when(criarPedidoUseCase.criar(any())).thenReturn(pedidoExemplo());

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.valorItens").value(119.80))
                .andExpect(jsonPath("$.valorTotal").value(145.30));
    }

    @Test
    void rejeitaCepInvalido() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("""
                                {"cep":"123","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void rejeitaPedidoSemItens() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("""
                                {"cep":"01310100","itens":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void retorna422QuandoProdutoInexistente() throws Exception {
        when(criarPedidoUseCase.criar(any())).thenThrow(new ProdutoInexistenteException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_INEXISTENTE"));
    }

    @Test
    void retorna422QuandoProdutoInativo() throws Exception {
        when(criarPedidoUseCase.criar(any())).thenThrow(new ProdutoInativoException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PRODUTO_INATIVO"));
    }

    @Test
    void retorna422QuandoEstoqueInsuficiente() throws Exception {
        when(criarPedidoUseCase.criar(any())).thenThrow(new EstoqueInsuficienteException(1L));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ESTOQUE_INSUFICIENTE"));
    }

    @Test
    void retorna502QuandoFreteIndisponivel() throws Exception {
        when(criarPedidoUseCase.criar(any())).thenThrow(new FreteIndisponivelException("timeout"));

        mockMvc.perform(post("/pedidos")
                        .contentType("application/json")
                        .content("""
                                {"cep":"01310100","itens":[{"produtoId":1,"quantidade":2}]}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value("FRETE_INDISPONIVEL"));
    }

    @Test
    void listaPedidos() throws Exception {
        when(listarPedidosUseCase.listarTodos()).thenReturn(List.of(pedidoExemplo()));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void buscaPedidoPorId() throws Exception {
        when(buscarPedidoUseCase.buscarPorId(1L)).thenReturn(pedidoExemplo());

        mockMvc.perform(get("/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01310100"));
    }

    @Test
    void retorna404QuandoPedidoNaoEncontrado() throws Exception {
        when(buscarPedidoUseCase.buscarPorId(99L)).thenThrow(new PedidoNaoEncontradoException(99L));

        mockMvc.perform(get("/pedidos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_NAO_ENCONTRADO"));
    }

    @Test
    void cancelaPedido() throws Exception {
        Pedido cancelado = new Pedido(1L, "01310100", StatusPedido.CANCELADO,
                List.of(new ItemPedido(1L, 2, new BigDecimal("59.90"))), new BigDecimal("25.50"), 5);
        when(cancelarPedidoUseCase.cancelar(1L)).thenReturn(cancelado);

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void retorna409QuandoPedidoJaCancelado() throws Exception {
        when(cancelarPedidoUseCase.cancelar(1L)).thenThrow(new PedidoJaCanceladoException(1L));

        mockMvc.perform(post("/pedidos/1/cancelamento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_JA_CANCELADO"));
    }
}
