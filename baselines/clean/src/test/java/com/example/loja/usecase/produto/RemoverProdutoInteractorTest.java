package com.example.loja.usecase.produto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.usecase.gateway.ProdutoGateway;

@ExtendWith(MockitoExtension.class)
class RemoverProdutoInteractorTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @Test
    void removeProdutoExistente() {
        when(produtoGateway.existePorId(1L)).thenReturn(true);

        var interactor = new RemoverProdutoInteractor(produtoGateway);
        interactor.executar(1L);

        verify(produtoGateway).remover(1L);
    }

    @Test
    void lancaExcecaoQuandoProdutoNaoExiste() {
        when(produtoGateway.existePorId(99L)).thenReturn(false);

        var interactor = new RemoverProdutoInteractor(produtoGateway);

        assertThatThrownBy(() -> interactor.executar(99L)).isInstanceOf(ProdutoNaoEncontradoException.class);
    }
}
