package com.example.loja.application.port.in;

import com.example.loja.domain.pedidos.Pedido;

public interface BuscarPedidoUseCase {

    Pedido buscarPorId(long id);
}
