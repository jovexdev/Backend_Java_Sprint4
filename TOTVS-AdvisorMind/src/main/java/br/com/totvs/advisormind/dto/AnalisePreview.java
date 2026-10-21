package br.com.totvs.advisormind.dto;

public class AnalisePreview {
  private long reuniaoId;
  private String tipo;
  private String resultado;
  private String modeloIa;
  private int tokensUtilizados;
  private boolean persistida;

  public AnalisePreview() {}

  public AnalisePreview(long reuniaoId, String tipo, String resultado, String modeloIa, int tokensUtilizados, boolean persistida) {
    this.reuniaoId = reuniaoId;
    this.tipo = tipo;
    this.resultado = resultado;
    this.modeloIa = modeloIa;
    this.tokensUtilizados = tokensUtilizados;
    this.persistida = persistida;
  }

  public long getReuniaoId() {
    return reuniaoId;
  }

  public void setReuniaoId(long reuniaoId) {
    this.reuniaoId = reuniaoId;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public String getResultado() {
    return resultado;
  }

  public void setResultado(String resultado) {
    this.resultado = resultado;
  }

  public String getModeloIa() {
    return modeloIa;
  }

  public void setModeloIa(String modeloIa) {
    this.modeloIa = modeloIa;
  }

  public int getTokensUtilizados() {
    return tokensUtilizados;
  }

  public void setTokensUtilizados(int tokensUtilizados) {
    this.tokensUtilizados = tokensUtilizados;
  }

  public boolean getPersistida() {
    return persistida;
  }

  public void setPersistida(boolean persistida) {
    this.persistida = persistida;
  }
}
