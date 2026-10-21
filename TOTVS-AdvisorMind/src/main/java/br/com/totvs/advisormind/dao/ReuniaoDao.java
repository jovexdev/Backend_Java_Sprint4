package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.entities.Reuniao;
import br.com.totvs.advisormind.entities.StatusReuniao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReuniaoDao implements AutoCloseable {
  private final Connection minhaConexao;
  private final boolean fecharConexao;

  public ReuniaoDao() throws SQLException, ClassNotFoundException {
    this.minhaConexao = new ConexaoFactory().conexao();
    this.fecharConexao = true;
  }

  // Recebe a mesma conexão quando o Service altera mais de uma tabela.
  public ReuniaoDao(Connection minhaConexao) {
    this.minhaConexao = minhaConexao;
    this.fecharConexao = false;
  }

  public long criar(Reuniao valor) throws SQLException {
    String sql = "INSERT INTO REUNIAO (TRANSCRICAO,DATA,STATUS,CLIENTE_ID_CLIENTE,VENDEDOR_ID_VENDEDOR,REUNIAO_REPO_ID_REUN_REPO) VALUES (?,?,?,?,?,?)";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql, new String[] {"ID_REUNIAO"})) {
      stmt.setString(1, valor.getTranscricao());
      stmt.setTimestamp(2, Timestamp.valueOf(valor.getData()));
      stmt.setString(3, valor.getStatus().name());
      stmt.setLong(4, valor.getClienteId());
      stmt.setLong(5, valor.getVendedorId());
      stmt.setLong(6, valor.getReuniaoRepoId());
      if (stmt.executeUpdate() != 1) throw new SQLException("Inserção não concluída.");
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next() || rs.getBigDecimal(1) == null) throw new SQLException("Identidade não retornada.");
        long id = rs.getBigDecimal(1).longValueExact();
        if (id <= 0) throw new SQLException("Identidade inválida.");
        return id;
      }
    }
  }

  public int atualizar(Reuniao valor) throws SQLException {
    String sql = "UPDATE REUNIAO SET TRANSCRICAO=?,DATA=?,STATUS=?,CLIENTE_ID_CLIENTE=?,VENDEDOR_ID_VENDEDOR=?,REUNIAO_REPO_ID_REUN_REPO=? WHERE ID_REUNIAO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setString(1, valor.getTranscricao());
      stmt.setTimestamp(2, Timestamp.valueOf(valor.getData()));
      stmt.setString(3, valor.getStatus().name());
      stmt.setLong(4, valor.getClienteId());
      stmt.setLong(5, valor.getVendedorId());
      stmt.setLong(6, valor.getReuniaoRepoId());
      stmt.setLong(7, valor.getId());
      return stmt.executeUpdate();
    }
  }

  public Reuniao buscar(long id) throws SQLException {
    String sql = "SELECT * FROM REUNIAO WHERE ID_REUNIAO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public Reuniao bloquear(long id) throws SQLException {
    String sql = "SELECT * FROM REUNIAO WHERE ID_REUNIAO=? FOR UPDATE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public List<Reuniao> listar() throws SQLException {
    List<Reuniao> valores = new ArrayList<>();
    String sql = "SELECT * FROM REUNIAO ORDER BY ID_REUNIAO";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) valores.add(ler(rs));
    }
    return valores;
  }

  public int excluir(long id) throws SQLException {
    String sql = "DELETE FROM REUNIAO WHERE ID_REUNIAO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      return stmt.executeUpdate();
    }
  }

  private Reuniao ler(ResultSet rs) throws SQLException {
    Reuniao valor = new Reuniao();
    valor.setId(rs.getLong("ID_REUNIAO"));
    valor.setTranscricao(rs.getString("TRANSCRICAO"));
    valor.setData(rs.getTimestamp("DATA").toLocalDateTime());
    valor.setStatus(StatusReuniao.valueOf(rs.getString("STATUS")));
    valor.setClienteId(rs.getLong("CLIENTE_ID_CLIENTE"));
    valor.setVendedorId(rs.getLong("VENDEDOR_ID_VENDEDOR"));
    valor.setReuniaoRepoId(rs.getLong("REUNIAO_REPO_ID_REUN_REPO"));
    return valor;
  }

  @Override
  public void close() throws SQLException {
    if (fecharConexao) minhaConexao.close();
  }
}
