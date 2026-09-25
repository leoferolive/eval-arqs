package com.example.loja.usecase.pedido;

import com.example.loja.entity.ItemPedido;
import com.example.loja.entity.Pedido;
import com.example.loja.entity.Produto;
import com.example.loja.entity.exception.PedidoNaoEncontradoException;
import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.usecase.gateway.PedidoGateway;
import com.example.loja.usecase.gateway.ProdutoGateway;

public class CancelarPedidoInteractor {

    private final PedidoGateway pedidoGateway;
    private final ProdutoGateway produtoGateway;

    public CancelarPedidoInteractor(PedidoGateway pedidoGateway, ProdutoGateway produtoGateway) {
        this.pedidoGateway = pedidoGateway;
        this.produtoGateway = produtoGateway;
    }

    public PedidoOutput executar(Long id) {
        Pedido pedido = pedidoGateway.buscarPorId(id).orElseThrow(() -> new PedidoNaoEncontradoException(id));
        pedido.cancelar();

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = produtoGateway.buscarPorId(item.getProdutoId())
                    .orElseThrow(() -> new ProdutoNaoEncontradoException(item.getProdutoId()));
            produto.incrementarEstoque(item.getQuantidade());
            produtoGateway.salvar(produto);
        }

        Pedido salvo = pedidoGateway.salvar(pedido);
        return PedidoOutput.de(salvo);
    }
}
