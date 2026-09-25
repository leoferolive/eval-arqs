package com.example.loja.adapter.in.web;

import com.example.loja.adapter.in.web.dto.ProdutoRequest;
import com.example.loja.adapter.in.web.dto.ProdutoResponse;
import com.example.loja.adapter.in.web.dto.ProdutoUpdateRequest;
import com.example.loja.application.port.in.AtualizarProdutoUseCase;
import com.example.loja.application.port.in.BuscarProdutoUseCase;
import com.example.loja.application.port.in.CriarProdutoUseCase;
import com.example.loja.application.port.in.ExcluirProdutoUseCase;
import com.example.loja.application.port.in.ListarProdutosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final CriarProdutoUseCase criarProdutoUseCase;
    private final AtualizarProdutoUseCase atualizarProdutoUseCase;
    private final ExcluirProdutoUseCase excluirProdutoUseCase;
    private final BuscarProdutoUseCase buscarProdutoUseCase;
    private final ListarProdutosUseCase listarProdutosUseCase;

    public ProdutoController(CriarProdutoUseCase criarProdutoUseCase,
                              AtualizarProdutoUseCase atualizarProdutoUseCase,
                              ExcluirProdutoUseCase excluirProdutoUseCase,
                              BuscarProdutoUseCase buscarProdutoUseCase,
                              ListarProdutosUseCase listarProdutosUseCase) {
        this.criarProdutoUseCase = criarProdutoUseCase;
        this.atualizarProdutoUseCase = atualizarProdutoUseCase;
        this.excluirProdutoUseCase = excluirProdutoUseCase;
        this.buscarProdutoUseCase = buscarProdutoUseCase;
        this.listarProdutosUseCase = listarProdutosUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) {
        var produto = criarProdutoUseCase.criar(new CriarProdutoUseCase.NovoProdutoCommand(
                request.sku(), request.nome(), request.preco(), request.estoque()));
        return ProdutoResponse.from(produto);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return listarProdutosUseCase.listarTodos().stream().map(ProdutoResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ProdutoResponse buscar(@PathVariable long id) {
        return ProdutoResponse.from(buscarProdutoUseCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Transactional
    public ProdutoResponse atualizar(@PathVariable long id, @Valid @RequestBody ProdutoUpdateRequest request) {
        var produto = atualizarProdutoUseCase.atualizar(id, new AtualizarProdutoUseCase.AtualizarProdutoCommand(
                request.nome(), request.preco(), request.estoque(), request.ativo()));
        return ProdutoResponse.from(produto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void excluir(@PathVariable long id) {
        excluirProdutoUseCase.excluir(id);
    }
}
