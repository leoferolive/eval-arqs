package com.example.loja.catalogo.repository;

import com.example.loja.catalogo.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findBySku(String sku);

    List<Produto> findAllByOrderByIdAsc();
}
