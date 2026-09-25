package com.example.loja.usecase.produto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.Produto;
import com.example.loja.usecase.gateway.ProdutoGateway;

@ExtendWith(MockitoExtension.class)
class ListarProdutosInteractorTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @Test
    void retornaListaDeProdutos() {
        when(produtoGateway.listarTodos()).thenReturn(List.of(
                new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true),
                new Produto(2L, "SKU-2", "Calça", new BigDecimal("99.90"), 5, true)));

        var interactor = new ListarProdutosInteractor(produtoGateway);
        List<ProdutoOutput> output = interactor.executar();

        assertThat(output).hasSize(2);
        assertThat(output.get(0).sku()).isEqualTo("SKU-1");
        assertThat(output.get(1).sku()).isEqualTo("SKU-2");
    }
}
