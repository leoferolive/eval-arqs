package com.example.loja.domain.pedidos;

public class FreteIndisponivelException extends RuntimeException {

    public FreteIndisponivelException(String motivo) {
        super("Frete indisponível: " + motivo);
    }
}
