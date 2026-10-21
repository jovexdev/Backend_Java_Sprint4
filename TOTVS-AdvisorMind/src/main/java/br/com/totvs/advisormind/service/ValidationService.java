package br.com.totvs.advisormind.service;

import br.com.totvs.advisormind.exception.ApiException;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class ValidationService {
  private final Validator validator;

  public ValidationService(Validator validator) {
    this.validator = validator;
  }

  public void validar(Object req) {
    if (req == null || !validator.validate(req).isEmpty())
      throw new ApiException(400, "Dados inválidos: confira campos obrigatórios, datas e limites.");
  }

  public void texto(String field, String value, int max) {
    if (value != null
        && (value.length() > max || value.getBytes(StandardCharsets.UTF_8).length > max))
      throw new ApiException(
          400, field + " excede o limite de " + max + " caracteres/bytes UTF-8.");
  }

  public void id(long id) {
    if (id < 1) throw new ApiException(400, "ID deve ser positivo.");
  }

  public <T> T existe(T entity) {
    if (entity == null) throw new ApiException(404, "Registro não encontrado.");
    return entity;
  }
}
