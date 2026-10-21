package br.com.totvs.advisormind.repository;

import br.com.totvs.advisormind.dao.ReuniaoRepoDao;
import br.com.totvs.advisormind.entities.ReuniaoRepo;
import br.com.totvs.advisormind.util.JsonUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ReuniaoRepository {
  public void sincronizar(Connection conexao, long id) throws SQLException {
    ReuniaoRepoDao dao = new ReuniaoRepoDao(conexao);
    List<Long> ids = dao.reunioes(id);
    dao.atualizar(new ReuniaoRepo(id, JsonUtil.indice(ids), ids.size()));
  }
}
