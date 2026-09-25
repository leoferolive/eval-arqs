package com.example.loja.exception;

import org.springframework.http.HttpStatus;

public class SkuDuplicadoException extends ApiException {

    public SkuDuplicadoException(String sku) {
        super("SKU já cadastrado: " + sku);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getCodigo() {
        return "SKU_DUPLICADO";
    }
}
