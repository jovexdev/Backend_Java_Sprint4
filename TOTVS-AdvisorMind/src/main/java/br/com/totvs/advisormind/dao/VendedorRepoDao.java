package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.entities.*;
import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import org.springframework.stereotype.Repository;

@Repository
public class VendedorRepoDao extends JdbcSupport<VendedorRepo> {
  public VendedorRepoDao(ConexaoFactory conexoes) {
    super(
        conexoes,
        "VENDEDOR_REPO",
        "ID_VEND_REPO",
        r ->
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
    executarUpdate(
        "UPDATE VENDEDOR_REPO SET TOTAL_VENDEDORES=(SELECT COUNT(*) FROM VENDEDOR WHERE"
            + " VENDEDOR_REPO_ID_VEND_REPO=?) WHERE ID_VEND_REPO=?",
        id,
        id);
  }
}
