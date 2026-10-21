package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.entities.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDao implements AutoCloseable {
  private final Connection minhaConexao;
  private final boolean fecharConexao;

  public ClienteDao() throws SQLException, ClassNotFoundException {
    this.minhaConexao = new ConexaoFactory().conexao();
    this.fecharConexao = true;
  }

  // Recebe a mesma conexão quando o Service altera mais de uma tabela.
  public ClienteDao(Connection minhaConexao) {
    this.minhaConexao = minhaConexao;
    this.fecharConexao = false;
  }

  public long criar(Cliente valor) throws SQLException {
    String sql = "INSERT INTO CLIENTE (NOME,EMPRESA,DATA_NASCIMENTO) VALUES (?,?,?)";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql, new String[] {"ID_CLIENTE"})) {
      stmt.setString(1, valor.getNome());
      stmt.setString(2, valor.getEmpresa());
      stmt.setDate(3, java.sql.Date.valueOf(valor.getDataNascimento()));
      if (stmt.executeUpdate() != 1) throw new SQLException("Inserção não concluída.");
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next() || rs.getBigDecimal(1) == null) throw new SQLException("Identidade não retornada.");
        long id = rs.getBigDecimal(1).longValueExact();
        if (id <= 0) throw new SQLException("Identidade inválida.");
        return id;
      }
    }
  }

  public int atualizar(Cliente valor) throws SQLException {
    String sql = "UPDATE CLIENTE SET NOME=?,EMPRESA=?,DATA_NASCIMENTO=? WHERE ID_CLIENTE=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setString(1, valor.getNome());
      stmt.setString(2, valor.getEmpresa());
      stmt.setDate(3, java.sql.Date.valueOf(valor.getDataNascimento()));
      stmt.setLong(4, valor.getId());
      return stmt.executeUpdate();
    }
  }

  public Cliente buscar(long id) throws SQLException {
    String sql = "SELECT * FROM CLIENTE WHERE ID_CLIENTE=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public Cliente bloquear(long id) throws SQLException {
    String sql = "SELECT * FROM CLIENTE WHERE ID_CLIENTE=? FOR UPDATE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) return ler(rs);
        return null;
      }
    }
  }

  public List<Cliente> listar() throws SQLException {
    List<Cliente> valores = new ArrayList<>();
    String sql = "SELECT * FROM CLIENTE ORDER BY ID_CLIENTE";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) valores.add(ler(rs));
    }
    return valores;
  }

  public int excluir(long id) throws SQLException {
    String sql = "DELETE FROM CLIENTE WHERE ID_CLIENTE=?";
    try (PreparedStatement stmt = minhaConexao.prepareStatement(sql)) {
      stmt.setLong(1, id);
      return stmt.executeUpdate();
    }
  }

  private Cliente ler(ResultSet rs) throws SQLException {
    Cliente valor = new Cliente();
    valor.setId(rs.getLong("ID_CLIENTE"));
    valor.setNome(rs.getString("NOME"));
    valor.setEmpresa(rs.getString("EMPRESA"));
    valor.setDataNascimento(rs.getDate("DATA_NASCIMENTO").toLocalDate());
    return valor;
  }

  @Override
  public void close() throws SQLException {
    if (fecharConexao) minhaConexao.close();
  }
}
