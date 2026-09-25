package com.example.loja.usecase.produto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.Produto;
import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.usecase.gateway.ProdutoGateway;

@ExtendWith(MockitoExtension.class)
class BuscarProdutoInteractorTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @Test
    void retornaProdutoQuandoExiste() {
        when(produtoGateway.buscarPorId(1L))
                .thenReturn(Optional.of(new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true)));

        var interactor = new BuscarProdutoInteractor(produtoGateway);
        ProdutoOutput output = interactor.executar(1L);

        assertThat(output.id()).isEqualTo(1L);
    }

    @Test
    void lancaExcecaoQuandoNaoExiste() {
        when(produtoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        var interactor = new BuscarProdutoInteractor(produtoGateway);

        assertThatThrownBy(() -> interactor.executar(99L)).isInstanceOf(ProdutoNaoEncontradoException.class);
    }
}
