package com.example.loja.usecase.produto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
class AtualizarProdutoInteractorTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @Test
    void atualizaProdutoExistente() {
        Produto existente = new Produto(1L, "SKU-1", "Camiseta", new BigDecimal("59.90"), 10, true);
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(produtoGateway.salvar(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        var interactor = new AtualizarProdutoInteractor(produtoGateway);
        ProdutoOutput output = interactor
                .executar(new AtualizarProdutoInput(1L, "Nova", new BigDecimal("10.00"), 3, false));

        assertThat(output.nome()).isEqualTo("Nova");
        assertThat(output.preco()).isEqualByComparingTo("10.00");
        assertThat(output.estoque()).isEqualTo(3);
        assertThat(output.ativo()).isFalse();
    }

    @Test
    void lancaExcecaoQuandoProdutoNaoExiste() {
        when(produtoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        var interactor = new AtualizarProdutoInteractor(produtoGateway);

        assertThatThrownBy(() -> interactor
                .executar(new AtualizarProdutoInput(99L, "Nova", new BigDecimal("10.00"), 3, false)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }
}
