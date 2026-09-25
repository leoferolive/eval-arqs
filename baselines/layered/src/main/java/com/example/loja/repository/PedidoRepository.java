package com.example.loja.repository;

import com.example.loja.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("select distinct p from Pedido p left join fetch p.itens order by p.id asc")
    List<Pedido> findAllComItensOrderByIdAsc();

    @Query("select p from Pedido p left join fetch p.itens where p.id = :id")
    Optional<Pedido> findByIdComItens(Long id);
}
