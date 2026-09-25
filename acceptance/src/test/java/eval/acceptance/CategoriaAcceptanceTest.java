package eval.acceptance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

/** T1 — categoria de produto. */
@Tag("t1")
class CategoriaAcceptanceTest extends AcceptanceSupport {

    @Test
    void categoriaPadraoEhGeral() {
        api().body(Map.of("sku", novoSku(), "nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1))
                .post("/produtos").then().statusCode(201).body("categoria", equalTo("GERAL"));
    }

    @Test
    void criaEAtualizaCategoria() {
        Number id = api().body(Map.of("sku", novoSku(), "nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1, "categoria", "ROUPAS"))
                .post("/produtos").then().statusCode(201).body("categoria", equalTo("ROUPAS")).extract().path("id");
        api().body(Map.of("nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1, "ativo", true, "categoria", "CALCADOS"))
                .put("/produtos/{id}", id).then().statusCode(200).body("categoria", equalTo("CALCADOS"));
        api().body(Map.of("nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1, "ativo", true))
                .put("/produtos/{id}", id).then().statusCode(200).body("categoria", equalTo("GERAL"));
    }

    @Test
    void categoriaMuitoLongaRetorna400() {
        api().body(Map.of("sku", novoSku(), "nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1, "categoria", "C".repeat(41)))
                .post("/produtos").then().statusCode(400).body("codigo", equalTo("VALIDACAO"));
    }

    @Test
    void filtraPorCategoriaSemDiferenciarCaixa() {
        String categoria = "CAT" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        api().body(Map.of("sku", novoSku(), "nome", "X", "preco", new BigDecimal("1.00"), "estoque", 1, "categoria", categoria))
                .post("/produtos").then().statusCode(201);
        api().body(Map.of("sku", novoSku(), "nome", "Y", "preco", new BigDecimal("1.00"), "estoque", 1, "categoria", "OUTRA"))
                .post("/produtos").then().statusCode(201);

        List<String> categorias = api().queryParam("categoria", categoria.toLowerCase()).get("/produtos")
                .then().statusCode(200).body("$", hasSize(1)).extract().path("categoria");
        org.junit.jupiter.api.Assertions.assertEquals(List.of(categoria), categorias);
    }
}
