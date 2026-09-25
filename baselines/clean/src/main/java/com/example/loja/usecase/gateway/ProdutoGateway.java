package com.example.loja.usecase.gateway;

import java.util.List;
import java.util.Optional;

import com.example.loja.entity.Produto;

public interface ProdutoGateway {

    Produto salvar(Produto produto);

    Optional<Produto> buscarPorId(Long id);

    List<Produto> listarTodos();

    boolean existePorSku(String sku);

    boolean existePorId(Long id);

    void remover(Long id);
}
