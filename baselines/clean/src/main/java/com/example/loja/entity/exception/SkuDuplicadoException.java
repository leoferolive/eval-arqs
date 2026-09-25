package com.example.loja.entity.exception;

public class SkuDuplicadoException extends DomainException {

    public SkuDuplicadoException(String sku) {
        super("SKU_DUPLICADO", "SKU já cadastrado: " + sku);
    }
}
