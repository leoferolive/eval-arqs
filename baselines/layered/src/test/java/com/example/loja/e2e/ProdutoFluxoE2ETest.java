package com.example.loja.e2e;

import com.example.loja.dto.ErroResponse;
import com.example.loja.dto.ProdutoRequest;
import com.example.loja.dto.ProdutoResponse;
import com.example.loja.dto.ProdutoUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProdutoFluxoE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String caminho) {
        return "http://localhost:" + port + caminho;
    }

    @Test
    void fluxoCompletoDeCrudDeProduto() {
        ProdutoRequest request = new ProdutoRequest("SKU-CRUD-1", "Produto CRUD", new BigDecimal("30.00"), 15);
        ResponseEntity<ProdutoResponse> criacao = restTemplate.postForEntity(url("/produtos"), request, ProdutoResponse.class);
        assertThat(criacao.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProdutoResponse criado = criacao.getBody();
        assertThat(criado.ativo()).isTrue();

        ResponseEntity<ErroResponse> duplicado = restTemplate.postForEntity(url("/produtos"), request, ErroResponse.class);
        assertThat(duplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicado.getBody().codigo()).isEqualTo("SKU_DUPLICADO");

        ResponseEntity<ProdutoResponse[]> lista = restTemplate.getForEntity(url("/produtos"), ProdutoResponse[].class);
        assertThat(lista.getBody()).extracting(ProdutoResponse::sku).contains("SKU-CRUD-1");

        ResponseEntity<ProdutoResponse> busca = restTemplate.getForEntity(url("/produtos/" + criado.id()), ProdutoResponse.class);
        assertThat(busca.getStatusCode()).isEqualTo(HttpStatus.OK);

        ProdutoUpdateRequest atualizacao = new ProdutoUpdateRequest("Produto Atualizado", new BigDecimal("45.00"), 8, false);
        ResponseEntity<ProdutoResponse> atualizado = restTemplate.exchange(
                url("/produtos/" + criado.id()), HttpMethod.PUT, new org.springframework.http.HttpEntity<>(atualizacao), ProdutoResponse.class);
        assertThat(atualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(atualizado.getBody().nome()).isEqualTo("Produto Atualizado");
        assertThat(atualizado.getBody().ativo()).isFalse();

        restTemplate.delete(url("/produtos/" + criado.id()));

        ResponseEntity<ErroResponse> buscaAposDelecao = restTemplate.getForEntity(url("/produtos/" + criado.id()), ErroResponse.class);
        assertThat(buscaAposDelecao.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(buscaAposDelecao.getBody().codigo()).isEqualTo("PRODUTO_NAO_ENCONTRADO");
    }

    @Test
    void criarProdutoComDadosInvalidosRetorna400() {
        ProdutoRequest request = new ProdutoRequest("", "", new BigDecimal("-1"), -1);
        ResponseEntity<ErroResponse> resposta = restTemplate.postForEntity(url("/produtos"), request, ErroResponse.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().codigo()).isEqualTo("VALIDACAO");
    }

    @Test
    void atualizarProdutoInexistenteRetorna404() {
        ProdutoUpdateRequest atualizacao = new ProdutoUpdateRequest("Nome", BigDecimal.TEN, 1, true);
        ResponseEntity<ErroResponse> resposta = restTemplate.exchange(
                url("/produtos/999999"), HttpMethod.PUT, new org.springframework.http.HttpEntity<>(atualizacao), ErroResponse.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().codigo()).isEqualTo("PRODUTO_NAO_ENCONTRADO");
    }

    @Test
    void deletarProdutoInexistenteRetorna404() {
        ResponseEntity<ErroResponse> resposta = restTemplate.exchange(
                url("/produtos/999999"), HttpMethod.DELETE, null, ErroResponse.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().codigo()).isEqualTo("PRODUTO_NAO_ENCONTRADO");
    }
}
