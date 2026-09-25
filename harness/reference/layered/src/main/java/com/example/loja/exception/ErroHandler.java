package com.example.loja.exception;

import com.example.loja.dto.Dtos.Erro;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErroHandler {

    @ExceptionHandler(NegocioException.class)
    ResponseEntity<Erro> negocio(NegocioException e) {
        return ResponseEntity.status(e.getStatus()).body(new Erro(e.getCodigo(), e.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Erro> validacao(Exception e) {
        return ResponseEntity.badRequest().body(new Erro("VALIDACAO", "Requisição inválida"));
    }
}
