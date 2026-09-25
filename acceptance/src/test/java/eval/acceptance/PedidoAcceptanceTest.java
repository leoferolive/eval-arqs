package eval.acceptance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("base")
class PedidoAcceptanceTest extends AcceptanceSupport {

    static final String CEP_OK = "01310100";

    @Test
    void criaPedidoCalculandoTotaisEBaixandoEstoque() {
        stubFrete(CEP_OK, "25.50", 5);
        long produto = criarProduto("59.90", 10);

        Number id = api().body(pedido(CEP_OK, produto, 2)).post("/pedidos")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("cep", equalTo(CEP_OK))
                .body("status", equalTo("CRIADO"))
                .body("itens", hasSize(1))
                .body("itens[0].produtoId", inteiro(produto))
                .body("itens[0].quantidade", inteiro(2))
                .body("itens[0].precoUnitario", comparesEqualTo(new BigDecimal("59.90")))
                .body("itens[0].subtotal", comparesEqualTo(new BigDecimal("119.80")))
                .body("valorItens", comparesEqualTo(new BigDecimal("119.80")))
                .body("valorFrete", comparesEqualTo(new BigDecimal("25.50")))
                .body("prazoEntregaDias", inteiro(5))
                .body("valorTotal", comparesEqualTo(new BigDecimal("145.30")))
                .extract().path("id");

        assertEquals(8, estoqueDe(produto));
        api().get("/pedidos/{id}", id).then().statusCode(200).body("status", equalTo("CRIADO"));
    }

    @Test
    void pedidoComVariosItens() {
        stubFrete(CEP_OK, "25.50", 5);
        long p1 = criarProduto("10.00", 5);
        long p2 = criarProduto("3.33", 5);
        api().body(Map.of("cep", CEP_OK, "itens", List.of(
                        Map.of("produtoId", p1, "quantidade", 1),
                        Map.of("produtoId", p2, "quantidade", 3))))
                .post("/pedidos")
                .then().statusCode(201)
                .body("itens", hasSize(2))
                .body("valorItens", comparesEqualTo(new BigDecimal("19.99")))
                .body("valorTotal", comparesEqualTo(new BigDecimal("45.49")));
        assertEquals(4, estoqueDe(p1));
        assertEquals(2, estoqueDe(p2));
    }

    @Test
    void precoUnitarioEhCongeladoNaCriacao() {
        stubFrete(CEP_OK, "25.50", 5);
        long produto = criarProduto("10.00", 5);
        long pedidoId = criarPedido(CEP_OK, produto, 1);
        api().body(Map.of("nome", "X", "preco", new BigDecimal("99.00"), "estoque", 4, "ativo", true))
                .put("/produtos/{id}", produto).then().statusCode(200);
        api().get("/pedidos/{id}", pedidoId).then().statusCode(200)
                .body("itens[0].precoUnitario", comparesEqualTo(new BigDecimal("10.00")));
    }

    @Test
    void listaPedidosOrdenadosPorId() {
        stubFrete(CEP_OK, "25.50", 5);
        long produto = criarProduto("1.00", 5);
        long a = criarPedido(CEP_OK, produto, 1);
        long b = criarPedido(CEP_OK, produto, 1);
        List<Number> ids = api().get("/pedidos").then().statusCode(200).extract().path("id");
        List<Long> longs = ids.stream().map(Number::longValue).toList();
        org.junit.jupiter.api.Assertions.assertTrue(longs.contains(a) && longs.contains(b));
        assertEquals(longs.stream().sorted().toList(), longs);
    }

    @Test
    void validacoesDePedidoRetornam400() {
        long produto = criarProduto("1.00", 5);
        api().body(pedido("123", produto, 1)).post("/pedidos")
                .then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
        api().body(pedido("0131010A", produto, 1)).post("/pedidos")
                .then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
        api().body(pedido(CEP_OK, produto, 0)).post("/pedidos")
                .then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
        api().body(Map.of("cep", CEP_OK, "itens", List.of())).post("/pedidos")
                .then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
    }

    @Test
    void produtoInexistenteRetorna422() {
        stubFrete(CEP_OK, "25.50", 5);
        api().body(pedido(CEP_OK, 999_999, 1)).post("/pedidos")
                .then().statusCode(422).body("codigo", equalTo("PRODUTO_INEXISTENTE"));
    }

    @Test
    void produtoInativoRetorna422() {
        stubFrete(CEP_OK, "25.50", 5);
        long produto = criarProduto("1.00", 5);
        desativar(produto);
        api().body(pedido(CEP_OK, produto, 1)).post("/pedidos")
                .then().statusCode(422).body("codigo", equalTo("PRODUTO_INATIVO"));
        assertEquals(5, estoqueDe(produto));
    }

    @Test
    void estoqueInsuficienteRetorna422SemAlterarEstoque() {
        stubFrete(CEP_OK, "25.50", 5);
        long produto = criarProduto("1.00", 2);
        api().body(pedido(CEP_OK, produto, 3)).post("/pedidos")
                .then().statusCode(422).body("codigo", equalTo("ESTOQUE_INSUFICIENTE"));
        assertEquals(2, estoqueDe(produto));
    }

    @Test
    void falhaNaApiDeFreteRetorna502SemPersistir() {
        String cep = "99999001";
        stubFreteErro(cep, 500);
        long produto = criarProduto("1.00", 5);
        int pedidosAntes = api().get("/pedidos").then().extract().path("size()");
        api().body(pedido(cep, produto, 1)).post("/pedidos")
                .then().statusCode(502).body("codigo", equalTo("FRETE_INDISPONIVEL"));
        assertEquals(5, estoqueDe(produto));
        int pedidosDepois = api().get("/pedidos").then().extract().path("size()");
        assertEquals(pedidosAntes, pedidosDepois);
    }

    @Test
    void timeoutNaApiDeFreteRetorna502() {
        String cep = "99999002";
        stubFreteLento(cep, 3500);
        long produto = criarProduto("1.00", 5);
        api().body(pedido(cep, produto, 1)).post("/pedidos")
                .then().statusCode(502).body("codigo", equalTo("FRETE_INDISPONIVEL"));
        assertEquals(5, estoqueDe(produto));
    }

    @Test
    void cancelamentoDevolveEstoque() {
        stubFrete(CEP_OK, "25.50", 5);
        long produto = criarProduto("1.00", 10);
        long pedidoId = criarPedido(CEP_OK, produto, 3);
        assertEquals(7, estoqueDe(produto));

        api().post("/pedidos/{id}/cancelamento", pedidoId)
                .then().statusCode(200).body("status", equalTo("CANCELADO"));
        assertEquals(10, estoqueDe(produto));
        api().get("/pedidos/{id}", pedidoId).then().body("status", equalTo("CANCELADO"));
    }

    @Test
    void cancelarDuasVezesRetorna409() {
        stubFrete(CEP_OK, "25.50", 5);
        long produto = criarProduto("1.00", 10);
        long pedidoId = criarPedido(CEP_OK, produto, 3);
        api().post("/pedidos/{id}/cancelamento", pedidoId).then().statusCode(200);
        api().post("/pedidos/{id}/cancelamento", pedidoId)
                .then().statusCode(409).body("codigo", equalTo("PEDIDO_JA_CANCELADO"));
        assertEquals(10, estoqueDe(produto));
    }

    @Test
    void pedidoInexistenteRetorna404() {
        api().get("/pedidos/{id}", 999_999).then().statusCode(404).body("codigo", equalTo("PEDIDO_NAO_ENCONTRADO"));
        api().post("/pedidos/{id}/cancelamento", 999_999).then().statusCode(404).body("codigo", equalTo("PEDIDO_NAO_ENCONTRADO"));
    }
}
