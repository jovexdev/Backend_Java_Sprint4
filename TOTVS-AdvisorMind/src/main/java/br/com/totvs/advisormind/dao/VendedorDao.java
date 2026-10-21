package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.entities.Vendedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VendedorDao implements AutoCloseable {
  private final Connection minhaConexao;
  private final boolean fecharConexao;

  public VendedorDao() throws SQLException, ClassNotFoundException {
    this.minhaConexao = new ConexaoFactory().conexao();
    this.fecharConexao = true;
  }

  // Recebe a mesma conexão quando o Service altera mais de uma tabela.
  public VendedorDao(Connection minhaConexao) {
    this.minhaConexao = minhaConexao;
    this.fecharConexao = false;
  }

  public long criar(Vendedor valor) throws SQLException {
    String sql = "INSERT INTO VENDEDOR (NOME,CARGO,SETOR,TOTAL_REUNIOES,VENDEDOR_REPO_ID_VEND_REPO) VALUES (?,?,?,?,?)";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql, new String[] {"ID_VENDEDOR"})) {
      stmt.setString(1, valor.getNome());
      stmt.setString(2, valor.getCargo());
      stmt.setString(3, valor.getSetor());
      stmt.setInt(4, valor.getTotalReunioes());
      stmt.setLong(5, valor.getVendedorRepoId());
      if (stmt.executeUpdate() != 1) throw new SQLException("Inserção não concluída.");
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next() || rs.getBigDecimal(1) == null) throw new SQLException("Identidade não retornada.");
        long id = rs.getBigDecimal(1).longValueExact();
        if (id <= 0) throw new SQLException("Identidade inválida.");
        return id;
      }
    }
  }

  public int atualizar(Vendedor valor) throws SQLException {
    String sql = "UPDATE VENDEDOR SET NOME=?,CARGO=?,SETOR=?,TOTAL_REUNIOES=?,VENDEDOR_REPO_ID_VEND_REPO=? WHERE ID_VENDEDOR=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setString(1, valor.getNome());
      stmt.setString(2, valor.getCargo());
      stmt.setString(3, valor.getSetor());
      stmt.setInt(4, valor.getTotalReunioes());
      stmt.setLong(5, valor.getVendedorRepoId());
      stmt.setLong(6, valor.getId());
      return stmt.executeUpdate();
    }
  }

  public Vendedor buscar(long id) throws SQLException {
    String sql = "SELECT * FROM VENDEDOR WHERE ID_VENDEDOR=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public Vendedor bloquear(long id) throws SQLException {
    String sql = "SELECT * FROM VENDEDOR WHERE ID_VENDEDOR=? FOR UPDATE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public List<Vendedor> listar() throws SQLException {
    List<Vendedor> valores = new ArrayList<>();
    String sql = "SELECT * FROM VENDEDOR ORDER BY ID_VENDEDOR";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) valores.add(ler(rs));
    }
    return valores;
  }

  public int excluir(long id) throws SQLException {
    String sql = "DELETE FROM VENDEDOR WHERE ID_VENDEDOR=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      return stmt.executeUpdate();
    }
  }

  private Vendedor ler(ResultSet rs) throws SQLException {
    Vendedor valor = new Vendedor();
    valor.setId(rs.getLong("ID_VENDEDOR"));
    valor.setNome(rs.getString("NOME"));
    valor.setCargo(rs.getString("CARGO"));
    valor.setSetor(rs.getString("SETOR"));
    valor.setTotalReunioes(rs.getInt("TOTAL_REUNIOES"));
    valor.setVendedorRepoId(rs.getLong("VENDEDOR_REPO_ID_VEND_REPO"));
    return valor;
  }

  @Override
  public void close() throws SQLException {
    if (fecharConexao) minhaConexao.close();
  }
}
