package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.sql.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class SqlScriptTest {
  @Test
  void submittedLoadAndFiveQueryCategoriesExecuteOnH2Adaptation() throws Exception {
    String sql =
        Files.readString(Path.of("sql/recebido.sql"))
            .replace("NUMBER GENERATED AS IDENTITY", "BIGINT GENERATED ALWAYS AS IDENTITY");
    try (Connection c = DriverManager.getConnection("jdbc:h2:mem:submitted;MODE=Oracle", "sa", "");
        Statement s = c.createStatement()) {
      ScriptUtils.executeSqlScript(
          c, new ByteArrayResource(sql.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
      for (String table :
          new String[] {
            "CLIENTE", "VENDEDOR_REPO", "REUNIAO_REPO", "VENDEDOR", "REUNIAO", "ANALISE_IA"
          })
        try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
          assertTrue(r.next());
          assertEquals(5, r.getInt(1));
        }
      try (ResultSet r =
          s.executeQuery(
              "SELECT TOTAL_VENDEDORES,(SELECT COUNT(*) FROM VENDEDOR WHERE"
                  + " VENDEDOR_REPO_ID_VEND_REPO=1) FROM VENDEDOR_REPO WHERE ID_VEND_REPO=1")) {
        assertTrue(r.next());
        assertEquals(5, r.getInt(1));
        assertEquals(2, r.getInt(2));
      }
      assertThrows(
          Exception.class,
          () ->
              ScriptUtils.executeSqlScript(
                  c, new ByteArrayResource(sql.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }
  }
}
