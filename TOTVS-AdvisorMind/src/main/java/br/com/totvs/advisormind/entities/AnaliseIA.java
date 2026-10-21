package br.com.totvs.advisormind.entities;

import java.time.LocalDateTime;

public class AnaliseIA {
  private long id;
  private int tipo;
  private String resultado;
  private String modeloIa;
  private LocalDateTime datahoraExecucao;
  private int tokensUtilizados;
  private long reuniaoId;

  public AnaliseIA() {}

  public AnaliseIA(long id, int tipo, String resultado, String modeloIa, LocalDateTime datahoraExecucao, int tokensUtilizados, long reuniaoId) {
    this.id = id;
    this.tipo = tipo;
    this.resultado = resultado;
    this.modeloIa = modeloIa;
    this.datahoraExecucao = datahoraExecucao;
    this.tokensUtilizados = tokensUtilizados;
    this.reuniaoId = reuniaoId;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getTipo() {
    return tipo;
  }

  public void setTipo(int tipo) {
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

  public LocalDateTime getDatahoraExecucao() {
    return datahoraExecucao;
  }

  public void setDatahoraExecucao(LocalDateTime datahoraExecucao) {
    this.datahoraExecucao = datahoraExecucao;
  }

  public int getTokensUtilizados() {
    return tokensUtilizados;
  }

  public void setTokensUtilizados(int tokensUtilizados) {
    this.tokensUtilizados = tokensUtilizados;
  }

  public long getReuniaoId() {
    return reuniaoId;
  }

  public void setReuniaoId(long reuniaoId) {
    this.reuniaoId = reuniaoId;
  }
  @Override
  public String toString() {
    return "Análise de IA #" + id;
  }
}
