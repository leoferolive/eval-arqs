package eval.acceptance;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;

/**
 * Base dos testes de aceitação: a aplicação já está rodando em {@code base.url} e aponta a
 * API de frete para o WireMock iniciado aqui em {@code wiremock.port}.
 */
abstract class AcceptanceSupport {

    static WireMockServer wireMock;
    static final int FRETE_VERSAO = Integer.getInteger("frete.versao", 1);

    @BeforeAll
    static void setUp() {
        if (wireMock == null) {
            wireMock = new WireMockServer(options().port(Integer.getInteger("wiremock.port", 18089)));
            wireMock.start();
            Runtime.getRuntime().addShutdownHook(new Thread(wireMock::stop));
        }
        RestAssured.baseURI = System.getProperty("base.url", "http://localhost:18080");
        RestAssured.config = RestAssuredConfig.config()
                .jsonConfig(JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    /** Compara números inteiros independentemente do tipo devolvido pelo JsonPath (Integer, Long, BigDecimal). */
    static org.hamcrest.Matcher<Object> inteiro(long valor) {
        return org.hamcrest.Matchers.hasToString(String.valueOf(valor));
    }

    static RequestSpecification api() {
        return given().contentType(ContentType.JSON).accept(ContentType.JSON);
    }

    static String novoSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8);
    }

    static long criarProduto(String preco, int estoque) {
        Number id = api().body(Map.of("sku", novoSku(), "nome", "Produto teste", "preco", new BigDecimal(preco), "estoque", estoque))
                .post("/produtos").then().statusCode(201).extract().path("id");
        return id.longValue();
    }

    static int estoqueDe(long produtoId) {
        Number estoque = api().get("/produtos/{id}", produtoId).then().statusCode(200).extract().path("estoque");
        return estoque.intValue();
    }

    static void desativar(long produtoId) {
        Map<String, Object> atual = api().get("/produtos/{id}", produtoId).then().extract().path("$");
        api().body(Map.of("nome", atual.get("nome"), "preco", atual.get("preco"), "estoque", atual.get("estoque"), "ativo", false))
                .put("/produtos/{id}", produtoId).then().statusCode(200);
    }

    // ---- API de frete (v1: GET /fretes?cep= | v2: POST /v2/cotacoes) ----

    static void stubFrete(String cep, String valor, int prazo) {
        if (FRETE_VERSAO == 2) {
            wireMock.stubFor(post(urlPathEqualTo("/v2/cotacoes"))
                    .withRequestBody(matchingJsonPath("$.cepDestino", equalTo(cep)))
                    .willReturn(okJson("{\"preco\": " + valor + ", \"prazoEmDias\": " + prazo + "}")));
        } else {
            wireMock.stubFor(get(urlPathEqualTo("/fretes")).withQueryParam("cep", equalTo(cep))
                    .willReturn(okJson("{\"valor\": " + valor + ", \"prazoDias\": " + prazo + "}")));
        }
    }

    static void stubFreteErro(String cep, int status) {
        if (FRETE_VERSAO == 2) {
            wireMock.stubFor(post(urlPathEqualTo("/v2/cotacoes"))
                    .withRequestBody(matchingJsonPath("$.cepDestino", equalTo(cep)))
                    .willReturn(aResponse().withStatus(status)));
        } else {
            wireMock.stubFor(get(urlPathEqualTo("/fretes")).withQueryParam("cep", equalTo(cep))
                    .willReturn(aResponse().withStatus(status)));
        }
    }

    static void stubFreteLento(String cep, int atrasoMs) {
        if (FRETE_VERSAO == 2) {
            wireMock.stubFor(post(urlPathEqualTo("/v2/cotacoes"))
                    .withRequestBody(matchingJsonPath("$.cepDestino", equalTo(cep)))
                    .willReturn(okJson("{\"preco\": 10.00, \"prazoEmDias\": 1}").withFixedDelay(atrasoMs)));
        } else {
            wireMock.stubFor(get(urlPathEqualTo("/fretes")).withQueryParam("cep", equalTo(cep))
                    .willReturn(okJson("{\"valor\": 10.00, \"prazoDias\": 1}").withFixedDelay(atrasoMs)));
        }
    }

    static Map<String, Object> pedido(String cep, long produtoId, int quantidade) {
        return Map.of("cep", cep, "itens", java.util.List.of(Map.of("produtoId", produtoId, "quantidade", quantidade)));
    }

    static long criarPedido(String cep, long produtoId, int quantidade) {
        Number id = api().body(pedido(cep, produtoId, quantidade)).post("/pedidos")
                .then().statusCode(201).extract().path("id");
        return id.longValue();
    }
}
