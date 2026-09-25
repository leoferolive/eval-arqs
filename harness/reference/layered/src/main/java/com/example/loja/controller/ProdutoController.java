package com.example.loja.controller;

import com.example.loja.dto.Dtos.AtualizaProduto;
import com.example.loja.dto.Dtos.NovoProduto;
import com.example.loja.dto.Dtos.ProdutoResponse;
import com.example.loja.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoService service;

    public ProdutoController(ProdutoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody NovoProduto req) { return ProdutoResponse.of(service.criar(req)); }

    @GetMapping
    public List<ProdutoResponse> listar() { return service.listar().stream().map(ProdutoResponse::of).toList(); }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) { return ProdutoResponse.of(service.buscar(id)); }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizaProduto req) {
        return ProdutoResponse.of(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) { service.remover(id); }
}
