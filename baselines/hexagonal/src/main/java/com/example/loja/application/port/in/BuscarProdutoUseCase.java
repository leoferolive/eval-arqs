package com.example.loja.application.port.in;

import com.example.loja.domain.catalogo.Produto;

public interface BuscarProdutoUseCase {

    Produto buscarPorId(long id);
}
