package com.example.loja.adapter.out.persistence;

import com.example.loja.application.port.out.PedidoRepositoryPort;
import com.example.loja.domain.pedidos.ItemPedido;
import com.example.loja.domain.pedidos.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {

    private final PedidoSpringDataRepository springDataRepository;

    public PedidoRepositoryAdapter(PedidoSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity entity = new PedidoJpaEntity(pedido.getId(), pedido.getCep(), pedido.getStatus(),
                pedido.getValorFrete(), pedido.getPrazoEntregaDias());
        for (ItemPedido item : pedido.getItens()) {
            entity.adicionarItem(new ItemPedidoJpaEntity(item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario()));
        }
        PedidoJpaEntity salvo = springDataRepository.save(entity);
        return toDomain(salvo);
    }

    @Override
    public Optional<Pedido> buscarPorId(long id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Pedido> listarTodos() {
        return springDataRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
    }

    private Pedido toDomain(PedidoJpaEntity entity) {
        List<ItemPedido> itens = entity.getItens().stream()
                .map(item -> new ItemPedido(item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario()))
                .toList();
        return new Pedido(entity.getId(), entity.getCep(), entity.getStatus(), itens,
                entity.getValorFrete(), entity.getPrazoEntregaDias());
    }
}
