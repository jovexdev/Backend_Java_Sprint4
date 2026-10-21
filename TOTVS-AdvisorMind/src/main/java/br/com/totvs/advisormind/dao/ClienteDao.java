package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.entities.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteDao extends JdbcSupport<Cliente> {
  public ClienteDao(DataSource dataSource) {
    super(
        dataSource,
        "CLIENTE",
        "ID_CLIENTE",
        r ->
            new Cliente(
                r.getLong("ID_CLIENTE"),
                r.getString("NOME"),
                r.getString("EMPRESA"),
                r.getDate("DATA_NASCIMENTO").toLocalDate()));
  }

  public long criar(Cliente v) {
    return inserir(
        "NOME,EMPRESA,DATA_NASCIMENTO",
        v.getNome(),
        v.getEmpresa(),
        java.sql.Date.valueOf(v.getDataNascimento()));
  }

  public int atualizar(Cliente v) {
    return atualizarSql(
        "NOME=?,EMPRESA=?,DATA_NASCIMENTO=?",
        v.getNome(),
        v.getEmpresa(),
        java.sql.Date.valueOf(v.getDataNascimento()),
        v.getId());
  }
}
