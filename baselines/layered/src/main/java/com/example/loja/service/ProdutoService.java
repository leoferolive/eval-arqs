package com.example.loja.service;

import com.example.loja.dto.ProdutoRequest;
import com.example.loja.dto.ProdutoResponse;
import com.example.loja.dto.ProdutoUpdateRequest;
import com.example.loja.exception.ProdutoNaoEncontradoException;
import com.example.loja.exception.SkuDuplicadoException;
import com.example.loja.model.Produto;
import com.example.loja.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        if (produtoRepository.existsBySku(request.sku())) {
            throw new SkuDuplicadoException(request.sku());
        }
        Produto produto = new Produto(request.sku(), request.nome(), escala(request.preco()), request.estoque());
        produto = produtoRepository.save(produto);
        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAllByOrderByIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoUpdateRequest request) {
        Produto produto = buscarEntidade(id);
        produto.setNome(request.nome());
        produto.setPreco(escala(request.preco()));
        produto.setEstoque(request.estoque());
        produto.setAtivo(request.ativo());
        return toResponse(produto);
    }

    @Transactional
    public void deletar(Long id) {
        Produto produto = buscarEntidade(id);
        produtoRepository.delete(produto);
    }

    Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }

    private ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.isAtivo()
        );
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
