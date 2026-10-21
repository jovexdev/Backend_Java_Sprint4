package br.com.totvs.advisormind.entities;

import br.com.totvs.advisormind.exception.ApiException;

public enum TipoAnalise {
  SMART_BRIEFING(1, "smart-briefing"),
  CONVERSATIONAL_ANALYTICS(2, "conversational-analytics"),
  VOC_TRACKER(3, "voc-tracker");
  private final int codigo;
  private final String rota;

  TipoAnalise(int codigo, String rota) {
    this.codigo = codigo;
    this.rota = rota;
  }

  public int getCodigo() {
    return codigo;
  }

  public String getRota() {
    return rota;
  }

  public static TipoAnalise porRota(String rota) {
    for (TipoAnalise t : values()) if (t.rota.equals(rota)) return t;
    throw new ApiException(404, "Tipo de análise não encontrado.");
  }
}
