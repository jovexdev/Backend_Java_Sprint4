package br.com.totvs.advisormind.conexoes;

import java.sql.*;

/** Console legado: configuração externa. A API usa o DataSource gerenciado. */
public class ConexaoFactory {
  public Connection conexao() throws ClassNotFoundException, SQLException {
    String url = System.getenv("DB_URL"),
        user = System.getenv("DB_USER"),
        pass = System.getenv("DB_PASSWORD");
    if (url == null || user == null || pass == null)
      throw new SQLException("Configure DB_URL, DB_USER e DB_PASSWORD.");
    return DriverManager.getConnection(url, user, pass);
  }
}
