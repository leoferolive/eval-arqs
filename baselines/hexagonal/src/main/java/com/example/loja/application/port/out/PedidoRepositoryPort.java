package com.example.loja.application.port.out;

import com.example.loja.domain.pedidos.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(long id);

    List<Pedido> listarTodos();
}
