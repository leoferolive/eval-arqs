package com.example.loja.adapter.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.loja.entity.Produto;
import com.example.loja.usecase.gateway.ProdutoGateway;

@Component
public class ProdutoGatewayImpl implements ProdutoGateway {

    private final ProdutoJpaRepository repository;

    public ProdutoGatewayImpl(ProdutoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Produto salvar(Produto produto) {
        ProdutoJpaEntity salvo = repository.save(ProdutoJpaEntity.de(produto));
        return salvo.paraDominio();
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id).map(ProdutoJpaEntity::paraDominio);
    }

    @Override
    public List<Produto> listarTodos() {
        return repository.findAllByOrderByIdAsc().stream().map(ProdutoJpaEntity::paraDominio).toList();
    }

    @Override
    public boolean existePorSku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void remover(Long id) {
        repository.deleteById(id);
    }
}
