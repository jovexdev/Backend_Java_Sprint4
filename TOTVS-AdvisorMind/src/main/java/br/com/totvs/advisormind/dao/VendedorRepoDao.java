package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.entities.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VendedorRepoDao extends JdbcSupport<VendedorRepo> {
  public VendedorRepoDao(JdbcTemplate jdbc) {
    super(
        jdbc,
        "VENDEDOR_REPO",
        "ID_VEND_REPO",
        (r, n) ->
            new VendedorRepo(
                r.getLong("ID_VEND_REPO"), r.getInt("TOTAL_VENDEDORES"), r.getString("FILTRO")));
  }

  public long criar(VendedorRepo v) {
    return inserir("TOTAL_VENDEDORES,FILTRO", v.totalVendedores(), v.filtro());
  }

  public int atualizar(VendedorRepo v) {
    return atualizarSql("TOTAL_VENDEDORES=?,FILTRO=?", v.totalVendedores(), v.filtro(), v.id());
  }

  public void sincronizar(long id) {
    jdbc.update(
        "UPDATE VENDEDOR_REPO SET TOTAL_VENDEDORES=(SELECT COUNT(*) FROM VENDEDOR WHERE"
            + " VENDEDOR_REPO_ID_VEND_REPO=?) WHERE ID_VEND_REPO=?",
        id,
        id);
  }
}
