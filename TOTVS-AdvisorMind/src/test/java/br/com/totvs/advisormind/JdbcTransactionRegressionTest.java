package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import br.com.totvs.advisormind.dao.*;
import br.com.totvs.advisormind.entities.*;
import com.zaxxer.hikari.HikariDataSource;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.support.TransactionTemplate;

class JdbcTransactionRegressionTest {
  HikariDataSource source;
  ConexaoFactory factory;
  TransactionTemplate tx;

  @BeforeEach
  void setup() throws Exception {
    source = new HikariDataSource();
    source.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=Oracle");
    source.setUsername("sa");
    source.setPassword("");
    // Uma conexão torna imediatamente visível qualquer aquisição fora da transação.
    source.setMaximumPoolSize(1);
    source.setConnectionTimeout(1000);
    try (var c = source.getConnection()) {
      ScriptUtils.executeSqlScript(c, new ClassPathResource("schema-local.sql"));
    }
    factory = new ConexaoFactory(source);
    tx = new TransactionTemplate(new DataSourceTransactionManager(source));
  }

  @AfterEach
  void close() {
    source.close();
  }

  @Test
  void releaseKeepsTransactionalConnectionUntilSpringCompletes() {
    tx.executeWithoutResult(status -> {
      var first = factory.conexao();
      factory.liberar(first);
      var second = factory.conexao();
      assertSame(first, second);
      assertDoesNotThrow(() -> assertFalse(second.isClosed()));
      factory.liberar(second);
      new VendedorRepoDao(factory).criar(new VendedorRepo(0, 0, null));
    });
    assertEquals(0, source.getHikariPoolMXBean().getActiveConnections());
    assertEquals(1, new VendedorRepoDao(factory).listar().size());
  }

  private void createAllSixTables() {
    var vrDao = new VendedorRepoDao(factory);
    var rrDao = new ReuniaoRepoDao(factory);
    long vr = vrDao.criar(new VendedorRepo(0, 0, null));
    long rr = rrDao.criar(new ReuniaoRepo(0, "{\"reunioes\":[]}", 0));
    long c = new ClienteDao(factory).criar(new Cliente(0, "João", "ME", LocalDate.of(1990, 1, 1)));
    long v = new VendedorDao(factory).criar(new Vendedor(0, "Ana", "Consultor", "ERP", 10, vr));
    vrDao.sincronizar(vr);
    var when = LocalDateTime.of(2026, 9, 1, 10, 17, 23);
    var meetingDao = new ReuniaoDao(factory);
    long r = meetingDao.criar(new Reuniao(0, "Transcrição", when, StatusReuniao.AGENDADA, c, v, rr));
    assertEquals(when, meetingDao.bloquear(r).getData());
    assertEquals(java.util.List.of(r), rrDao.reunioes(rr));
    rrDao.atualizar(new ReuniaoRepo(rr, "{\"reunioes\":[" + r + "]}", 1));
    var aDao = new AnaliseIADao(factory);
    aDao.criar(new AnaliseIA(0, 1, "Fixture, sem chamada de IA", "fixture", when, 5, r));
    assertTrue(aDao.existeTipo(r, 1));
    assertEquals(1, aDao.porReuniao(r).size());
  }

  @Test
  void allSixDaosAndDerivedDataRollbackTogether() throws Exception {
    assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(status -> {
      createAllSixTables();
      throw new IllegalStateException("Falha após gravação das seis tabelas");
    }));
    try (var c = source.getConnection()) {
      for (String table : new String[] {
          "VENDEDOR_REPO", "REUNIAO_REPO", "CLIENTE", "VENDEDOR", "REUNIAO", "ANALISE_IA"}) {
        try (var ps = c.prepareStatement("SELECT COUNT(*) FROM " + table);
            var rows = ps.executeQuery()) {
          assertTrue(rows.next());
          assertEquals(0, rows.getInt(1), table);
        }
      }
    }
    tx.executeWithoutResult(status -> createAllSixTables());
    assertEquals(1, new VendedorRepoDao(factory).listar().getFirst().totalVendedores());
    assertEquals(1, new ReuniaoRepoDao(factory).listar().getFirst().totalReunioes());
    assertEquals(1, new AnaliseIADao(factory).listar().size());
    assertEquals(0, source.getHikariPoolMXBean().getActiveConnections());
  }

  @Test
  void repeatedSuccessAndConstraintFailureDoNotExhaustPool() {
    var dao = new ClienteDao(factory);
    var client = new Cliente(0, "D'Ávila'); --", "Empresa", LocalDate.of(1990, 1, 1));
    long id = dao.criar(client);
    for (int i = 0; i < 10; i++) {
      assertThrows(DataIntegrityViolationException.class, () -> dao.criar(client));
      assertEquals(client.getNome(), dao.buscar(id).getNome());
      assertEquals(0, source.getHikariPoolMXBean().getActiveConnections());
    }
    assertEquals(1, dao.excluir(id));
    assertNull(dao.buscar(id));
  }
}
