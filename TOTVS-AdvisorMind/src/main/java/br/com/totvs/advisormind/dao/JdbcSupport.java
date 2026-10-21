package br.com.totvs.advisormind.dao;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;

public abstract class JdbcSupport<T> {
  protected final JdbcTemplate jdbc;
  private final String table, idColumn;
  private final RowMapper<T> mapper;

  protected JdbcSupport(JdbcTemplate jdbc, String table, String idColumn, RowMapper<T> mapper) {
    this.jdbc = jdbc;
    this.table = table;
    this.idColumn = idColumn;
    this.mapper = mapper;
  }

  protected void bind(PreparedStatement ps, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) {
      Object v = args[i];
      if (v instanceof StringReader reader) ps.setCharacterStream(i + 1, reader);
      else ps.setObject(i + 1, v);
    }
  }

  public long inserir(String columns, Object... args) {
    String sql =
        "INSERT INTO "
            + table
            + " ("
            + columns
            + ") VALUES ("
            + String.join(",", Collections.nCopies(args.length, "?"))
            + ")";
    var key = new GeneratedKeyHolder();
    jdbc.update(
        c -> {
          PreparedStatement ps = c.prepareStatement(sql, new String[] {idColumn});
          bind(ps, args);
          return ps;
        },
        key);
    Number value = key.getKey();
    if (value == null) throw new IllegalStateException("Banco não devolveu identidade.");
    return new BigDecimal(value.toString()).longValueExact();
  }

  public T buscar(long id) {
    var rows = jdbc.query("SELECT * FROM " + table + " WHERE " + idColumn + "=?", mapper, id);
    return rows.isEmpty() ? null : rows.getFirst();
  }

  public T bloquear(long id) {
    var rows =
        jdbc.query("SELECT * FROM " + table + " WHERE " + idColumn + "=? FOR UPDATE", mapper, id);
    return rows.isEmpty() ? null : rows.getFirst();
  }

  public List<T> listar() {
    return jdbc.query("SELECT * FROM " + table + " ORDER BY " + idColumn, mapper);
  }

  public int excluir(long id) {
    return jdbc.update("DELETE FROM " + table + " WHERE " + idColumn + "=?", id);
  }

  protected int atualizarSql(String assignments, Object... args) {
    return jdbc.update(
        c -> {
          PreparedStatement ps =
              c.prepareStatement(
                  "UPDATE " + table + " SET " + assignments + " WHERE " + idColumn + "=?");
          bind(ps, args);
          return ps;
        });
  }
}
