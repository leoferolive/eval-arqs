package com.example.loja.usecase.pedido;

import java.util.ArrayList;
import java.util.List;

import com.example.loja.entity.ItemPedido;
import com.example.loja.entity.Pedido;
import com.example.loja.entity.Produto;
import com.example.loja.entity.exception.ProdutoInativoException;
import com.example.loja.entity.exception.ProdutoInexistenteException;
import com.example.loja.usecase.gateway.FreteGateway;
import com.example.loja.usecase.gateway.FreteInfo;
import com.example.loja.usecase.gateway.PedidoGateway;
import com.example.loja.usecase.gateway.ProdutoGateway;

public class CriarPedidoInteractor {

    private final ProdutoGateway produtoGateway;
    private final PedidoGateway pedidoGateway;
    private final FreteGateway freteGateway;

    public CriarPedidoInteractor(ProdutoGateway produtoGateway, PedidoGateway pedidoGateway,
            FreteGateway freteGateway) {
        this.produtoGateway = produtoGateway;
        this.pedidoGateway = pedidoGateway;
        this.freteGateway = freteGateway;
    }

    public PedidoOutput executar(CriarPedidoInput input) {
        List<Produto> produtos = new ArrayList<>();
        List<ItemPedido> itens = new ArrayList<>();

        for (CriarPedidoInput.ItemPedidoInput itemInput : input.itens()) {
            Produto produto = produtoGateway.buscarPorId(itemInput.produtoId())
                    .orElseThrow(() -> new ProdutoInexistenteException(itemInput.produtoId()));
            if (!produto.isAtivo()) {
                throw new ProdutoInativoException(produto.getId());
            }
            produto.decrementarEstoque(itemInput.quantidade());
            produtos.add(produto);
            itens.add(new ItemPedido(produto.getId(), itemInput.quantidade(), produto.getPreco()));
        }

        FreteInfo frete = freteGateway.consultar(input.cep());

        Pedido pedido = new Pedido(input.cep(), itens, frete.valor(), frete.prazoDias());
        Pedido salvo = pedidoGateway.salvar(pedido);
        produtos.forEach(produtoGateway::salvar);

        return PedidoOutput.de(salvo);
    }
}
