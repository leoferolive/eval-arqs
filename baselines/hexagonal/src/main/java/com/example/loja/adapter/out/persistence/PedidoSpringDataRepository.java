package com.example.loja.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoSpringDataRepository extends JpaRepository<PedidoJpaEntity, Long> {

    List<PedidoJpaEntity> findAllByOrderByIdAsc();
}
