package com.example.loja.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProdutoE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fluxoCompletoDeProduto() {
        Map<String, Object> criar = Map.of(
                "sku", "CAM-E2E-1",
                "nome", "Camiseta E2E",
                "preco", 59.90,
                "estoque", 10
        );

        ResponseEntity<Map> criado = restTemplate.postForEntity("/produtos", criar, Map.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number id = (Number) criado.getBody().get("id");
        assertThat(criado.getBody().get("ativo")).isEqualTo(true);

        ResponseEntity<Map> duplicado = restTemplate.postForEntity("/produtos", criar, Map.class);
        assertThat(duplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicado.getBody().get("codigo")).isEqualTo("SKU_DUPLICADO");

        ResponseEntity<List> lista = restTemplate.getForEntity("/produtos", List.class);
        assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lista.getBody()).isNotEmpty();

        ResponseEntity<Map> buscado = restTemplate.getForEntity("/produtos/" + id, Map.class);
        assertThat(buscado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(buscado.getBody().get("sku")).isEqualTo("CAM-E2E-1");

        Map<String, Object> atualizacao = new LinkedHashMap<>();
        atualizacao.put("nome", "Camiseta Atualizada");
        atualizacao.put("preco", 79.90);
        atualizacao.put("estoque", 20);
        atualizacao.put("ativo", false);

        restTemplate.put("/produtos/" + id, atualizacao);
        ResponseEntity<Map> atualizado = restTemplate.getForEntity("/produtos/" + id, Map.class);
        assertThat(atualizado.getBody().get("nome")).isEqualTo("Camiseta Atualizada");
        assertThat(atualizado.getBody().get("ativo")).isEqualTo(false);

        restTemplate.delete("/produtos/" + id);
        ResponseEntity<Map> aposDelete = restTemplate.getForEntity("/produtos/" + id, Map.class);
        assertThat(aposDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(aposDelete.getBody().get("codigo")).isEqualTo("PRODUTO_NAO_ENCONTRADO");
    }

    @Test
    void criarDeveRetornar400QuandoCorpoInvalido() {
        Map<String, Object> invalido = Map.of("sku", "", "nome", "", "preco", -1, "estoque", -1);

        ResponseEntity<Map> resposta = restTemplate.postForEntity("/produtos", invalido, Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo")).isEqualTo("VALIDACAO");
    }
}
