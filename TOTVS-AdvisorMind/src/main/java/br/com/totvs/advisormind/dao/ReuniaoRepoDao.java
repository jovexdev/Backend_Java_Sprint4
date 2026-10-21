package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.entities.ReuniaoRepo;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReuniaoRepoDao implements AutoCloseable {
  private final Connection minhaConexao;
  private final boolean fecharConexao;

  public ReuniaoRepoDao() throws SQLException, ClassNotFoundException {
    this.minhaConexao = new ConexaoFactory().conexao();
    this.fecharConexao = true;
  }

  // Recebe a mesma conexão quando o Service altera mais de uma tabela.
  public ReuniaoRepoDao(Connection minhaConexao) {
    this.minhaConexao = minhaConexao;
    this.fecharConexao = false;
  }

  public long criar(ReuniaoRepo valor) throws SQLException {
    String sql = "INSERT INTO REUNIAO_REPO (ARQUIVO_JSON,TOTAL_REUNIOES) VALUES (?,?)";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql, new String[] {"ID_REUN_REPO"})) {
      stmt.setCharacterStream(1, new StringReader(valor.getArquivoJson()));
      stmt.setInt(2, valor.getTotalReunioes());
      if (stmt.executeUpdate() != 1) throw new SQLException("Inserção não concluída.");
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next() || rs.getBigDecimal(1) == null) throw new SQLException("Identidade não retornada.");
        long id = rs.getBigDecimal(1).longValueExact();
        if (id <= 0) throw new SQLException("Identidade inválida.");
        return id;
      }
    }
  }

  public int atualizar(ReuniaoRepo valor) throws SQLException {
    String sql = "UPDATE REUNIAO_REPO SET ARQUIVO_JSON=?,TOTAL_REUNIOES=? WHERE ID_REUN_REPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setCharacterStream(1, new StringReader(valor.getArquivoJson()));
      stmt.setInt(2, valor.getTotalReunioes());
      stmt.setLong(3, valor.getId());
      return stmt.executeUpdate();
    }
  }

  public ReuniaoRepo buscar(long id) throws SQLException {
    String sql = "SELECT * FROM REUNIAO_REPO WHERE ID_REUN_REPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public ReuniaoRepo bloquear(long id) throws SQLException {
    String sql = "SELECT * FROM REUNIAO_REPO WHERE ID_REUN_REPO=? FOR UPDATE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public List<ReuniaoRepo> listar() throws SQLException {
    List<ReuniaoRepo> valores = new ArrayList<>();
    String sql = "SELECT * FROM REUNIAO_REPO ORDER BY ID_REUN_REPO";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) valores.add(ler(rs));
    }
    return valores;
  }

  public int excluir(long id) throws SQLException {
    String sql = "DELETE FROM REUNIAO_REPO WHERE ID_REUN_REPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      return stmt.executeUpdate();
    }
  }

  private ReuniaoRepo ler(ResultSet rs) throws SQLException {
    ReuniaoRepo valor = new ReuniaoRepo();
    valor.setId(rs.getLong("ID_REUN_REPO"));
    valor.setArquivoJson(rs.getString("ARQUIVO_JSON"));
    valor.setTotalReunioes(rs.getInt("TOTAL_REUNIOES"));
    return valor;
  }

  public List<Long> reunioes(long id) throws SQLException {
    List<Long> ids = new ArrayList<>();
    String sql = "SELECT ID_REUNIAO FROM REUNIAO WHERE REUNIAO_REPO_ID_REUN_REPO=? ORDER BY ID_REUNIAO";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) ids.add(rs.getLong("ID_REUNIAO"));
      }
    }
    return ids;
  }

  @Override
  public void close() throws SQLException {
    if (fecharConexao) minhaConexao.close();
  }
}
