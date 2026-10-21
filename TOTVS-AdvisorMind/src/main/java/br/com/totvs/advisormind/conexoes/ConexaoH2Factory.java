package br.com.totvs.advisormind.conexoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Somente perfil local/testes. A ConexaoFactory principal segue o exemplo Oracle. */
public class ConexaoH2Factory extends ConexaoFactory {
  private final String url, usuario, senha;

  public ConexaoH2Factory(String url, String usuario, String senha) {
    super(url, usuario, senha);
    this.url = url;
    this.usuario = usuario;
    this.senha = senha;
  }

  @Override
  public Connection conexao() throws ClassNotFoundException, SQLException {
    Class.forName("org.h2.Driver");
    return DriverManager.getConnection(url, usuario, senha);
  }
}
