package br.com.totvs.advisormind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class VendedorRequest {
  @NotBlank @Size(max = 100)
  private String nome;
  @NotBlank @Size(max = 50)
  private String cargo;
  @NotBlank @Size(max = 50)
  private String setor;
  @NotNull @PositiveOrZero
  private Integer totalReunioes;
  @NotNull @Positive
  private Long vendedorRepoId;

  public VendedorRequest() {}

  public VendedorRequest(String nome, String cargo, String setor, Integer totalReunioes, Long vendedorRepoId) {
    this.nome = nome;
    this.cargo = cargo;
    this.setor = setor;
    this.totalReunioes = totalReunioes;
    this.vendedorRepoId = vendedorRepoId;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getCargo() {
    return cargo;
  }

  public void setCargo(String cargo) {
    this.cargo = cargo;
  }

  public String getSetor() {
    return setor;
  }

  public void setSetor(String setor) {
    this.setor = setor;
  }

  public Integer getTotalReunioes() {
    return totalReunioes;
  }

  public void setTotalReunioes(Integer totalReunioes) {
    this.totalReunioes = totalReunioes;
  }

  public Long getVendedorRepoId() {
    return vendedorRepoId;
  }

  public void setVendedorRepoId(Long vendedorRepoId) {
    this.vendedorRepoId = vendedorRepoId;
  }
}
