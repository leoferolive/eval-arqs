package com.example.loja.usecase.pedido;

import com.example.loja.entity.exception.PedidoNaoEncontradoException;
import com.example.loja.usecase.gateway.PedidoGateway;

public class BuscarPedidoInteractor {

    private final PedidoGateway pedidoGateway;

    public BuscarPedidoInteractor(PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public PedidoOutput executar(Long id) {
        return pedidoGateway.buscarPorId(id)
                .map(PedidoOutput::de)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }
}
