package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.entities.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VendedorDao extends JdbcSupport<Vendedor> {
  public VendedorDao(JdbcTemplate jdbc) {
    super(
        jdbc,
        "VENDEDOR",
        "ID_VENDEDOR",
        (r, n) ->
            new Vendedor(
                r.getLong("ID_VENDEDOR"),
                r.getString("NOME"),
                r.getString("CARGO"),
                r.getString("SETOR"),
                r.getInt("TOTAL_REUNIOES"),
                r.getLong("VENDEDOR_REPO_ID_VEND_REPO")));
  }

  public long criar(Vendedor v) {
    return inserir(
        "NOME,CARGO,SETOR,TOTAL_REUNIOES,VENDEDOR_REPO_ID_VEND_REPO",
        v.getNome(),
        v.getCargo(),
        v.getSetor(),
        v.getTotalReunioes(),
        v.getVendedorRepoId());
  }

  public int atualizar(Vendedor v) {
    return atualizarSql(
        "NOME=?,CARGO=?,SETOR=?,TOTAL_REUNIOES=?,VENDEDOR_REPO_ID_VEND_REPO=?",
        v.getNome(),
        v.getCargo(),
        v.getSetor(),
        v.getTotalReunioes(),
        v.getVendedorRepoId(),
        v.getId());
  }
}
