package br.com.totvs.advisormind.exception;

public class PersistenciaException extends RuntimeException {
  public PersistenciaException(Exception causa) {
    super("Não foi possível concluir a operação no banco de dados.", causa);
  }
}
