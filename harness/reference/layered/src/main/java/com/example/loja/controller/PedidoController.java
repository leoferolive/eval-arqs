package com.example.loja.controller;

import com.example.loja.dto.Dtos.NovoPedido;
import com.example.loja.dto.Dtos.PedidoResponse;
import com.example.loja.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService service;

    public PedidoController(PedidoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody NovoPedido req) { return PedidoResponse.of(service.criar(req)); }

    @GetMapping
    public List<PedidoResponse> listar() { return service.listar().stream().map(PedidoResponse::of).toList(); }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) { return PedidoResponse.of(service.buscar(id)); }

    @PostMapping("/{id}/cancelamento")
    public PedidoResponse cancelar(@PathVariable Long id) { return PedidoResponse.of(service.cancelar(id)); }
}
