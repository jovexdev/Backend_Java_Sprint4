package br.com.totvs.advisormind;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Respostas JDBC controladas para os testes; não executa SQL nem acessa um servidor. */
class JdbcFixture {
  final Connection connection = mock(Connection.class);
  final List<String> sqlExecutado = new ArrayList<>();
  final Map<String, PreparedStatement> statements = new LinkedHashMap<>();
  final List<ResultSet> resultados = new ArrayList<>();
  final LocalDateTime data = LocalDateTime.of(2026, 9, 1, 10, 0);
  String transcricao = "Texto da reunião";
  String falharEm;
  boolean analiseExiste;

  JdbcFixture() throws Exception {
    when(connection.prepareStatement(anyString())).thenAnswer(call -> preparar(call.getArgument(0)));
    when(connection.prepareStatement(anyString(), any(String[].class))).thenAnswer(call -> preparar(call.getArgument(0)));
  }

  private PreparedStatement preparar(String sql) throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    statements.put(sql, stmt);
    Map<Integer, Object> parametros = new HashMap<>();
    doAnswer(call -> { parametros.put(call.getArgument(0), call.getArgument(1)); return null; })
        .when(stmt).setLong(anyInt(), anyLong());
    when(stmt.executeUpdate()).thenAnswer(call -> {
      sqlExecutado.add(sql);
      if (falharEm != null && sql.startsWith(falharEm))
        throw new SQLIntegrityConstraintViolationException("fixture", "23000", 2291);
      return 1;
    });
    when(stmt.getGeneratedKeys()).thenAnswer(call -> resultado(Collections.singletonMap("#1", new BigDecimal("99"))));
    when(stmt.executeQuery()).thenAnswer(call -> {
      sqlExecutado.add(sql);
      long id = parametros.containsKey(1) ? ((Number) parametros.get(1)).longValue() : 99L;
      Map<String, Object> row = new HashMap<>();
      row.put("ID_CLIENTE", id); row.put("ID_VENDEDOR", id); row.put("ID_VEND_REPO", id);
      row.put("ID_REUN_REPO", id); row.put("ID_REUNIAO", id); row.put("ID_ANALISE", id);
      row.put("NOME", "Ana"); row.put("EMPRESA", "Empresa ME");
      row.put("CARGO", "Consultor"); row.put("SETOR", "ERP");
      row.put("DATA_NASCIMENTO", java.sql.Date.valueOf("1990-01-01"));
      row.put("DATA", Timestamp.valueOf(data)); row.put("DATAHORA_EXECUCAO", Timestamp.valueOf(data));
      row.put("STATUS", "AGENDADA"); row.put("TRANSCRICAO", transcricao);
      row.put("CLIENTE_ID_CLIENTE", 11L); row.put("VENDEDOR_ID_VENDEDOR", 22L);
      row.put("REUNIAO_REPO_ID_REUN_REPO", 33L); row.put("VENDEDOR_REPO_ID_VEND_REPO", 44L);
      row.put("TOTAL_REUNIOES", 10); row.put("TOTAL_VENDEDORES", 1); row.put("FILTRO", "Todos");
      row.put("ARQUIVO_JSON", "{\"reunioes\":[99]}");
      row.put("TIPO", 1); row.put("RESULTADO", "Resumo"); row.put("MODELO_IA", "fixture");
      row.put("TOKENS_UTILIZADOS", 42); row.put("REUNIAO_ID_REUNIAO", 99L);
      row.put("#1", analiseExiste ? 1L : 0L);
      if (sql.startsWith("SELECT ID_REUNIAO")) row.put("ID_REUNIAO", 99L);
      return resultado(row);
    });
    return stmt;
  }

  private ResultSet resultado(Map<String, Object> row) throws Exception {
    ResultSet rs = mock(ResultSet.class);
    resultados.add(rs);
    AtomicInteger position = new AtomicInteger();
    when(rs.next()).thenAnswer(call -> position.getAndIncrement() == 0);
    when(rs.getLong(anyString())).thenAnswer(call -> ((Number) row.get(call.getArgument(0))).longValue());
    when(rs.getLong(anyInt())).thenAnswer(call -> ((Number) row.get("#" + call.getArgument(0))).longValue());
    when(rs.getInt(anyString())).thenAnswer(call -> ((Number) row.get(call.getArgument(0))).intValue());
    when(rs.getString(anyString())).thenAnswer(call -> (String) row.get(call.getArgument(0)));
    when(rs.getDate(anyString())).thenAnswer(call -> (java.sql.Date) row.get(call.getArgument(0)));
    when(rs.getTimestamp(anyString())).thenAnswer(call -> (Timestamp) row.get(call.getArgument(0)));
    when(rs.getBigDecimal(anyInt())).thenAnswer(call -> (BigDecimal) row.get("#" + call.getArgument(0)));
    return rs;
  }

  PreparedStatement insercao(String tabela) {
    for (Map.Entry<String, PreparedStatement> item : statements.entrySet()) {
      if (item.getKey().startsWith("INSERT INTO " + tabela + " ")) return item.getValue();
    }
    throw new AssertionError("INSERT não executado: " + tabela);
  }
}
