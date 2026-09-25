package com.example.loja.service;

import com.example.loja.dto.Dtos.AtualizaProduto;
import com.example.loja.dto.Dtos.NovoProduto;
import com.example.loja.exception.NegocioException;
import com.example.loja.model.Produto;
import com.example.loja.repository.ProdutoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) { this.repository = repository; }

    @Transactional
    public Produto criar(NovoProduto req) {
        if (repository.existsBySku(req.sku())) {
            throw new NegocioException(HttpStatus.CONFLICT, "SKU_DUPLICADO", "SKU já cadastrado");
        }
        return repository.save(new Produto(req.sku(), req.nome(), req.preco(), req.estoque()));
    }

    public List<Produto> listar() { return repository.findAll(Sort.by("id")); }

    public Produto buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new NegocioException(HttpStatus.NOT_FOUND, "PRODUTO_NAO_ENCONTRADO", "Produto não encontrado"));
    }

    @Transactional
    public Produto atualizar(Long id, AtualizaProduto req) {
        Produto p = buscar(id);
        p.atualizar(req.nome(), req.preco(), req.estoque(), req.ativo());
        return p;
    }

    @Transactional
    public void remover(Long id) { repository.delete(buscar(id)); }
}
