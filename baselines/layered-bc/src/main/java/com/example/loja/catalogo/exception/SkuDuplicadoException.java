package com.example.loja.catalogo.exception;

import com.example.loja.shared.exception.DomainException;
import org.springframework.http.HttpStatus;

public class SkuDuplicadoException extends DomainException {

    public SkuDuplicadoException(String sku) {
        super(HttpStatus.CONFLICT, "SKU_DUPLICADO", "SKU já cadastrado: " + sku);
    }
}
