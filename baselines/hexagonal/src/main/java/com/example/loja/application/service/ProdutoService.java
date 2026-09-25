package com.example.loja.application.service;

import com.example.loja.application.port.in.AtualizarProdutoUseCase;
import com.example.loja.application.port.in.BuscarProdutoUseCase;
import com.example.loja.application.port.in.CriarProdutoUseCase;
import com.example.loja.application.port.in.ExcluirProdutoUseCase;
import com.example.loja.application.port.in.ListarProdutosUseCase;
import com.example.loja.application.port.out.ProdutoRepositoryPort;
import com.example.loja.domain.catalogo.Produto;
import com.example.loja.domain.catalogo.ProdutoNaoEncontradoException;
import com.example.loja.domain.catalogo.SkuDuplicadoException;

import java.util.List;

public class ProdutoService implements CriarProdutoUseCase, AtualizarProdutoUseCase,
        ExcluirProdutoUseCase, BuscarProdutoUseCase, ListarProdutosUseCase {

    private final ProdutoRepositoryPort produtoRepositoryPort;

    public ProdutoService(ProdutoRepositoryPort produtoRepositoryPort) {
        this.produtoRepositoryPort = produtoRepositoryPort;
    }

    @Override
    public Produto criar(NovoProdutoCommand command) {
        if (produtoRepositoryPort.buscarPorSku(command.sku()).isPresent()) {
            throw new SkuDuplicadoException(command.sku());
        }
        Produto produto = Produto.novo(command.sku(), command.nome(), command.preco(), command.estoque());
        return produtoRepositoryPort.salvar(produto);
    }

    @Override
    public Produto atualizar(long id, AtualizarProdutoCommand command) {
        Produto produto = buscarPorId(id);
        produto.atualizar(command.nome(), command.preco(), command.estoque(), command.ativo());
        return produtoRepositoryPort.salvar(produto);
    }

    @Override
    public void excluir(long id) {
        if (!produtoRepositoryPort.existePorId(id)) {
            throw new ProdutoNaoEncontradoException(id);
        }
        produtoRepositoryPort.excluir(id);
    }

    @Override
    public Produto buscarPorId(long id) {
        return produtoRepositoryPort.buscarPorId(id).orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }

    @Override
    public List<Produto> listarTodos() {
        return produtoRepositoryPort.listarTodos();
    }
}
