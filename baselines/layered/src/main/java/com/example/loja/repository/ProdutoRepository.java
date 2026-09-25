package com.example.loja.repository;

import com.example.loja.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsBySku(String sku);

    List<Produto> findAllByOrderByIdAsc();

    Optional<Produto> findById(Long id);
}
