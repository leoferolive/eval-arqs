package com.example.loja.pedidos.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class ProdutoInativoException extends DomainException {

    public ProdutoInativoException(Long produtoId) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "PRODUTO_INATIVO", "Produto inativo: " + produtoId);
    }
}
