package com.example.loja.service;

import com.example.loja.client.FreteClient;
import com.example.loja.client.FreteResponse;
import com.example.loja.dto.ItemPedidoRequest;
import com.example.loja.dto.ItemPedidoResponse;
import com.example.loja.dto.PedidoRequest;
import com.example.loja.dto.PedidoResponse;
import com.example.loja.exception.EstoqueInsuficienteException;
import com.example.loja.exception.PedidoJaCanceladoException;
import com.example.loja.exception.PedidoNaoEncontradoException;
import com.example.loja.exception.ProdutoInativoException;
import com.example.loja.exception.ProdutoInexistenteException;
import com.example.loja.model.ItemPedido;
import com.example.loja.model.Pedido;
import com.example.loja.model.Produto;
import com.example.loja.model.StatusPedido;
import com.example.loja.repository.PedidoRepository;
import com.example.loja.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final FreteClient freteClient;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository,
                          FreteClient freteClient) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.freteClient = freteClient;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        List<Produto> produtos = new ArrayList<>();
        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal valorItens = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (ItemPedidoRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new ProdutoInexistenteException(itemRequest.produtoId()));
            if (!produto.isAtivo()) {
                throw new ProdutoInativoException(produto.getId());
            }
            if (produto.getEstoque() < itemRequest.quantidade()) {
                throw new EstoqueInsuficienteException(produto.getId());
            }
            BigDecimal precoUnitario = produto.getPreco();
            BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(itemRequest.quantidade()))
                    .setScale(2, RoundingMode.HALF_UP);
            valorItens = valorItens.add(subtotal);

            produtos.add(produto);
            itens.add(new ItemPedido(produto.getId(), itemRequest.quantidade(), precoUnitario, subtotal));
        }

        FreteResponse frete = freteClient.consultarFrete(request.cep());
        BigDecimal valorFrete = frete.valor().setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorTotal = valorItens.add(valorFrete).setScale(2, RoundingMode.HALF_UP);

        for (int i = 0; i < produtos.size(); i++) {
            produtos.get(i).decrementarEstoque(itens.get(i).getQuantidade());
        }

        Pedido pedido = new Pedido(request.cep(), valorItens, valorFrete, frete.prazoDias(), valorTotal);
        itens.forEach(pedido::adicionarItem);
        pedido = pedidoRepository.save(pedido);

        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAllComItensOrderByIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponse cancelar(Long id) {
        Pedido pedido = buscarEntidade(id);
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new PedidoJaCanceladoException(id);
        }
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new ProdutoInexistenteException(item.getProdutoId()));
            produto.incrementarEstoque(item.getQuantidade());
        }
        pedido.cancelar();
        return toResponse(pedido);
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findByIdComItens(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(item -> new ItemPedidoResponse(item.getProdutoId(), item.getQuantidade(),
                        item.getPrecoUnitario(), item.getSubtotal()))
                .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCep(),
                pedido.getStatus(),
                itens,
                pedido.getValorItens(),
                pedido.getValorFrete(),
                pedido.getPrazoEntregaDias(),
                pedido.getValorTotal()
        );
    }
}
