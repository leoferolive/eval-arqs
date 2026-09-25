package com.example.loja.adapter.presenter;

import org.springframework.stereotype.Component;

import com.example.loja.adapter.controller.dto.ProdutoResponse;
import com.example.loja.usecase.produto.ProdutoOutput;

@Component
public class ProdutoPresenter {

    public ProdutoResponse apresentar(ProdutoOutput output) {
        return new ProdutoResponse(output.id(), output.sku(), output.nome(), output.preco(), output.estoque(),
                output.ativo());
    }
}
