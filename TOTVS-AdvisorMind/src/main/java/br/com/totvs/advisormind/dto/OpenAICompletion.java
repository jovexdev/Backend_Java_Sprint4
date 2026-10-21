package br.com.totvs.advisormind.dto;

public class OpenAICompletion {
  private String resultado;
  private String modelo;
  private int tokensUtilizados;

  public OpenAICompletion() {}

  public OpenAICompletion(String resultado, String modelo, int tokensUtilizados) {
    this.resultado = resultado;
    this.modelo = modelo;
    this.tokensUtilizados = tokensUtilizados;
  }

  public String getResultado() {
    return resultado;
  }

  public void setResultado(String resultado) {
    this.resultado = resultado;
  }

  public String getModelo() {
    return modelo;
  }

  public void setModelo(String modelo) {
    this.modelo = modelo;
  }

  public int getTokensUtilizados() {
    return tokensUtilizados;
  }

  public void setTokensUtilizados(int tokensUtilizados) {
    this.tokensUtilizados = tokensUtilizados;
  }
}
