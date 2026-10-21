package br.com.totvs.advisormind.entities;

public class ReuniaoRepo {
  private long id;
  private String arquivoJson;
  private int totalReunioes;

  public ReuniaoRepo() {}

  public ReuniaoRepo(long id, String arquivoJson, int totalReunioes) {
    this.id = id;
    this.arquivoJson = arquivoJson;
    this.totalReunioes = totalReunioes;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getArquivoJson() {
    return arquivoJson;
  }

  public void setArquivoJson(String arquivoJson) {
    this.arquivoJson = arquivoJson;
  }

  public int getTotalReunioes() {
    return totalReunioes;
  }

  public void setTotalReunioes(int totalReunioes) {
    this.totalReunioes = totalReunioes;
  }
  @Override
  public String toString() {
    return "Repositório de reuniões #" + id;
  }
}
