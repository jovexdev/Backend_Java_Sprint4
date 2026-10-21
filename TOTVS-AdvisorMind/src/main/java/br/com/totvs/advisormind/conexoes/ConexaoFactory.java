package br.com.totvs.advisormind.conexoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Abertura JDBC no mesmo fluxo do exemplo do professor. */
public class ConexaoFactory {
  private final String URL;
  private final String USUARIO;
  private final String SENHA;

  public ConexaoFactory() {
    this(
        System.getenv().getOrDefault("DB_URL", "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl"),
        System.getenv("DB_USER"),
        System.getenv("DB_PASSWORD"));
  }

  public ConexaoFactory(String url, String usuario, String senha) {
    this.URL = url;
    this.USUARIO = usuario;
    this.SENHA = senha;
  }

  public Connection conexao() throws ClassNotFoundException, SQLException {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    return DriverManager.getConnection(URL, USUARIO, SENHA);
  }
}
