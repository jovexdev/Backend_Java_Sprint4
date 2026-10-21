package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.entities.*;
import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ReuniaoDao extends JdbcSupport<Reuniao> {
  public ReuniaoDao(ConexaoFactory conexoes) {
    super(
        conexoes,
        "REUNIAO",
        "ID_REUNIAO",
        r ->
            new Reuniao(
                r.getLong("ID_REUNIAO"),
                r.getString("TRANSCRICAO"),
                r.getTimestamp("DATA").toLocalDateTime(),
                StatusReuniao.valueOf(r.getString("STATUS")),
                r.getLong("CLIENTE_ID_CLIENTE"),
                r.getLong("VENDEDOR_ID_VENDEDOR"),
                r.getLong("REUNIAO_REPO_ID_REUN_REPO")));
  }

  public long criar(Reuniao v) {
    return inserir(
        "TRANSCRICAO,DATA,STATUS,CLIENTE_ID_CLIENTE,VENDEDOR_ID_VENDEDOR,REUNIAO_REPO_ID_REUN_REPO",
        v.getTranscricao(),
        java.sql.Timestamp.valueOf(v.getData()),
        v.getStatus().name(),
        v.getClienteId(),
        v.getVendedorId(),
        v.getReuniaoRepoId());
  }

  public int atualizar(Reuniao v) {
    return atualizarSql(
        "TRANSCRICAO=?,DATA=?,STATUS=?,CLIENTE_ID_CLIENTE=?,VENDEDOR_ID_VENDEDOR=?,REUNIAO_REPO_ID_REUN_REPO=?",
        v.getTranscricao(),
        java.sql.Timestamp.valueOf(v.getData()),
        v.getStatus().name(),
        v.getClienteId(),
        v.getVendedorId(),
        v.getReuniaoRepoId(),
        v.getId());
  }
}
