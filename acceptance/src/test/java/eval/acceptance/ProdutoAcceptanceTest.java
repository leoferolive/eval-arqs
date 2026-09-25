package eval.acceptance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@Tag("base")
class ProdutoAcceptanceTest extends AcceptanceSupport {

    @Test
    void criaEConsultaProduto() {
        String sku = novoSku();
        Number id = api().body(Map.of("sku", sku, "nome", "Camiseta", "preco", new BigDecimal("59.90"), "estoque", 10))
                .post("/produtos")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("sku", equalTo(sku))
                .body("nome", equalTo("Camiseta"))
                .body("preco", comparesEqualTo(new BigDecimal("59.90")))
                .body("estoque", inteiro(10))
                .body("ativo", equalTo(true))
                .extract().path("id");

        api().get("/produtos/{id}", id).then().statusCode(200).body("sku", equalTo(sku));
    }

    @Test
    void listaProdutosOrdenadosPorId() {
        long a = criarProduto("1.00", 1);
        long b = criarProduto("2.00", 1);
        List<Number> ids = api().get("/produtos").then().statusCode(200).extract().path("id");
        List<Long> longs = ids.stream().map(Number::longValue).toList();
        org.junit.jupiter.api.Assertions.assertTrue(longs.contains(a) && longs.contains(b));
        org.junit.jupiter.api.Assertions.assertEquals(longs.stream().sorted().toList(), longs);
    }

    @Test
    void skuDuplicadoRetorna409() {
        String sku = novoSku();
        Map<String, Object> body = Map.of("sku", sku, "nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1);
        api().body(body).post("/produtos").then().statusCode(201);
        api().body(body).post("/produtos").then().statusCode(409).body("codigo", equalTo("SKU_DUPLICADO"));
    }

    @Test
    void validacoesRetornam400() {
        api().body(Map.of("sku", "", "nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1))
                .post("/produtos").then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
        api().body(Map.of("sku", novoSku(), "nome", "X", "preco", BigDecimal.ZERO, "estoque", 1))
                .post("/produtos").then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
        api().body(Map.of("sku", novoSku(), "nome", "X", "preco", new BigDecimal("1.00"), "estoque", -1))
                .post("/produtos").then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
        api().body(Map.of("sku", "S".repeat(31), "nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1))
                .post("/produtos").then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
        api().body(Map.of("sku", novoSku(), "preco", new BigDecimal("1.00"), "estoque", 1))
                .post("/produtos").then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
    }

    @Test
    void atualizaProdutoMantendoSku() {
        long id = criarProduto("10.00", 5);
        String sku = api().get("/produtos/{id}", id).then().extract().path("sku");
        api().body(Map.of("nome", "Novo nome", "preco", new BigDecimal("12.50"), "estoque", 7, "ativo", false))
                .put("/produtos/{id}", id)
                .then().statusCode(200)
                .body("sku", equalTo(sku))
                .body("nome", equalTo("Novo nome"))
                .body("preco", comparesEqualTo(new BigDecimal("12.50")))
                .body("estoque", inteiro(7))
                .body("ativo", equalTo(false));
    }

    @Test
    void atualizaComCorpoInvalidoRetorna400() {
        long id = criarProduto("10.00", 5);
        api().body(Map.of("nome", "X", "preco", new BigDecimal("-1"), "estoque", 7, "ativo", true))
                .put("/produtos/{id}", id).then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
    }

    @Test
    void removeProduto() {
        long id = criarProduto("10.00", 5);
        api().delete("/produtos/{id}", id).then().statusCode(204);
        api().get("/produtos/{id}", id).then().statusCode(404).body("codigo", equalTo("PRODUTO_NAO_ENCONTRADO"));
    }

    @Test
    void produtoInexistenteRetorna404() {
        api().get("/produtos/{id}", 999_999).then().statusCode(404).body("codigo", equalTo("PRODUTO_NAO_ENCONTRADO"));
        api().body(Map.of("nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1, "ativo", true))
                .put("/produtos/{id}", 999_999).then().statusCode(404).body("codigo", equalTo("PRODUTO_NAO_ENCONTRADO"));
        api().delete("/produtos/{id}", 999_999).then().statusCode(404).body("codigo", equalTo("PRODUTO_NAO_ENCONTRADO"));
    }
}
