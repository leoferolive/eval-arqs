package com.example.loja.pedidos.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class ProdutoInexistenteException extends DomainException {

    public ProdutoInexistenteException(Long produtoId) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "PRODUTO_INEXISTENTE", "Produto inexistente: " + produtoId);
    }
}
