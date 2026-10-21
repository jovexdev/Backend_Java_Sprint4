package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Somente schema de teste explicitamente autorizado. Sem DDL/limpeza; transacao sempre revertida.
 */
@EnabledIfEnvironmentVariable(named = "RUN_ORACLE_TRANSACTIONAL", matches = "true")
class OracleTransactionalProbeTest {
  long insert(Connection c, String table, String key, String columns, Object... values)
      throws SQLException {
    try (var p =
        c.prepareStatement(
            "INSERT INTO "
                + table
                + "("
                + columns
                + ") VALUES("
                + String.join(",", Collections.nCopies(values.length, "?"))
                + ")",
            new String[] {key})) {
      for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
      p.executeUpdate();
      try (var r = p.getGeneratedKeys()) {
        assertTrue(r.next());
        long id = r.getLong(1);
        assertTrue(id > 0);
        return id;
      }
    }
  }

  @Test
  void identitiesDatesClobConstraintsCrudAndRollback() throws Exception {
    String marker = "TEST_" + UUID.randomUUID().toString();
    try (var c =
        DriverManager.getConnection(
            System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"))) {
      c.setAutoCommit(false);
      try {
        long vr = insert(c, "VENDEDOR_REPO", "ID_VEND_REPO", "TOTAL_VENDEDORES,FILTRO", 0, marker);
        long rr =
            insert(
                c,
                "REUNIAO_REPO",
                "ID_REUN_REPO",
                "ARQUIVO_JSON,TOTAL_REUNIOES",
                "{\"reunioes\":[]}",
                0);
        long cl =
            insert(
                c,
                "CLIENTE",
                "ID_CLIENTE",
                "NOME,EMPRESA,DATA_NASCIMENTO",
                marker,
                "Test only",
                java.sql.Date.valueOf("1990-01-01"));
        long v =
            insert(
                c,
                "VENDEDOR",
                "ID_VENDEDOR",
                "NOME,CARGO,SETOR,TOTAL_REUNIOES,VENDEDOR_REPO_ID_VEND_REPO",
                marker,
                "Consultor",
                "Test",
                0,
                vr);
        Timestamp when = Timestamp.valueOf(LocalDateTime.of(2026, 9, 1, 10, 0, 17));
        long r =
            insert(
                c,
                "REUNIAO",
                "ID_REUNIAO",
                "TRANSCRICAO,DATA,STATUS,CLIENTE_ID_CLIENTE,VENDEDOR_ID_VENDEDOR,REUNIAO_REPO_ID_REUN_REPO",
                "TEST ONLY",
                when,
                "AGENDADA",
                cl,
                v,
                rr);
        long a =
            insert(
                c,
                "ANALISE_IA",
                "ID_ANALISE",
                "TIPO,RESULTADO,MODELO_IA,DATAHORA_EXECUCAO,TOKENS_UTILIZADOS,REUNIAO_ID_REUNIAO",
                1,
                "TEST FIXTURE - not a provider result",
                "TEST_FIXTURE",
                when,
                5,
                r);
        try (var q = c.prepareStatement("SELECT DATA FROM REUNIAO WHERE ID_REUNIAO=?")) {
          q.setLong(1, r);
          try (var rs = q.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(when.toLocalDateTime(), rs.getTimestamp(1).toLocalDateTime());
          }
        }
        String clob = "{\"probe\":\"" + "a".repeat(6000) + "\"}";
        try (var q =
            c.prepareStatement("UPDATE REUNIAO_REPO SET ARQUIVO_JSON=? WHERE ID_REUN_REPO=?")) {
          q.setCharacterStream(1, new java.io.StringReader(clob));
          q.setLong(2, rr);
          assertEquals(1, q.executeUpdate());
        }
        try (var q =
            c.prepareStatement("SELECT ARQUIVO_JSON FROM REUNIAO_REPO WHERE ID_REUN_REPO=?")) {
          q.setLong(1, rr);
          try (var rs = q.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(clob, rs.getString(1));
          }
        }
        assertThrows(
            SQLException.class,
            () ->
                insert(
                    c,
                    "CLIENTE",
                    "ID_CLIENTE",
                    "NOME,EMPRESA,DATA_NASCIMENTO",
                    marker,
                    "Test only",
                    java.sql.Date.valueOf("1990-01-01")));
        assertThrows(
            SQLException.class,
            () ->
                insert(
                    c,
                    "REUNIAO",
                    "ID_REUNIAO",
                    "TRANSCRICAO,DATA,STATUS,CLIENTE_ID_CLIENTE,VENDEDOR_ID_VENDEDOR,REUNIAO_REPO_ID_REUN_REPO",
                    "TEST",
                    when,
                    "AGENDADA",
                    cl,
                    v,
                    rr));
        assertThrows(
            SQLException.class,
            () ->
                insert(
                    c,
                    "ANALISE_IA",
                    "ID_ANALISE",
                    "TIPO,RESULTADO,MODELO_IA,DATAHORA_EXECUCAO,TOKENS_UTILIZADOS,REUNIAO_ID_REUNIAO",
                    1,
                    "TEST",
                    "TEST",
                    when,
                    5,
                    r));
        try (var q = c.prepareStatement("DELETE FROM VENDEDOR WHERE ID_VENDEDOR=?")) {
          q.setLong(1, v);
          assertThrows(SQLException.class, q::executeUpdate);
        }
        try (var q =
            c.prepareStatement("UPDATE VENDEDOR SET TOTAL_REUNIOES=? WHERE ID_VENDEDOR=?")) {
          q.setInt(1, 2);
          q.setLong(2, v);
          assertEquals(1, q.executeUpdate());
        }
        for (Object[] item :
            new Object[][] {
              {"ANALISE_IA", "ID_ANALISE", a},
              {"REUNIAO", "ID_REUNIAO", r},
              {"VENDEDOR", "ID_VENDEDOR", v},
              {"CLIENTE", "ID_CLIENTE", cl},
              {"REUNIAO_REPO", "ID_REUN_REPO", rr},
              {"VENDEDOR_REPO", "ID_VEND_REPO", vr}
            })
          try (var q = c.prepareStatement("DELETE FROM " + item[0] + " WHERE " + item[1] + "=?")) {
            q.setLong(1, (Long) item[2]);
            assertEquals(1, q.executeUpdate());
          }
      } finally {
        c.rollback();
      }
      try (var q = c.prepareStatement("SELECT COUNT(*) FROM VENDEDOR WHERE NOME=?")) {
        q.setString(1, marker);
        try (var rs = q.executeQuery()) {
          assertTrue(rs.next());
          assertEquals(0, rs.getInt(1));
        }
      }
    }
  }
}
