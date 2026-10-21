package br.com.totvs.advisormind.conexoes;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

/** Conexões da API e do console, configuradas pelos perfis local/oracle. */
@Component
public class ConexaoFactory {
  private final DataSource dataSource;

  public ConexaoFactory(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Connection conexao() {
    return DataSourceUtils.getConnection(dataSource);
  }

  public void liberar(Connection connection) {
    // Dentro de @Transactional, só o Spring deve fechar/confirmar a conexão.
    DataSourceUtils.releaseConnection(connection, dataSource);
  }
}
