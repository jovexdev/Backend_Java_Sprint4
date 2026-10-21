package br.com.totvs.advisormind.dao;

import br.com.totvs.advisormind.entities.*;
import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import org.springframework.stereotype.Repository;

@Repository
public class AnaliseIADao extends JdbcSupport<AnaliseIA> {
  public AnaliseIADao(ConexaoFactory conexoes) {
    super(
        conexoes,
        "ANALISE_IA",
        "ID_ANALISE",
        r ->
            new AnaliseIA(
                r.getLong("ID_ANALISE"),
                r.getInt("TIPO"),
                r.getString("RESULTADO"),
                r.getString("MODELO_IA"),
                r.getTimestamp("DATAHORA_EXECUCAO").toLocalDateTime(),
                r.getInt("TOKENS_UTILIZADOS"),
                r.getLong("REUNIAO_ID_REUNIAO")));
  }

  public long criar(AnaliseIA v) {
    return inserir(
        "TIPO,RESULTADO,MODELO_IA,DATAHORA_EXECUCAO,TOKENS_UTILIZADOS,REUNIAO_ID_REUNIAO",
        v.tipo(),
        v.resultado(),
        v.modeloIa(),
        java.sql.Timestamp.valueOf(v.datahoraExecucao()),
        v.tokensUtilizados(),
        v.reuniaoId());
  }

  public int atualizar(AnaliseIA v) {
    return atualizarSql(
        "TIPO=?,RESULTADO=?,MODELO_IA=?,DATAHORA_EXECUCAO=?,TOKENS_UTILIZADOS=?,REUNIAO_ID_REUNIAO=?",
        v.tipo(),
        v.resultado(),
        v.modeloIa(),
        java.sql.Timestamp.valueOf(v.datahoraExecucao()),
        v.tokensUtilizados(),
        v.reuniaoId(),
        v.id());
  }

  public java.util.List<AnaliseIA> porReuniao(long id) {
    return consultar(
        "SELECT * FROM ANALISE_IA WHERE REUNIAO_ID_REUNIAO=? ORDER BY TIPO",
        r ->
            new AnaliseIA(
                r.getLong("ID_ANALISE"),
                r.getInt("TIPO"),
                r.getString("RESULTADO"),
                r.getString("MODELO_IA"),
                r.getTimestamp("DATAHORA_EXECUCAO").toLocalDateTime(),
                r.getInt("TOKENS_UTILIZADOS"),
                r.getLong("REUNIAO_ID_REUNIAO")),
        id);
  }

  public boolean existeTipo(long reuniao, int tipo) {
    return consultar(
            "SELECT COUNT(*) FROM ANALISE_IA WHERE REUNIAO_ID_REUNIAO=? AND TIPO=?",
            r -> r.getLong(1),
            reuniao,
            tipo).getFirst()
        > 0;
  }
}
