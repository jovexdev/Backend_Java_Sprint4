package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.entities.*;
import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ReuniaoRepoDao extends JdbcSupport<ReuniaoRepo> {
  public ReuniaoRepoDao(ConexaoFactory conexoes) {
    super(
        conexoes,
        "REUNIAO_REPO",
        "ID_REUN_REPO",
        r ->
            new ReuniaoRepo(
                r.getLong("ID_REUN_REPO"),
                r.getString("ARQUIVO_JSON"),
                r.getInt("TOTAL_REUNIOES")));
  }

  public long criar(ReuniaoRepo v) {
    return inserir(
        "ARQUIVO_JSON,TOTAL_REUNIOES",
        new java.io.StringReader(v.arquivoJson()),
        v.totalReunioes());
  }

  public int atualizar(ReuniaoRepo v) {
    return atualizarSql(
        "ARQUIVO_JSON=?,TOTAL_REUNIOES=?",
        new java.io.StringReader(v.arquivoJson()),
        v.totalReunioes(),
        v.id());
  }

  public java.util.List<Long> reunioes(long id) {
    return consultar(
        "SELECT ID_REUNIAO FROM REUNIAO WHERE REUNIAO_REPO_ID_REUN_REPO=? ORDER BY ID_REUNIAO",
        r -> r.getLong("ID_REUNIAO"),
        id);
  }
}
