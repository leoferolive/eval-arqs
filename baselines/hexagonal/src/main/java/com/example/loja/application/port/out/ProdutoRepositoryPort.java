package com.example.loja.application.port.out;

import com.example.loja.domain.catalogo.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepositoryPort {

    Produto salvar(Produto produto);

    Optional<Produto> buscarPorId(long id);

    Optional<Produto> buscarPorSku(String sku);

    List<Produto> listarTodos();

    boolean existePorId(long id);

    void excluir(long id);
}
