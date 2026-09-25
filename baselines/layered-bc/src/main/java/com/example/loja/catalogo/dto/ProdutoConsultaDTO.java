package com.example.loja.catalogo.dto;

import java.math.BigDecimal;

/**
 * DTO de alto nível exposto a outros contextos (ex.: pedidos) para consultar
 * disponibilidade de um produto sem acessar a entidade JPA diretamente.
 */
public record ProdutoConsultaDTO(
        Long id,
        BigDecimal preco,
        Integer estoque,
        boolean ativo
) {
}
