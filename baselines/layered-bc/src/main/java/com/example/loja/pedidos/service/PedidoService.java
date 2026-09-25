package com.example.loja.pedidos.service;

import com.example.loja.catalogo.dto.ProdutoConsultaDTO;
import com.example.loja.catalogo.service.ProdutoService;
import com.example.loja.pedidos.client.FreteClient;
import com.example.loja.pedidos.client.FreteRespostaDTO;
import com.example.loja.pedidos.dto.ItemRequest;
import com.example.loja.pedidos.dto.PedidoRequest;
import com.example.loja.pedidos.dto.PedidoResponse;
import com.example.loja.pedidos.exception.EstoqueInsuficienteException;
import com.example.loja.pedidos.exception.PedidoJaCanceladoException;
import com.example.loja.pedidos.exception.PedidoNaoEncontradoException;
import com.example.loja.pedidos.exception.ProdutoInativoException;
import com.example.loja.pedidos.exception.ProdutoInexistenteException;
import com.example.loja.pedidos.model.ItemPedido;
import com.example.loja.pedidos.model.Pedido;
import com.example.loja.pedidos.model.PedidoStatus;
import com.example.loja.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;
    private final FreteClient freteClient;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoService produtoService, FreteClient freteClient) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
        this.freteClient = freteClient;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        List<ItemPedido> itens = request.itens().stream()
                .map(this::validarEConstruirItem)
                .toList();

        FreteRespostaDTO frete = freteClient.consultarFrete(request.cep());

        for (ItemRequest itemRequest : request.itens()) {
            produtoService.debitarEstoque(itemRequest.produtoId(), itemRequest.quantidade());
        }

        Pedido pedido = new Pedido(request.cep(), itens, frete.valor(), frete.prazoDias());
        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    private ItemPedido validarEConstruirItem(ItemRequest itemRequest) {
        ProdutoConsultaDTO produto = produtoService.consultar(itemRequest.produtoId())
                .orElseThrow(() -> new ProdutoInexistenteException(itemRequest.produtoId()));
        if (!produto.ativo()) {
            throw new ProdutoInativoException(itemRequest.produtoId());
        }
        if (produto.estoque() < itemRequest.quantidade()) {
            throw new EstoqueInsuficienteException(itemRequest.produtoId());
        }
        return new ItemPedido(itemRequest.produtoId(), itemRequest.quantidade(), produto.preco());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAllByOrderByIdAsc().stream()
                .map(PedidoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return PedidoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponse cancelar(Long id) {
        Pedido pedido = buscarEntidade(id);
        if (pedido.getStatus() == PedidoStatus.CANCELADO) {
            throw new PedidoJaCanceladoException(id);
        }
        pedido.cancelar();
        pedido.getItens().forEach(item -> produtoService.creditarEstoque(item.getProdutoId(), item.getQuantidade()));
        return PedidoResponse.de(pedido);
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }
}
