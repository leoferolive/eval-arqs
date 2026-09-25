package com.example.loja;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LojaFluxoTest {

    static WireMockServer wm = new WireMockServer(options().dynamicPort());

    static {
        wm.start();
        wm.stubFor(WireMock.get(WireMock.urlPathEqualTo("/fretes")).withQueryParam("cep", WireMock.equalTo("01310100"))
                .willReturn(WireMock.okJson("{\"valor\": 25.50, \"prazoDias\": 5}")));
        wm.stubFor(WireMock.get(WireMock.urlPathEqualTo("/fretes")).withQueryParam("cep", WireMock.equalTo("99999999"))
                .willReturn(WireMock.aResponse().withStatus(500)));
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) { r.add("frete.api.url", wm::baseUrl); }

    @AfterAll
    static void stop() { wm.stop(); }

    @Autowired MockMvc mvc;

    ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    String produto(String sku, int estoque) {
        return "{\"sku\":\"" + sku + "\",\"nome\":\"Camiseta\",\"preco\":59.90,\"estoque\":" + estoque + "}";
    }

    int criarProduto(String sku, int estoque) throws Exception {
        String json = postJson("/produtos", produto(sku, estoque)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Integer.parseInt(json.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void crudDeProduto() throws Exception {
        int id = criarProduto("A-1", 10);
        postJson("/produtos", produto("A-1", 1)).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("SKU_DUPLICADO"));
        postJson("/produtos", "{\"sku\":\"\"}").andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("VALIDACAO"));
        mvc.perform(get("/produtos")).andExpect(status().isOk());
        mvc.perform(get("/produtos/" + id)).andExpect(jsonPath("$.sku").value("A-1"));
        mvc.perform(put("/produtos/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"N\",\"preco\":10.00,\"estoque\":3,\"ativo\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(delete("/produtos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/produtos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void fluxoDePedido() throws Exception {
        int produto = criarProduto("B-1", 10);
        String json = postJson("/pedidos", "{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":" + produto + ",\"quantidade\":3}]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(205.20))
                .andReturn().getResponse().getContentAsString();
        String pedido = json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
        mvc.perform(get("/produtos/" + produto)).andExpect(jsonPath("$.estoque").value(7));
        mvc.perform(get("/pedidos")).andExpect(status().isOk());
        mvc.perform(get("/pedidos/" + pedido)).andExpect(jsonPath("$.status").value("CRIADO"));
        mvc.perform(post("/pedidos/" + pedido + "/cancelamento")).andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(get("/produtos/" + produto)).andExpect(jsonPath("$.estoque").value(10));
        mvc.perform(post("/pedidos/" + pedido + "/cancelamento")).andExpect(status().isConflict());
        mvc.perform(get("/pedidos/999999")).andExpect(status().isNotFound());
    }

    @Test
    void errosDePedido() throws Exception {
        int produto = criarProduto("C-1", 1);
        postJson("/pedidos", "{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":999999,\"quantidade\":1}]}")
                .andExpect(status().is(422)).andExpect(jsonPath("$.codigo").value("PRODUTO_INEXISTENTE"));
        postJson("/pedidos", "{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":" + produto + ",\"quantidade\":2}]}")
                .andExpect(status().is(422)).andExpect(jsonPath("$.codigo").value("ESTOQUE_INSUFICIENTE"));
        postJson("/pedidos", "{\"cep\":\"99999999\",\"itens\":[{\"produtoId\":" + produto + ",\"quantidade\":1}]}")
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.codigo").value("FRETE_INDISPONIVEL"));
        mvc.perform(put("/produtos/" + produto).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"N\",\"preco\":10.00,\"estoque\":1,\"ativo\":false}")).andExpect(status().isOk());
        postJson("/pedidos", "{\"cep\":\"01310100\",\"itens\":[{\"produtoId\":" + produto + ",\"quantidade\":1}]}")
                .andExpect(status().is(422)).andExpect(jsonPath("$.codigo").value("PRODUTO_INATIVO"));
    }
}
