package br.com.totvs.advisormind.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiErrorHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ApiErrorHandler.class);

  private ResponseEntity<Map<String, Object>> error(int status, String message) {
    return ResponseEntity.status(status)
        .body(Map.of("status", status, "mensagem", message, "timestamp", Instant.now().toString()));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<?> domain(ApiException e) {
    return error(e.getStatus(), e.getMessage());
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    ConstraintViolationException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class,
    IllegalArgumentException.class
  })
  public ResponseEntity<?> invalid(Exception e) {
    return error(
        400,
        "Requisição inválida. Confira tipos, campos obrigatórios e limites no contrato da API.");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> duplicate(Exception e) {
    return error(
        409, "Conflito de unicidade ou vínculo: confira duplicidades e registros dependentes.");
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<?> missing(Exception e) {
    return error(404, "Recurso não encontrado.");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> method(Exception e) {
    return error(405, "Método HTTP não permitido.");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<?> media(Exception e) {
    return error(415, "Use Content-Type application/json.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> unexpected(Exception e) {
    LOG.error("Falha interna: {}", e.getClass().getSimpleName());
    return error(500, "Não foi possível concluir a operação.");
  }
}
