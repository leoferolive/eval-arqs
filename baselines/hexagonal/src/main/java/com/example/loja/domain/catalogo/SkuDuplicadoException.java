package com.example.loja.domain.catalogo;

public class SkuDuplicadoException extends RuntimeException {

    public SkuDuplicadoException(String sku) {
        super("SKU já cadastrado: " + sku);
    }
}
