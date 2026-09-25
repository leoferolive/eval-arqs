package com.example.loja.application.port.in;

import com.example.loja.domain.pedidos.Pedido;

import java.util.List;

public interface CriarPedidoUseCase {

    Pedido criar(NovoPedidoCommand command);

    record NovoPedidoCommand(String cep, List<ItemCommand> itens) {
    }

    record ItemCommand(Long produtoId, int quantidade) {
    }
}
