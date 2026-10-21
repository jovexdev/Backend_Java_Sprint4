package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.entities.VendedorRepo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VendedorRepoDao implements AutoCloseable {
  private final Connection minhaConexao;
  private final boolean fecharConexao;

  public VendedorRepoDao() throws SQLException, ClassNotFoundException {
    this.minhaConexao = new ConexaoFactory().conexao();
    this.fecharConexao = true;
  }

  // Recebe a mesma conexão quando o Service altera mais de uma tabela.
  public VendedorRepoDao(Connection minhaConexao) {
    this.minhaConexao = minhaConexao;
    this.fecharConexao = false;
  }

  public long criar(VendedorRepo valor) throws SQLException {
    String sql = "INSERT INTO VENDEDOR_REPO (TOTAL_VENDEDORES,FILTRO) VALUES (?,?)";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql, new String[] {"ID_VEND_REPO"})) {
      stmt.setInt(1, valor.getTotalVendedores());
      stmt.setString(2, valor.getFiltro());
      if (stmt.executeUpdate() != 1) throw new SQLException("Inserção não concluída.");
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next() || rs.getBigDecimal(1) == null) throw new SQLException("Identidade não retornada.");
        long id = rs.getBigDecimal(1).longValueExact();
        if (id <= 0) throw new SQLException("Identidade inválida.");
        return id;
      }
    }
  }

  public int atualizar(VendedorRepo valor) throws SQLException {
    String sql = "UPDATE VENDEDOR_REPO SET TOTAL_VENDEDORES=?,FILTRO=? WHERE ID_VEND_REPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setInt(1, valor.getTotalVendedores());
      stmt.setString(2, valor.getFiltro());
      stmt.setLong(3, valor.getId());
      return stmt.executeUpdate();
    }
  }

  public VendedorRepo buscar(long id) throws SQLException {
    String sql = "SELECT * FROM VENDEDOR_REPO WHERE ID_VEND_REPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public VendedorRepo bloquear(long id) throws SQLException {
    String sql = "SELECT * FROM VENDEDOR_REPO WHERE ID_VEND_REPO=? FOR UPDATE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public List<VendedorRepo> listar() throws SQLException {
    List<VendedorRepo> valores = new ArrayList<>();
    String sql = "SELECT * FROM VENDEDOR_REPO ORDER BY ID_VEND_REPO";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) valores.add(ler(rs));
    }
    return valores;
  }

  public int excluir(long id) throws SQLException {
    String sql = "DELETE FROM VENDEDOR_REPO WHERE ID_VEND_REPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      return stmt.executeUpdate();
    }
  }

  private VendedorRepo ler(ResultSet rs) throws SQLException {
    VendedorRepo valor = new VendedorRepo();
    valor.setId(rs.getLong("ID_VEND_REPO"));
    valor.setTotalVendedores(rs.getInt("TOTAL_VENDEDORES"));
    valor.setFiltro(rs.getString("FILTRO"));
    return valor;
  }

  public void sincronizar(long id) throws SQLException {
    String sql = "UPDATE VENDEDOR_REPO SET TOTAL_VENDEDORES=(SELECT COUNT(*) FROM VENDEDOR WHERE VENDEDOR_REPO_ID_VEND_REPO=?) WHERE ID_VEND_REPO=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      stmt.setLong(2, id);
      stmt.executeUpdate();
    }
  }

  @Override
  public void close() throws SQLException {
    if (fecharConexao) minhaConexao.close();
  }
}
