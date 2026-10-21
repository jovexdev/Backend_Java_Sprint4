package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.entities.AnaliseIA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AnaliseIADao implements AutoCloseable {
  private final Connection minhaConexao;
  private final boolean fecharConexao;

  public AnaliseIADao() throws SQLException, ClassNotFoundException {
    this.minhaConexao = new ConexaoFactory().conexao();
    this.fecharConexao = true;
  }

  // Recebe a mesma conexão quando o Service altera mais de uma tabela.
  public AnaliseIADao(Connection minhaConexao) {
    this.minhaConexao = minhaConexao;
    this.fecharConexao = false;
  }

  public long criar(AnaliseIA valor) throws SQLException {
    String sql = "INSERT INTO ANALISE_IA (TIPO,RESULTADO,MODELO_IA,DATAHORA_EXECUCAO,TOKENS_UTILIZADOS,REUNIAO_ID_REUNIAO) VALUES (?,?,?,?,?,?)";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql, new String[] {"ID_ANALISE"})) {
      stmt.setInt(1, valor.getTipo());
      stmt.setString(2, valor.getResultado());
      stmt.setString(3, valor.getModeloIa());
      stmt.setTimestamp(4, Timestamp.valueOf(valor.getDatahoraExecucao()));
      stmt.setInt(5, valor.getTokensUtilizados());
      stmt.setLong(6, valor.getReuniaoId());
      if (stmt.executeUpdate() != 1) throw new SQLException("Inserção não concluída.");
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next() || rs.getBigDecimal(1) == null) throw new SQLException("Identidade não retornada.");
        long id = rs.getBigDecimal(1).longValueExact();
        if (id <= 0) throw new SQLException("Identidade inválida.");
        return id;
      }
    }
  }

  public int atualizar(AnaliseIA valor) throws SQLException {
    String sql = "UPDATE ANALISE_IA SET TIPO=?,RESULTADO=?,MODELO_IA=?,DATAHORA_EXECUCAO=?,TOKENS_UTILIZADOS=?,REUNIAO_ID_REUNIAO=? WHERE ID_ANALISE=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setInt(1, valor.getTipo());
      stmt.setString(2, valor.getResultado());
      stmt.setString(3, valor.getModeloIa());
      stmt.setTimestamp(4, Timestamp.valueOf(valor.getDatahoraExecucao()));
      stmt.setInt(5, valor.getTokensUtilizados());
      stmt.setLong(6, valor.getReuniaoId());
      stmt.setLong(7, valor.getId());
      return stmt.executeUpdate();
    }
  }

  public AnaliseIA buscar(long id) throws SQLException {
    String sql = "SELECT * FROM ANALISE_IA WHERE ID_ANALISE=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public AnaliseIA bloquear(long id) throws SQLException {
    String sql = "SELECT * FROM ANALISE_IA WHERE ID_ANALISE=? FOR UPDATE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public List<AnaliseIA> listar() throws SQLException {
    List<AnaliseIA> valores = new ArrayList<>();
    String sql = "SELECT * FROM ANALISE_IA ORDER BY ID_ANALISE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) valores.add(ler(rs));
    }
    return valores;
  }

  public int excluir(long id) throws SQLException {
    String sql = "DELETE FROM ANALISE_IA WHERE ID_ANALISE=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      return stmt.executeUpdate();
    }
  }

  private AnaliseIA ler(ResultSet rs) throws SQLException {
    AnaliseIA valor = new AnaliseIA();
    valor.setId(rs.getLong("ID_ANALISE"));
    valor.setTipo(rs.getInt("TIPO"));
    valor.setResultado(rs.getString("RESULTADO"));
    valor.setModeloIa(rs.getString("MODELO_IA"));
    valor.setDatahoraExecucao(rs.getTimestamp("DATAHORA_EXECUCAO").toLocalDateTime());
    valor.setTokensUtilizados(rs.getInt("TOKENS_UTILIZADOS"));
    valor.setReuniaoId(rs.getLong("REUNIAO_ID_REUNIAO"));
    return valor;
  }

  public List<AnaliseIA> porReuniao(long id) throws SQLException {
    List<AnaliseIA> valores = new ArrayList<>();
    String sql = "SELECT * FROM ANALISE_IA WHERE REUNIAO_ID_REUNIAO=? ORDER BY TIPO";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) valores.add(ler(rs));
      }
    }
    return valores;
  }

  public boolean existeTipo(long reuniao, int tipo) throws SQLException {
    String sql = "SELECT COUNT(*) FROM ANALISE_IA WHERE REUNIAO_ID_REUNIAO=? AND TIPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, reuniao);
      stmt.setInt(2, tipo);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() && rs.getLong(1) > 0;
      }
    }
  }

  @Override
  public void close() throws SQLException {
    if (fecharConexao) minhaConexao.close();
  }
}
