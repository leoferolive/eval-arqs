package com.example.loja.service;

import com.example.loja.client.FreteClient;
import com.example.loja.dto.Dtos.Frete;
import com.example.loja.dto.Dtos.NovoItem;
import com.example.loja.dto.Dtos.NovoPedido;
import com.example.loja.exception.NegocioException;
import com.example.loja.model.ItemPedido;
import com.example.loja.model.Pedido;
import com.example.loja.model.Produto;
import com.example.loja.model.StatusPedido;
import com.example.loja.repository.PedidoRepository;
import com.example.loja.repository.ProdutoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;
    private final FreteClient frete;

    public PedidoService(PedidoRepository pedidos, ProdutoRepository produtos, FreteClient frete) {
        this.pedidos = pedidos; this.produtos = produtos; this.frete = frete;
    }

    @Transactional
    public Pedido criar(NovoPedido req) {
        List<Produto> encontrados = new ArrayList<>();
        List<ItemPedido> itens = new ArrayList<>();
        for (NovoItem item : req.itens()) {
            Produto p = produtos.findById(item.produtoId()).orElseThrow(
                    () -> new NegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "PRODUTO_INEXISTENTE", "Produto inexistente"));
            if (!p.isAtivo()) throw new NegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "PRODUTO_INATIVO", "Produto inativo");
            if (p.getEstoque() < item.quantidade()) {
                throw new NegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "ESTOQUE_INSUFICIENTE", "Estoque insuficiente");
            }
            encontrados.add(p);
            itens.add(new ItemPedido(p.getId(), item.quantidade(), p.getPreco()));
        }
        Frete cotacao = frete.cotar(req.cep());
        for (int i = 0; i < itens.size(); i++) encontrados.get(i).ajustarEstoque(-itens.get(i).getQuantidade());
        return pedidos.save(new Pedido(req.cep(), itens, cotacao.valor(), cotacao.prazoDias()));
    }

    public List<Pedido> listar() { return pedidos.findAll(Sort.by("id")); }

    public Pedido buscar(Long id) {
        return pedidos.findById(id).orElseThrow(
                () -> new NegocioException(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO", "Pedido não encontrado"));
    }

    @Transactional
    public Pedido cancelar(Long id) {
        Pedido pedido = buscar(id);
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new NegocioException(HttpStatus.CONFLICT, "PEDIDO_JA_CANCELADO", "Pedido já cancelado");
        }
        pedido.cancelar();
        for (ItemPedido item : pedido.getItens()) {
            produtos.findById(item.getProdutoId()).ifPresent(p -> p.ajustarEstoque(item.getQuantidade()));
        }
        return pedido;
    }
}
