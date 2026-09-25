package com.example.loja.application.service;

import com.example.loja.application.port.in.CancelarPedidoUseCase;
import com.example.loja.application.port.in.CriarPedidoUseCase;
import com.example.loja.application.port.in.BuscarPedidoUseCase;
import com.example.loja.application.port.in.ListarPedidosUseCase;
import com.example.loja.application.port.out.FreteGateway;
import com.example.loja.application.port.out.FreteInfo;
import com.example.loja.application.port.out.PedidoRepositoryPort;
import com.example.loja.application.port.out.ProdutoRepositoryPort;
import com.example.loja.domain.catalogo.Produto;
import com.example.loja.domain.pedidos.EstoqueInsuficienteException;
import com.example.loja.domain.pedidos.ItemPedido;
import com.example.loja.domain.pedidos.Pedido;
import com.example.loja.domain.pedidos.PedidoNaoEncontradoException;
import com.example.loja.domain.pedidos.ProdutoInativoException;
import com.example.loja.domain.pedidos.ProdutoInexistenteException;

import java.util.ArrayList;
import java.util.List;

public class PedidoService implements CriarPedidoUseCase, BuscarPedidoUseCase,
        ListarPedidosUseCase, CancelarPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepositoryPort;
    private final ProdutoRepositoryPort produtoRepositoryPort;
    private final FreteGateway freteGateway;

    public PedidoService(PedidoRepositoryPort pedidoRepositoryPort,
                          ProdutoRepositoryPort produtoRepositoryPort,
                          FreteGateway freteGateway) {
        this.pedidoRepositoryPort = pedidoRepositoryPort;
        this.produtoRepositoryPort = produtoRepositoryPort;
        this.freteGateway = freteGateway;
    }

    @Override
    public Pedido criar(NovoPedidoCommand command) {
        List<Produto> produtos = new ArrayList<>();
        List<ItemPedido> itens = new ArrayList<>();

        for (ItemCommand itemCommand : command.itens()) {
            Produto produto = produtoRepositoryPort.buscarPorId(itemCommand.produtoId())
                    .orElseThrow(() -> new ProdutoInexistenteException(itemCommand.produtoId()));
            if (!produto.isAtivo()) {
                throw new ProdutoInativoException(produto.getId());
            }
            if (produto.getEstoque() < itemCommand.quantidade()) {
                throw new EstoqueInsuficienteException(produto.getId());
            }
            produtos.add(produto);
            itens.add(new ItemPedido(produto.getId(), itemCommand.quantidade(), produto.getPreco()));
        }

        FreteInfo frete = freteGateway.consultar(command.cep());

        for (int i = 0; i < produtos.size(); i++) {
            Produto produto = produtos.get(i);
            produto.decrementarEstoque(command.itens().get(i).quantidade());
            produtoRepositoryPort.salvar(produto);
        }

        Pedido pedido = Pedido.criar(command.cep(), itens, frete.valor(), frete.prazoDias());
        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    public Pedido buscarPorId(long id) {
        return pedidoRepositoryPort.buscarPorId(id).orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    @Override
    public List<Pedido> listarTodos() {
        return pedidoRepositoryPort.listarTodos();
    }

    @Override
    public Pedido cancelar(long id) {
        Pedido pedido = buscarPorId(id);
        pedido.cancelar();
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = produtoRepositoryPort.buscarPorId(item.getProdutoId())
                    .orElseThrow(() -> new ProdutoInexistenteException(item.getProdutoId()));
            produto.incrementarEstoque(item.getQuantidade());
            produtoRepositoryPort.salvar(produto);
        }
        return pedidoRepositoryPort.salvar(pedido);
    }
}
