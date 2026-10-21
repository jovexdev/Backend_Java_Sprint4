package br.com.totvs.advisormind.dto;

import jakarta.validation.constraints.Size;

public class VendedorRepoRequest {
  @Size(max = 100)
  private String filtro;

  public VendedorRepoRequest() {}

  public VendedorRepoRequest(String filtro) {
    this.filtro = filtro;
  }

  public String getFiltro() {
    return filtro;
  }

  public void setFiltro(String filtro) {
    this.filtro = filtro;
  }
}
