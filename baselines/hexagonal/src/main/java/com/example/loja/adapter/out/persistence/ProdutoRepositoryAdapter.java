package com.example.loja.adapter.out.persistence;

import com.example.loja.application.port.out.ProdutoRepositoryPort;
import com.example.loja.domain.catalogo.Produto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProdutoRepositoryAdapter implements ProdutoRepositoryPort {

    private final ProdutoSpringDataRepository springDataRepository;

    public ProdutoRepositoryAdapter(ProdutoSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Produto salvar(Produto produto) {
        ProdutoJpaEntity entity = new ProdutoJpaEntity(produto.getId(), produto.getSku(), produto.getNome(),
                produto.getPreco(), produto.getEstoque(), produto.isAtivo());
        ProdutoJpaEntity salvo = springDataRepository.save(entity);
        return toDomain(salvo);
    }

    @Override
    public Optional<Produto> buscarPorId(long id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Produto> buscarPorSku(String sku) {
        return springDataRepository.findBySku(sku).map(this::toDomain);
    }

    @Override
    public List<Produto> listarTodos() {
        return springDataRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existePorId(long id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public void excluir(long id) {
        springDataRepository.deleteById(id);
    }

    private Produto toDomain(ProdutoJpaEntity entity) {
        return new Produto(entity.getId(), entity.getSku(), entity.getNome(), entity.getPreco(),
                entity.getEstoque(), entity.isAtivo());
    }
}
