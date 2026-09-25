package com.example.loja.application.port.in;

import com.example.loja.domain.pedidos.Pedido;

import java.util.List;

public interface ListarPedidosUseCase {

    List<Pedido> listarTodos();
}
