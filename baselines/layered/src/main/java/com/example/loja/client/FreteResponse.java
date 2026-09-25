package com.example.loja.client;

import java.math.BigDecimal;

public record FreteResponse(
        BigDecimal valor,
        Integer prazoDias
) {
}
