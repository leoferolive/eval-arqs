package com.example.loja.adapter.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.loja.entity.Pedido;
import com.example.loja.usecase.gateway.PedidoGateway;

@Component
public class PedidoGatewayImpl implements PedidoGateway {

    private final PedidoJpaRepository repository;

    public PedidoGatewayImpl(PedidoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity salvo = repository.save(PedidoJpaEntity.de(pedido));
        return salvo.paraDominio();
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return repository.buscarComItensPorId(id).map(PedidoJpaEntity::paraDominio);
    }

    @Override
    public List<Pedido> listarTodos() {
        return repository.buscarTodosComItens().stream().map(PedidoJpaEntity::paraDominio).toList();
    }
}
