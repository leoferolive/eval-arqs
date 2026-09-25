package com.example.loja.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoSpringDataRepository extends JpaRepository<ProdutoJpaEntity, Long> {

    Optional<ProdutoJpaEntity> findBySku(String sku);

    List<ProdutoJpaEntity> findAllByOrderByIdAsc();
}
