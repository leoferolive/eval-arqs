package com.example.loja.adapter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loja.adapter.controller.dto.ProdutoAtualizacaoRequest;
import com.example.loja.adapter.controller.dto.ProdutoRequest;
import com.example.loja.adapter.controller.dto.ProdutoResponse;
import com.example.loja.adapter.presenter.ProdutoPresenter;
import com.example.loja.usecase.produto.AtualizarProdutoInput;
import com.example.loja.usecase.produto.AtualizarProdutoInteractor;
import com.example.loja.usecase.produto.BuscarProdutoInteractor;
import com.example.loja.usecase.produto.CriarProdutoInput;
import com.example.loja.usecase.produto.CriarProdutoInteractor;
import com.example.loja.usecase.produto.ListarProdutosInteractor;
import com.example.loja.usecase.produto.RemoverProdutoInteractor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final CriarProdutoInteractor criarProdutoInteractor;
    private final ListarProdutosInteractor listarProdutosInteractor;
    private final BuscarProdutoInteractor buscarProdutoInteractor;
    private final AtualizarProdutoInteractor atualizarProdutoInteractor;
    private final RemoverProdutoInteractor removerProdutoInteractor;
    private final ProdutoPresenter presenter;

    public ProdutoController(CriarProdutoInteractor criarProdutoInteractor,
            ListarProdutosInteractor listarProdutosInteractor, BuscarProdutoInteractor buscarProdutoInteractor,
            AtualizarProdutoInteractor atualizarProdutoInteractor, RemoverProdutoInteractor removerProdutoInteractor,
            ProdutoPresenter presenter) {
        this.criarProdutoInteractor = criarProdutoInteractor;
        this.listarProdutosInteractor = listarProdutosInteractor;
        this.buscarProdutoInteractor = buscarProdutoInteractor;
        this.atualizarProdutoInteractor = atualizarProdutoInteractor;
        this.removerProdutoInteractor = removerProdutoInteractor;
        this.presenter = presenter;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        var output = criarProdutoInteractor
                .executar(new CriarProdutoInput(request.sku(), request.nome(), request.preco(), request.estoque()));
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.apresentar(output));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return listarProdutosInteractor.executar().stream().map(presenter::apresentar).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ProdutoResponse buscar(@PathVariable Long id) {
        return presenter.apresentar(buscarProdutoInteractor.executar(id));
    }

    @PutMapping("/{id}")
    @Transactional
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoAtualizacaoRequest request) {
        var output = atualizarProdutoInteractor.executar(new AtualizarProdutoInput(id, request.nome(),
                request.preco(), request.estoque(), request.ativo()));
        return presenter.apresentar(output);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        removerProdutoInteractor.executar(id);
        return ResponseEntity.noContent().build();
    }
}
