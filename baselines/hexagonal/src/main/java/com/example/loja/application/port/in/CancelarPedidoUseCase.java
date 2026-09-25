package com.example.loja.application.port.in;

import com.example.loja.domain.pedidos.Pedido;

public interface CancelarPedidoUseCase {

    Pedido cancelar(long id);
}
