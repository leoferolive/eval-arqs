package com.example.loja.adapter.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, Long> {

    @Query("select distinct p from PedidoJpaEntity p left join fetch p.itens where p.id = :id")
    Optional<PedidoJpaEntity> buscarComItensPorId(@Param("id") Long id);

    @Query("select distinct p from PedidoJpaEntity p left join fetch p.itens order by p.id asc")
    List<PedidoJpaEntity> buscarTodosComItens();
}
