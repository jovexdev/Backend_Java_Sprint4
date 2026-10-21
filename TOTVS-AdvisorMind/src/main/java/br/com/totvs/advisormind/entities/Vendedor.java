package br.com.totvs.advisormind.entities;

import java.math.*;
import java.time.*;

public class Vendedor {
  private long id;
  private String nome;
  private String cargo;
  private String setor;
  private int totalReunioes;
  private long vendedorRepoId;

  public Vendedor() {}

  public Vendedor(
      long id, String nome, String cargo, String setor, int totalReunioes, long vendedorRepoId) {
    this.id = id;
    this.nome = nome;
    this.cargo = cargo;
    this.setor = setor;
    this.totalReunioes = totalReunioes;
    this.vendedorRepoId = vendedorRepoId;
  }

  public long getId() {
    return id;
  }

  public void setId(long value) {
    this.id = value;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String value) {
    this.nome = value;
  }

  public String getCargo() {
    return cargo;
  }

  public void setCargo(String value) {
    this.cargo = value;
  }

  public String getSetor() {
    return setor;
  }

  public void setSetor(String value) {
    this.setor = value;
  }

  public int getTotalReunioes() {
    return totalReunioes;
  }

  public void setTotalReunioes(int value) {
    this.totalReunioes = value;
  }

  public long getVendedorRepoId() {
    return vendedorRepoId;
  }

  public void setVendedorRepoId(long value) {
    this.vendedorRepoId = value;
  }

  public String calcularNivelSenioridade() {
    if (totalReunioes < 5) {
      return "TRAINEE";
    } else if (totalReunioes < 15) {
      return "JÚNIOR";
    } else if (totalReunioes < 30) {
      return "PLENO";
    } else if (totalReunioes < 60) {
      return "SÊNIOR";
    } else {
      return "ESPECIALISTA";
    }
  }

  public void registrarReuniao() {
    totalReunioes = Math.addExact(totalReunioes, 1);
  }

  /** O esquema não contém ticket médio. Use o overload com valor explícito. */
  @Deprecated
  public BigDecimal calcularComissaoEstimada() {
    throw new IllegalStateException(
        "Ticket médio não persistido; informe-o explicitamente para simulação.");
  }

  public BigDecimal calcularComissaoEstimada(BigDecimal ticket) {
    if (ticket == null
        || ticket.signum() < 0
        || ticket.scale() > 2
        || ticket.precision() - ticket.scale() > 12)
      throw new IllegalArgumentException("Ticket de simulação inválido.");
    String base =
        cargo == null
            ? "0.02"
            : cargo.equalsIgnoreCase("Gerente")
                ? "0.015"
                : cargo.equalsIgnoreCase("Consultor") ? "0.025" : "0.03";
    String bonus =
        switch (calcularNivelSenioridade()) {
          case "ESPECIALISTA" -> "0.10";
          case "SÊNIOR" -> "0.06";
          case "PLENO" -> "0.03";
          case "JÚNIOR" -> "0.01";
          default -> "0.0";
        };
    return ticket
        .multiply(BigDecimal.valueOf(totalReunioes))
        .multiply(new BigDecimal(base).add(new BigDecimal(bonus)))
        .setScale(2, RoundingMode.HALF_EVEN);
  }

  @Override
  public String toString() {
    return "Vendedor #" + id;
  }
}
