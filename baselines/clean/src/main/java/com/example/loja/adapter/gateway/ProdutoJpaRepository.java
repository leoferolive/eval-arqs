package com.example.loja.adapter.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoJpaEntity, Long> {

    Optional<ProdutoJpaEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProdutoJpaEntity> findAllByOrderByIdAsc();
}
