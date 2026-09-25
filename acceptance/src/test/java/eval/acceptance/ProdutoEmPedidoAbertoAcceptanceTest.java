package eval.acceptance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/** T3 — produto com pedido em aberto não pode ser removido nem desativado. */
@Tag("t3")
class ProdutoEmPedidoAbertoAcceptanceTest extends AcceptanceSupport {

    static final String CEP = "01310100";

    @Test
    void naoRemoveProdutoComPedidoAberto() {
        stubFrete(CEP, "10.00", 2);
        long produto = criarProduto("5.00", 10);
        criarPedido(CEP, produto, 1);
        api().delete("/produtos/{id}", produto)
                .then().statusCode(409).body("codigo", equalTo("PRODUTO_EM_PEDIDO_ABERTO"));
        api().get("/produtos/{id}", produto).then().statusCode(200);
    }

    @Test
    void naoDesativaProdutoComPedidoAbertoMasPermiteOutrasAlteracoes() {
        stubFrete(CEP, "10.00", 2);
        long produto = criarProduto("5.00", 10);
        criarPedido(CEP, produto, 1);
        api().body(Map.of("nome", "X", "preco", new BigDecimal("5.00"), "estoque", 9, "ativo", false))
                .put("/produtos/{id}", produto)
                .then().statusCode(409).body("codigo", equalTo("PRODUTO_EM_PEDIDO_ABERTO"));
        api().body(Map.of("nome", "Renomeado", "preco", new BigDecimal("6.00"), "estoque", 9, "ativo", true))
                .put("/produtos/{id}", produto)
                .then().statusCode(200).body("nome", equalTo("Renomeado"));
    }

    @Test
    void pedidoCanceladoNaoBloqueia() {
        stubFrete(CEP, "10.00", 2);
        long produto = criarProduto("5.00", 10);
        long pedido = criarPedido(CEP, produto, 1);
        api().post("/pedidos/{id}/cancelamento", pedido).then().statusCode(200);
        api().delete("/produtos/{id}", produto).then().statusCode(204);
    }
}
