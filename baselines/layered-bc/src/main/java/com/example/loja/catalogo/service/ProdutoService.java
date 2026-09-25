package com.example.loja.catalogo.service;

import com.example.loja.catalogo.dto.ProdutoConsultaDTO;
import com.example.loja.catalogo.dto.ProdutoRequest;
import com.example.loja.catalogo.dto.ProdutoResponse;
import com.example.loja.catalogo.dto.ProdutoUpdateRequest;
import com.example.loja.catalogo.exception.ProdutoNaoEncontradoException;
import com.example.loja.catalogo.exception.SkuDuplicadoException;
import com.example.loja.catalogo.model.Produto;
import com.example.loja.catalogo.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        produtoRepository.findBySku(request.sku()).ifPresent(p -> {
            throw new SkuDuplicadoException(request.sku());
        });
        Produto produto = new Produto(request.sku(), request.nome(), request.preco(), request.estoque());
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAllByOrderByIdAsc().stream()
                .map(ProdutoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return ProdutoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoUpdateRequest request) {
        Produto produto = buscarEntidade(id);
        produto.atualizar(request.nome(), request.preco(), request.estoque(), request.ativo());
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public void deletar(Long id) {
        Produto produto = buscarEntidade(id);
        produtoRepository.delete(produto);
    }

    @Transactional(readOnly = true)
    public Optional<ProdutoConsultaDTO> consultar(Long id) {
        return produtoRepository.findById(id)
                .map(p -> new ProdutoConsultaDTO(p.getId(), p.getPreco(), p.getEstoque(), p.isAtivo()));
    }

    @Transactional
    public void debitarEstoque(Long id, int quantidade) {
        Produto produto = buscarEntidade(id);
        produto.debitarEstoque(quantidade);
    }

    @Transactional
    public void creditarEstoque(Long id, int quantidade) {
        Produto produto = buscarEntidade(id);
        produto.creditarEstoque(quantidade);
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }
}
