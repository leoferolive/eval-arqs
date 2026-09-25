package com.example.loja.adapter.in.web;

import com.example.loja.adapter.in.web.dto.ItemPedidoRequest;
import com.example.loja.adapter.in.web.dto.PedidoRequest;
import com.example.loja.adapter.in.web.dto.PedidoResponse;
import com.example.loja.application.port.in.BuscarPedidoUseCase;
import com.example.loja.application.port.in.CancelarPedidoUseCase;
import com.example.loja.application.port.in.CriarPedidoUseCase;
import com.example.loja.application.port.in.ListarPedidosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedidoUseCase;
    private final BuscarPedidoUseCase buscarPedidoUseCase;
    private final ListarPedidosUseCase listarPedidosUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;

    public PedidoController(CriarPedidoUseCase criarPedidoUseCase,
                             BuscarPedidoUseCase buscarPedidoUseCase,
                             ListarPedidosUseCase listarPedidosUseCase,
                             CancelarPedidoUseCase cancelarPedidoUseCase) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.buscarPedidoUseCase = buscarPedidoUseCase;
        this.listarPedidosUseCase = listarPedidosUseCase;
        this.cancelarPedidoUseCase = cancelarPedidoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest request) {
        List<CriarPedidoUseCase.ItemCommand> itens = request.itens().stream()
                .map(this::toItemCommand)
                .toList();
        var pedido = criarPedidoUseCase.criar(new CriarPedidoUseCase.NovoPedidoCommand(request.cep(), itens));
        return PedidoResponse.from(pedido);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return listarPedidosUseCase.listarTodos().stream().map(PedidoResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PedidoResponse buscar(@PathVariable long id) {
        return PedidoResponse.from(buscarPedidoUseCase.buscarPorId(id));
    }

    @PostMapping("/{id}/cancelamento")
    @Transactional
    public PedidoResponse cancelar(@PathVariable long id) {
        return PedidoResponse.from(cancelarPedidoUseCase.cancelar(id));
    }

    private CriarPedidoUseCase.ItemCommand toItemCommand(ItemPedidoRequest request) {
        return new CriarPedidoUseCase.ItemCommand(request.produtoId(), request.quantidade());
    }
}
