package com.example.loja.usecase.produto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.Produto;
import com.example.loja.entity.exception.SkuDuplicadoException;
import com.example.loja.usecase.gateway.ProdutoGateway;

@ExtendWith(MockitoExtension.class)
class CriarProdutoInteractorTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @Test
    void criaProdutoQuandoSkuNaoExiste() {
        when(produtoGateway.existePorSku("SKU-1")).thenReturn(false);
        when(produtoGateway.salvar(any(Produto.class)))
                .thenAnswer(inv -> new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true));

        var interactor = new CriarProdutoInteractor(produtoGateway);
        ProdutoOutput output = interactor
                .executar(new CriarProdutoInput("SKU-1", "Camiseta", new BigDecimal("59.90"), 10));

        assertThat(output.id()).isEqualTo(1L);
        assertThat(output.sku()).isEqualTo("SKU-1");
        assertThat(output.ativo()).isTrue();
    }

    @Test
    void lancaExcecaoQuandoSkuDuplicado() {
        when(produtoGateway.existePorSku("SKU-1")).thenReturn(true);

        var interactor = new CriarProdutoInteractor(produtoGateway);

        assertThatThrownBy(() -> interactor
                .executar(new CriarProdutoInput("SKU-1", "Camiseta", new BigDecimal("59.90"), 10)))
                .isInstanceOf(SkuDuplicadoException.class);
    }
}
