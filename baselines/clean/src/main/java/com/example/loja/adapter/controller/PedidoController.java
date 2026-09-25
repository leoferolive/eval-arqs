package com.example.loja.adapter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.loja.adapter.controller.dto.PedidoRequest;
import com.example.loja.adapter.controller.dto.PedidoResponse;
import com.example.loja.adapter.presenter.PedidoPresenter;
import com.example.loja.usecase.pedido.BuscarPedidoInteractor;
import com.example.loja.usecase.pedido.CancelarPedidoInteractor;
import com.example.loja.usecase.pedido.CriarPedidoInput;
import com.example.loja.usecase.pedido.CriarPedidoInteractor;
import com.example.loja.usecase.pedido.ListarPedidosInteractor;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final CriarPedidoInteractor criarPedidoInteractor;
    private final ListarPedidosInteractor listarPedidosInteractor;
    private final BuscarPedidoInteractor buscarPedidoInteractor;
    private final CancelarPedidoInteractor cancelarPedidoInteractor;
    private final PedidoPresenter presenter;

    public PedidoController(CriarPedidoInteractor criarPedidoInteractor,
            ListarPedidosInteractor listarPedidosInteractor, BuscarPedidoInteractor buscarPedidoInteractor,
            CancelarPedidoInteractor cancelarPedidoInteractor, PedidoPresenter presenter) {
        this.criarPedidoInteractor = criarPedidoInteractor;
        this.listarPedidosInteractor = listarPedidosInteractor;
        this.buscarPedidoInteractor = buscarPedidoInteractor;
        this.cancelarPedidoInteractor = cancelarPedidoInteractor;
        this.presenter = presenter;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        var itens = request.itens().stream()
                .map(item -> new CriarPedidoInput.ItemPedidoInput(item.produtoId(), item.quantidade()))
                .toList();
        var output = criarPedidoInteractor.executar(new CriarPedidoInput(request.cep(), itens));
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.apresentar(output));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return listarPedidosInteractor.executar().stream().map(presenter::apresentar).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PedidoResponse buscar(@PathVariable Long id) {
        return presenter.apresentar(buscarPedidoInteractor.executar(id));
    }

    @PostMapping("/{id}/cancelamento")
    @Transactional
    public PedidoResponse cancelar(@PathVariable Long id) {
        return presenter.apresentar(cancelarPedidoInteractor.executar(id));
    }
}
