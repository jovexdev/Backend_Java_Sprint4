package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.*;
import br.com.totvs.advisormind.entities.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Somente schema de teste autorizado: sem DDL e com rollback obrigatório. */
@EnabledIfEnvironmentVariable(named = "RUN_ORACLE_TRANSACTIONAL", matches = "true")
class OracleDaoIntegrationTest {
  @Test
  void sixDaosUseOneConnectionAndRollback() throws Exception {
    ConexaoFactory factory = new ConexaoFactory(System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
    try (Connection conexao = factory.conexao()) {
      conexao.setAutoCommit(false);
      long clienteId = 0;
      try {
        VendedorRepoDao vrDao = new VendedorRepoDao(conexao);
        ReuniaoRepoDao rrDao = new ReuniaoRepoDao(conexao);
        ClienteDao cDao = new ClienteDao(conexao);
        VendedorDao vDao = new VendedorDao(conexao);
        ReuniaoDao rDao = new ReuniaoDao(conexao);
        AnaliseIADao aDao = new AnaliseIADao(conexao);
        String marca = "TEST_DAO_" + UUID.randomUUID();
        long vr = vrDao.criar(new VendedorRepo(0, 0, marca));
        long rr = rrDao.criar(new ReuniaoRepo(0, "{\"reunioes\":[]}", 0));
        clienteId = cDao.criar(new Cliente(0, marca, "Test only", LocalDate.of(1990, 3, 15)));
        long v = vDao.criar(new Vendedor(0, marca, "Consultor", "Test", 0, vr));
        vrDao.sincronizar(vr);
        assertEquals(1, vrDao.buscar(vr).getTotalVendedores());
        LocalDateTime data = LocalDateTime.of(2026, 9, 1, 10, 17, 23);
        long r = rDao.criar(new Reuniao(0, marca, data, StatusReuniao.AGENDADA, clienteId, v, rr));
        assertEquals(data, rDao.bloquear(r).getData());
        String clob = "{\"fixture\":\"" + "a".repeat(6000) + "\"}";
        rrDao.atualizar(new ReuniaoRepo(rr, clob, 1));
        assertEquals(clob, rrDao.buscar(rr).getArquivoJson());
        long a = aDao.criar(new AnaliseIA(0, 1, "TEST FIXTURE", "TEST_FIXTURE", data, 5, r));
        assertTrue(aDao.existeTipo(r, 1));
        assertEquals(a, aDao.porReuniao(r).get(0).getId());
        assertThrows(SQLException.class, () -> vDao.excluir(v));
        assertEquals(1, aDao.excluir(a));
        assertNull(aDao.buscar(a));
      } finally {
        conexao.rollback();
      }
      assertTrue(clienteId > 0);
      assertNull(new ClienteDao(conexao).buscar(clienteId));
    }
  }
}
