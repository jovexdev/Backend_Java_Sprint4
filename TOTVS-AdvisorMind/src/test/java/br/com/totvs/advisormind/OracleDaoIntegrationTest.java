package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.*;
import br.com.totvs.advisormind.entities.*;
import com.zaxxer.hikari.HikariDataSource;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Opt-in: seis DAOs reais, sem DDL, somente em schema de teste autorizado; rollback obrigatório. */
@EnabledIfEnvironmentVariable(named = "RUN_ORACLE_TRANSACTIONAL", matches = "true")
class OracleDaoIntegrationTest {
  @Test
  void realDaosShareOracleTransactionAndRollback() {
    try (var source = new HikariDataSource()) {
      source.setJdbcUrl(System.getenv("DB_URL"));
      source.setUsername(System.getenv("DB_USER"));
      source.setPassword(System.getenv("DB_PASSWORD"));
      source.setDriverClassName("oracle.jdbc.OracleDriver");
      source.setMaximumPoolSize(1);
      source.setConnectionTimeout(5000);
      var factory = new ConexaoFactory(source);
      var vrDao = new VendedorRepoDao(factory);
      var rrDao = new ReuniaoRepoDao(factory);
      var cDao = new ClienteDao(factory);
      var vDao = new VendedorDao(factory);
      var rDao = new ReuniaoDao(factory);
      var aDao = new AnaliseIADao(factory);
      var tx = new TransactionTemplate(new DataSourceTransactionManager(source));
      var marker = "TEST_DAO_" + UUID.randomUUID();
      var clientId = new AtomicLong();
      tx.executeWithoutResult(status -> {
        try {
          long vr = vrDao.criar(new VendedorRepo(0, 0, marker));
          long rr = rrDao.criar(new ReuniaoRepo(0, "{\"reunioes\":[]}", 0));
          var birthday = LocalDate.of(1990, 3, 15);
          long c = cDao.criar(new Cliente(0, marker, "Test only", birthday));
          clientId.set(c);
          long v = vDao.criar(new Vendedor(0, marker, "Consultor", "Test", 0, vr));
          vrDao.sincronizar(vr);
          assertEquals(1, vrDao.buscar(vr).totalVendedores());
          assertEquals(birthday, cDao.buscar(c).getDataNascimento());
          var when = LocalDateTime.of(2026, 9, 1, 10, 17, 23);
          long r = rDao.criar(new Reuniao(0, marker, when, StatusReuniao.AGENDADA, c, v, rr));
          assertEquals(when, rDao.bloquear(r).getData());
          assertEquals(java.util.List.of(r), rrDao.reunioes(rr));
          String clob = "{\"fixture\":\"" + "a".repeat(6000) + "\"}";
          rrDao.atualizar(new ReuniaoRepo(rr, clob, 1));
          assertEquals(clob, rrDao.buscar(rr).arquivoJson());
          long a = aDao.criar(new AnaliseIA(0, 1, "TEST FIXTURE", "TEST_FIXTURE", when, 5, r));
          assertTrue(aDao.existeTipo(r, 1));
          assertEquals(a, aDao.porReuniao(r).getFirst().id());
          assertThrows(DataIntegrityViolationException.class, () -> vDao.excluir(v));
          assertEquals(1, aDao.excluir(a));
          assertNull(aDao.buscar(a));
          // Os outros cinco registros ficam presentes até o rollback.
        } finally {
          status.setRollbackOnly();
        }
      });
      assertTrue(clientId.get() > 0);
      assertNull(cDao.buscar(clientId.get()));
    }
  }
}
