package br.com.totvs.advisormind.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class ClienteRequest {
  @NotBlank @Size(max = 100)
  private String nome;
  @NotBlank @Size(max = 100)
  private String empresa;
  @NotNull @PastOrPresent @JsonFormat(pattern = "uuuu-MM-dd", lenient = OptBoolean.FALSE)
  private LocalDate dataNascimento;

  public ClienteRequest() {}

  public ClienteRequest(String nome, String empresa, LocalDate dataNascimento) {
    this.nome = nome;
    this.empresa = empresa;
    this.dataNascimento = dataNascimento;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getEmpresa() {
    return empresa;
  }

  public void setEmpresa(String empresa) {
    this.empresa = empresa;
  }

  public LocalDate getDataNascimento() {
    return dataNascimento;
  }

  public void setDataNascimento(LocalDate dataNascimento) {
    this.dataNascimento = dataNascimento;
  }
}
