package com.example.loja.adapter.presenter;

import org.springframework.stereotype.Component;

import com.example.loja.adapter.controller.dto.PedidoResponse;
import com.example.loja.usecase.pedido.ItemPedidoOutput;
import com.example.loja.usecase.pedido.PedidoOutput;

@Component
public class PedidoPresenter {

    public PedidoResponse apresentar(PedidoOutput output) {
        var itens = output.itens().stream().map(this::apresentarItem).toList();
        return new PedidoResponse(output.id(), output.cep(), output.status().name(), itens, output.valorItens(),
                output.valorFrete(), output.prazoEntregaDias(), output.valorTotal());
    }

    private PedidoResponse.ItemPedidoResponse apresentarItem(ItemPedidoOutput item) {
        return new PedidoResponse.ItemPedidoResponse(item.produtoId(), item.quantidade(), item.precoUnitario(),
                item.subtotal());
    }
}
