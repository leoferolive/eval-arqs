package com.example.loja.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.loja.adapter.controller.dto.ErroResponse;
import com.example.loja.adapter.controller.dto.ProdutoResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CatalogoE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void fluxoCompletoDeProduto() {
        Map<String, Object> criar = Map.of("sku", "CAM-001", "nome", "Camiseta", "preco", 59.90, "estoque", 10);
        ResponseEntity<ProdutoResponse> criado = restTemplate.postForEntity(url("/produtos"), criar,
                ProdutoResponse.class);

        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(criado.getBody()).isNotNull();
        Long id = criado.getBody().id();
        assertThat(criado.getBody().ativo()).isTrue();

        ResponseEntity<ProdutoResponse> buscado = restTemplate.getForEntity(url("/produtos/" + id),
                ProdutoResponse.class);
        assertThat(buscado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(buscado.getBody().sku()).isEqualTo("CAM-001");

        ResponseEntity<ProdutoResponse[]> listado = restTemplate.getForEntity(url("/produtos"),
                ProdutoResponse[].class);
        assertThat(listado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listado.getBody()).extracting(ProdutoResponse::id).contains(id);

        Map<String, Object> atualizar = Map.of("nome", "Camiseta Premium", "preco", 79.90, "estoque", 20, "ativo",
                false);
        ResponseEntity<ProdutoResponse> atualizado = restTemplate.exchange(url("/produtos/" + id),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(atualizar), ProdutoResponse.class);
        assertThat(atualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(atualizado.getBody().nome()).isEqualTo("Camiseta Premium");
        assertThat(atualizado.getBody().ativo()).isFalse();

        restTemplate.delete(url("/produtos/" + id));

        ResponseEntity<ErroResponse> apos = restTemplate.getForEntity(url("/produtos/" + id), ErroResponse.class);
        assertThat(apos.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apos.getBody().codigo()).isEqualTo("PRODUTO_NAO_ENCONTRADO");
    }

    @Test
    void criarProdutoComSkuDuplicadoRetorna409() {
        Map<String, Object> criar = Map.of("sku", "CAM-002", "nome", "Camiseta", "preco", 59.90, "estoque", 10);
        restTemplate.postForEntity(url("/produtos"), criar, ProdutoResponse.class);

        ResponseEntity<ErroResponse> duplicado = restTemplate.postForEntity(url("/produtos"), criar,
                ErroResponse.class);

        assertThat(duplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicado.getBody().codigo()).isEqualTo("SKU_DUPLICADO");
    }

    @Test
    void criarProdutoComCorpoInvalidoRetorna400() {
        Map<String, Object> invalido = Map.of("sku", "", "nome", "Camiseta", "preco", -1, "estoque", -1);

        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/produtos"), invalido,
                ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().codigo()).isEqualTo("VALIDACAO");
    }

    @Test
    void removerProdutoInexistenteRetorna404() {
        ResponseEntity<ErroResponse> resposta = restTemplate.exchange(url("/produtos/999999"),
                org.springframework.http.HttpMethod.DELETE, null, ErroResponse.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
