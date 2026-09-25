package com.example.loja.application.port.in;

import com.example.loja.domain.catalogo.Produto;

import java.util.List;

public interface ListarProdutosUseCase {

    List<Produto> listarTodos();
}
