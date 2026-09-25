package com.example.loja.usecase.gateway;

import java.util.List;
import java.util.Optional;

import com.example.loja.entity.Pedido;

public interface PedidoGateway {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(Long id);

    List<Pedido> listarTodos();
}
