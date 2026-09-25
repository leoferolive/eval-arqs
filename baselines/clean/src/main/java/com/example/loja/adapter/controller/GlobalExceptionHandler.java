package com.example.loja.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.loja.adapter.controller.dto.ErroResponse;
import com.example.loja.entity.exception.EstoqueInsuficienteException;
import com.example.loja.entity.exception.FreteIndisponivelException;
import com.example.loja.entity.exception.PedidoJaCanceladoException;
import com.example.loja.entity.exception.PedidoNaoEncontradoException;
import com.example.loja.entity.exception.ProdutoInativoException;
import com.example.loja.entity.exception.ProdutoInexistenteException;
import com.example.loja.entity.exception.ProdutoNaoEncontradoException;
import com.example.loja.entity.exception.SkuDuplicadoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarValidacao(Exception ex) {
        return new ErroResponse("VALIDACAO", "Corpo da requisição inválido");
    }

    @ExceptionHandler({ ProdutoNaoEncontradoException.class, PedidoNaoEncontradoException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse tratarNaoEncontrado(Exception ex) {
        return corpo(ex);
    }

    @ExceptionHandler({ SkuDuplicadoException.class, PedidoJaCanceladoException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse tratarConflito(Exception ex) {
        return corpo(ex);
    }

    @ExceptionHandler({ ProdutoInexistenteException.class, ProdutoInativoException.class,
            EstoqueInsuficienteException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroResponse tratarRegraNegocio(Exception ex) {
        return corpo(ex);
    }

    @ExceptionHandler(FreteIndisponivelException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErroResponse tratarFreteIndisponivel(Exception ex) {
        return corpo(ex);
    }

    private ErroResponse corpo(Exception ex) {
        var domainException = (com.example.loja.entity.exception.DomainException) ex;
        return new ErroResponse(domainException.getCodigo(), domainException.getMessage());
    }
}
