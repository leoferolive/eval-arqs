package com.example.loja.catalogo.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class ProdutoNaoEncontradoException extends DomainException {

    public ProdutoNaoEncontradoException(Long id) {
        super(HttpStatus.NOT_FOUND, "PRODUTO_NAO_ENCONTRADO", "Produto não encontrado: " + id);
    }
}
