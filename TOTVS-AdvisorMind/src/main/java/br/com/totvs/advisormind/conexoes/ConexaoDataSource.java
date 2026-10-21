package br.com.totvs.advisormind.conexoes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import org.springframework.jdbc.datasource.AbstractDataSource;

/** Adapta a factory JDBC para o pool e o gerenciador de transações do Spring. */
public class ConexaoDataSource extends AbstractDataSource {
  private final ConexaoFactory factory;

  public ConexaoDataSource(ConexaoFactory factory) {
    this.factory = factory;
  }

  @Override
  public Connection getConnection() throws SQLException {
    try {
      return factory.conexao();
    } catch (ClassNotFoundException e) {
      throw new SQLException("Driver JDBC não disponível.", e);
    }
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new SQLFeatureNotSupportedException("Use as credenciais configuradas na ConexaoFactory.");
  }
}
