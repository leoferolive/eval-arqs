package com.example.loja.usecase.pedido;

import java.util.List;

import com.example.loja.usecase.gateway.PedidoGateway;

public class ListarPedidosInteractor {

    private final PedidoGateway pedidoGateway;

    public ListarPedidosInteractor(PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public List<PedidoOutput> executar() {
        return pedidoGateway.listarTodos().stream().map(PedidoOutput::de).toList();
    }
}
