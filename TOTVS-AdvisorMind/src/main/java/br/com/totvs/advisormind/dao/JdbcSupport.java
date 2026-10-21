package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import java.io.StringReader;
import java.sql.*;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLExceptionSubclassTranslator;

/** JDBC direto compartilhado pelos seis DAOs; sem estado de conexão por requisição. */
public abstract class JdbcSupport<T> {
  private final ConexaoFactory conexoes;
  private final String table, idColumn;
  private final ResultMapper<T> mapper;
  private final SQLExceptionSubclassTranslator translator = new SQLExceptionSubclassTranslator();

  @FunctionalInterface
  protected interface ResultMapper<R> {
    R map(ResultSet result) throws SQLException;
  }

  @FunctionalInterface
  private interface SqlOperation<R> {
    R execute(Connection connection) throws SQLException;
  }

  protected JdbcSupport(
      ConexaoFactory conexoes, String table, String idColumn, ResultMapper<T> mapper) {
    this.conexoes = conexoes;
    this.table = table;
    this.idColumn = idColumn;
    this.mapper = mapper;
  }

  private <R> R executar(String sql, SqlOperation<R> operation) {
    Connection connection = conexoes.conexao();
    try {
      return operation.execute(connection);
    } catch (SQLException e) {
      DataAccessException translated = translator.translate("Operação JDBC", sql, e);
      throw translated != null ? translated : new UncategorizedSQLException("Operação JDBC", sql, e);
    } finally {
      conexoes.liberar(connection);
    }
  }

  private void bind(PreparedStatement ps, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) {
      Object value = args[i];
      int index = i + 1;
      if (value == null) ps.setNull(index, Types.VARCHAR); // FILTRO opcional
      else if (value instanceof StringReader reader) ps.setCharacterStream(index, reader);
      else if (value instanceof String text) ps.setString(index, text);
      else if (value instanceof Long number) ps.setLong(index, number);
      else if (value instanceof Integer number) ps.setInt(index, number);
      else if (value instanceof java.sql.Date date) ps.setDate(index, date);
      else if (value instanceof Timestamp timestamp) ps.setTimestamp(index, timestamp);
      else ps.setObject(index, value);
    }
  }

  // Identificadores/trechos SQL vêm exclusivamente das constantes dos DAOs.
  protected long inserir(String columns, Object... args) {
    String sql = "INSERT INTO " + table + " (" + columns + ") VALUES ("
        + String.join(",", Collections.nCopies(args.length, "?")) + ")";
    return executar(sql, connection -> {
      // Oracle exige o NOME da coluna; RETURN_GENERATED_KEYS pode devolver ROWID.
      try (PreparedStatement ps = connection.prepareStatement(sql, new String[] {idColumn})) {
        bind(ps, args);
        if (ps.executeUpdate() != 1)
          throw new DataRetrievalFailureException("Inserção não afetou exatamente um registro.");
        try (ResultSet keys = ps.getGeneratedKeys()) {
          if (!keys.next() || keys.getBigDecimal(1) == null)
            throw new DataRetrievalFailureException("Banco não devolveu identidade.");
          long id = keys.getBigDecimal(1).longValueExact();
          if (id < 1) throw new DataRetrievalFailureException("Identidade inválida.");
          return id;
        }
      }
    });
  }

  protected <R> List<R> consultar(String sql, ResultMapper<R> rowMapper, Object... args) {
    return executar(sql, connection -> {
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        bind(ps, args);
        try (ResultSet result = ps.executeQuery()) {
          List<R> rows = new ArrayList<>();
          while (result.next()) rows.add(rowMapper.map(result));
          return rows;
        }
      }
    });
  }

  protected List<T> consultar(String sql, Object... args) {
    return consultar(sql, mapper, args);
  }

  public T buscar(long id) {
    var rows = consultar("SELECT * FROM " + table + " WHERE " + idColumn + "=?", id);
    return rows.isEmpty() ? null : rows.getFirst();
  }

  public T bloquear(long id) {
    var rows = consultar("SELECT * FROM " + table + " WHERE " + idColumn + "=? FOR UPDATE", id);
    return rows.isEmpty() ? null : rows.getFirst();
  }

  public List<T> listar() {
    return consultar("SELECT * FROM " + table + " ORDER BY " + idColumn);
  }

  public int excluir(long id) {
    return executarUpdate("DELETE FROM " + table + " WHERE " + idColumn + "=?", id);
  }

  protected int executarUpdate(String sql, Object... args) {
    return executar(sql, connection -> {
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        bind(ps, args);
        return ps.executeUpdate();
      }
    });
  }

  protected int atualizarSql(String assignments, Object... args) {
    return executarUpdate("UPDATE " + table + " SET " + assignments + " WHERE " + idColumn + "=?", args);
  }
}
