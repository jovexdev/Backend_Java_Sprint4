package br.com.totvs.advisormind.repository;

import br.com.totvs.advisormind.dao.JdbcSupport;
import br.com.totvs.advisormind.entities.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReuniaoRepository extends JdbcSupport<Reuniao> {
  public ReuniaoRepository(JdbcTemplate jdbc) {
    super(
        jdbc,
        "REUNIAO",
        "ID_REUNIAO",
        (r, n) ->
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
