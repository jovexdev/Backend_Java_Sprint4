package br.com.totvs.advisormind.entities;

public class VendedorRepo {
  private long id;
  private int totalVendedores;
  private String filtro;

  public VendedorRepo() {}

  public VendedorRepo(long id, int totalVendedores, String filtro) {
    this.id = id;
    this.totalVendedores = totalVendedores;
    this.filtro = filtro;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getTotalVendedores() {
    return totalVendedores;
  }

  public void setTotalVendedores(int totalVendedores) {
    this.totalVendedores = totalVendedores;
  }

  public String getFiltro() {
    return filtro;
  }

  public void setFiltro(String filtro) {
    this.filtro = filtro;
  }
  @Override
  public String toString() {
    return "Repositório de vendedores #" + id;
  }
}
