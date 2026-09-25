package com.example.loja.usecase.pedido;

import java.util.List;

public record CriarPedidoInput(String cep, List<ItemPedidoInput> itens) {

    public record ItemPedidoInput(Long produtoId, int quantidade) {
    }
}
