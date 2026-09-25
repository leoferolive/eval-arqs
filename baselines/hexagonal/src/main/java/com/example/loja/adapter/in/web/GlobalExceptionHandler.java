package com.example.loja.adapter.in.web;

import com.example.loja.adapter.in.web.dto.ErroResponse;
import com.example.loja.domain.catalogo.ProdutoNaoEncontradoException;
import com.example.loja.domain.catalogo.SkuDuplicadoException;
import com.example.loja.domain.pedidos.EstoqueInsuficienteException;
import com.example.loja.domain.pedidos.FreteIndisponivelException;
import com.example.loja.domain.pedidos.PedidoJaCanceladoException;
import com.example.loja.domain.pedidos.PedidoNaoEncontradoException;
import com.example.loja.domain.pedidos.ProdutoInativoException;
import com.example.loja.domain.pedidos.ProdutoInexistenteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponse("VALIDACAO", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleCorpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse("VALIDACAO", "Corpo da requisição inválido"));
    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleProdutoNaoEncontrado(ProdutoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse("PRODUTO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(PedidoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handlePedidoNaoEncontrado(PedidoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse("PEDIDO_NAO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(SkuDuplicadoException.class)
    public ResponseEntity<ErroResponse> handleSkuDuplicado(SkuDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("SKU_DUPLICADO", ex.getMessage()));
    }

    @ExceptionHandler(PedidoJaCanceladoException.class)
    public ResponseEntity<ErroResponse> handlePedidoJaCancelado(PedidoJaCanceladoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("PEDIDO_JA_CANCELADO", ex.getMessage()));
    }

    @ExceptionHandler(ProdutoInexistenteException.class)
    public ResponseEntity<ErroResponse> handleProdutoInexistente(ProdutoInexistenteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("PRODUTO_INEXISTENTE", ex.getMessage()));
    }

    @ExceptionHandler(ProdutoInativoException.class)
    public ResponseEntity<ErroResponse> handleProdutoInativo(ProdutoInativoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("PRODUTO_INATIVO", ex.getMessage()));
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErroResponse> handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse("ESTOQUE_INSUFICIENTE", ex.getMessage()));
    }

    @ExceptionHandler(FreteIndisponivelException.class)
    public ResponseEntity<ErroResponse> handleFreteIndisponivel(FreteIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErroResponse("FRETE_INDISPONIVEL", ex.getMessage()));
    }
}
