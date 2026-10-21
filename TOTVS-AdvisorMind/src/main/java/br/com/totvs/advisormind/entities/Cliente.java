package br.com.totvs.advisormind.entities;

import java.time.LocalDate;

public class Cliente {
  private long id;
  private String nome;
  private String empresa;
  private LocalDate dataNascimento;

  public Cliente() {}

  public Cliente(long id, String nome, String empresa, LocalDate dataNascimento) {
    this.id = id;
    this.nome = nome;
    this.empresa = empresa;
    this.dataNascimento = dataNascimento;
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

  public String getEmpresa() {
    return empresa;
  }

  public void setEmpresa(String value) {
    this.empresa = value;
  }

  public LocalDate getDataNascimento() {
    return dataNascimento;
  }

  public void setDataNascimento(LocalDate value) {
    this.dataNascimento = value;
  }

  public String classificarPerfilComercial() {

    if (empresa == null || empresa.trim().isEmpty()) {
      return "PERFIL NÃO IDENTIFICADO";
    }

    String empresaUpper = empresa.trim().toUpperCase();

    boolean indicaGrandeCorporacao =
        empresaUpper.contains("S.A")
            || empresaUpper.contains("S/A")
            || empresaUpper.contains("HOLDING")
            || empresaUpper.contains("GROUP")
            || empresaUpper.contains("GRUPO");

    boolean indicaPequenoNegocio = empresaUpper.matches(".*\\b(ME|MEI|EIRELI)\\b.*");

    if (indicaGrandeCorporacao) {
      return "CONTA ENTERPRISE - abordagem consultiva, ciclo de venda longo";
    } else if (indicaPequenoNegocio) {
      return "PEQUENO NEGÓCIO - abordagem ágil, foco em custo-benefício";
    } else {
      return "MÉDIA EMPRESA - abordagem consultiva com ciclo de venda moderado";
    }
  }

  @Override
  public String toString() {
    return "Cliente #" + id;
  }
}
