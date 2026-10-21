package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.totvs.advisormind.conexoes.*;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.jupiter.api.*;

class ConexaoFactoryTest {
  static final String URL = "jdbc:conexao-factory:test";
  CapturingDriver driver;

  @BeforeEach
  void registerDriver() throws Exception {
    driver = new CapturingDriver();
    DriverManager.registerDriver(driver);
  }

  @AfterEach
  void deregisterDriver() throws Exception {
    DriverManager.deregisterDriver(driver);
  }

  @Test
  void oracleFactoryLoadsDriverAndOpensWithUrlUserAndPassword() throws Exception {
    var factory = new ConexaoFactory(URL, "fixture-user", "fixture-password");
    try (Connection connection = factory.conexao()) {
      assertNotNull(connection);
      assertEquals(URL, driver.url);
      assertEquals("fixture-user", driver.properties.getProperty("user"));
      assertEquals("fixture-password", driver.properties.getProperty("password"));
      assertEquals(1, driver.calls);
    }
  }

  @Test
  void eachPhysicalConnectionGoesThroughFactory() throws Exception {
    var source = new ConexaoDataSource(new ConexaoFactory(URL, "fixture-user", "fixture-password"));
    try (var first = source.getConnection(); var second = source.getConnection()) {
      assertNotSame(first, second);
      assertEquals(2, driver.calls);
    }
  }

  @Test
  void oracleSqlExceptionIsPropagated() {
    var failure = new SQLException("fixture failure", "08001");
    driver.failure = failure;
    assertSame(failure, assertThrows(SQLException.class,
        () -> new ConexaoFactory(URL, "fixture-user", "fixture-password").conexao()));
  }

  @Test
  void adapterReportsMissingDriverWithoutPrintingCredentials() throws Exception {
    var factory = mock(ConexaoFactory.class);
    var cause = new ClassNotFoundException("missing driver");
    when(factory.conexao()).thenThrow(cause);
    var error = assertThrows(SQLException.class, () -> new ConexaoDataSource(factory).getConnection());
    assertSame(cause, error.getCause());
    assertEquals("Driver JDBC não disponível.", error.getMessage());
  }

  @Test
  void h2LocalFactoryUsesIndependentRealJdbcConnections() throws Exception {
    var factory = new ConexaoH2Factory("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=Oracle", "sa", "");
    try (var first = factory.conexao(); var second = factory.conexao()) {
      assertNotSame(first, second);
      assertTrue(first.getAutoCommit());
      try (var statement = second.prepareStatement("SELECT 1 FROM DUAL");
          var rows = statement.executeQuery()) {
        assertTrue(rows.next());
        assertEquals(1, rows.getInt(1));
      }
    }
  }

  private static class CapturingDriver implements Driver {
    String url;
    Properties properties;
    int calls;
    SQLException failure;

    @Override
    public Connection connect(String url, Properties properties) throws SQLException {
      if (!acceptsURL(url)) return null;
      if (failure != null) throw failure;
      this.url = url;
      this.properties = properties;
      calls++;
      return mock(Connection.class);
    }

    @Override
    public boolean acceptsURL(String url) { return URL.equals(url); }
    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
    @Override
    public int getMajorVersion() { return 1; }
    @Override
    public int getMinorVersion() { return 0; }
    @Override
    public boolean jdbcCompliant() { return false; }
    @Override
    public Logger getParentLogger() { return Logger.getGlobal(); }
  }
}
