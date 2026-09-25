package com.example.loja.e2e;

import com.example.loja.adapter.in.web.dto.ProdutoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProdutoE2ETest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void fluxoCompletoDeCrudDeProduto() {
        Map<String, Object> corpoCriacao = Map.of(
                "sku", "CAM-E2E-1", "nome", "Camiseta E2E", "preco", 59.90, "estoque", 10);
        ResponseEntity<ProdutoResponse> criado = restTemplate.postForEntity(url("/produtos"), corpoCriacao, ProdutoResponse.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = criado.getBody().id();

        ResponseEntity<ProdutoResponse> buscado = restTemplate.getForEntity(url("/produtos/" + id), ProdutoResponse.class);
        assertThat(buscado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(buscado.getBody().sku()).isEqualTo("CAM-E2E-1");

        ResponseEntity<ProdutoResponse[]> lista = restTemplate.getForEntity(url("/produtos"), ProdutoResponse[].class);
        assertThat(lista.getBody()).extracting(ProdutoResponse::id).contains(id);

        Map<String, Object> corpoAtualizacao = Map.of(
                "nome", "Camiseta E2E Atualizada", "preco", 39.90, "estoque", 3, "ativo", false);
        restTemplate.put(url("/produtos/" + id), corpoAtualizacao);

        ResponseEntity<ProdutoResponse> atualizado = restTemplate.getForEntity(url("/produtos/" + id), ProdutoResponse.class);
        assertThat(atualizado.getBody().nome()).isEqualTo("Camiseta E2E Atualizada");
        assertThat(atualizado.getBody().preco()).isEqualByComparingTo(new BigDecimal("39.90"));
        assertThat(atualizado.getBody().ativo()).isFalse();

        restTemplate.delete(url("/produtos/" + id));

        ResponseEntity<String> apagado = restTemplate.getForEntity(url("/produtos/" + id), String.class);
        assertThat(apagado.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void retorna409QuandoSkuDuplicado() {
        Map<String, Object> corpo = Map.of("sku", "CAM-DUP", "nome", "Camiseta", "preco", 10.00, "estoque", 1);
        restTemplate.postForEntity(url("/produtos"), corpo, ProdutoResponse.class);

        ResponseEntity<String> segunda = restTemplate.postForEntity(url("/produtos"), corpo, String.class);

        assertThat(segunda.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(segunda.getBody()).contains("SKU_DUPLICADO");
    }
}
