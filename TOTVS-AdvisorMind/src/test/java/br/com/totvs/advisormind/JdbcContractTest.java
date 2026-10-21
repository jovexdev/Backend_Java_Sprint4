package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

import br.com.totvs.advisormind.dao.*;
import br.com.totvs.advisormind.entities.*;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.*;
import org.springframework.jdbc.UncategorizedSQLException;

/** Contratos do driver Oracle simulados; não prova conectividade ou comportamento do servidor. */
class JdbcContractTest {
  DataSource source;
  Connection connection;
  PreparedStatement statement;
  ResultSet result;

  @BeforeEach
  void setup() throws Exception {
    source = mock(DataSource.class);
    connection = mock(Connection.class);
    statement = mock(PreparedStatement.class);
    result = mock(ResultSet.class);
    when(source.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(connection.prepareStatement(anyString(), any(String[].class))).thenReturn(statement);
    when(statement.executeUpdate()).thenReturn(1);
    when(statement.getGeneratedKeys()).thenReturn(result);
    when(statement.executeQuery()).thenReturn(result);
    when(result.next()).thenReturn(true, false);
    when(result.getBigDecimal(1)).thenReturn(new BigDecimal("42"));
  }

  @Test
  void namedOracleIdentityAndParameterizedStrings() throws Exception {
    String input = "x'); DELETE FROM CLIENTE; --";
    assertEquals(42, new VendedorRepoDao(source).criar(new VendedorRepo(0, 0, input)));
    verify(connection).prepareStatement(
        eq("INSERT INTO VENDEDOR_REPO (TOTAL_VENDEDORES,FILTRO) VALUES (?,?)"),
        aryEq(new String[] {"ID_VEND_REPO"}));
    verify(statement).setInt(1, 0);
    verify(statement).setString(2, input);
    verify(result).close();
    verify(statement).close();
    verify(connection).close();
    verify(connection, never()).commit();
  }

  @Test
  void nullableFilterUsesSqlNull() throws Exception {
    new VendedorRepoDao(source).criar(new VendedorRepo(0, 0, null));
    verify(statement).setNull(2, Types.VARCHAR);
  }

  @Test
  void datesKeepBirthdayAndMeetingTime() throws Exception {
    when(result.next()).thenReturn(true);
    var birthday = LocalDate.of(1990, 3, 15);
    new ClienteDao(source).criar(new Cliente(0, "João", "ME", birthday));
    verify(statement).setDate(3, java.sql.Date.valueOf(birthday));
    var time = LocalDateTime.of(2026, 9, 1, 10, 17, 23);
    new ReuniaoDao(source).criar(new Reuniao(0, "Texto", time, StatusReuniao.AGENDADA, 5, 6, 7));
    verify(statement).setTimestamp(2, Timestamp.valueOf(time));
    verify(statement).setLong(4, 5L);
    verify(statement).setLong(5, 6L);
    verify(statement).setLong(6, 7L);
  }

  @Test
  void clobUsesCharacterStreamWithoutTruncation() throws Exception {
    String json = "{\"texto\":\"" + "ação".repeat(2000) + "\"}";
    new ReuniaoRepoDao(source).criar(new ReuniaoRepo(0, json, 0));
    var reader = ArgumentCaptor.forClass(Reader.class);
    verify(statement).setCharacterStream(eq(1), reader.capture());
    var text = new java.io.StringWriter();
    reader.getValue().transferTo(text);
    assertEquals(json, text.toString());
  }

  @Test
  void oracleConstraintErrorsStayConflictsAndReleaseResources() throws Exception {
    var dao = new VendedorRepoDao(source);
    for (int code : new int[] {1, 2291, 2292}) {
      doThrow(new SQLIntegrityConstraintViolationException("Oracle constraint", "23000", code))
          .when(statement).executeUpdate();
      assertThrows(DataIntegrityViolationException.class, () -> dao.excluir(42));
    }
    verify(statement, times(3)).close();
    verify(connection, times(3)).close();
  }

  @Test
  void mapperFailureClosesResultSetStatementAndConnection() throws Exception {
    when(result.getLong("ID_VEND_REPO")).thenThrow(new SQLException("mapping failure"));
    assertThrows(UncategorizedSQLException.class, () -> new VendedorRepoDao(source).listar());
    verify(result).close();
    verify(statement).close();
    verify(connection).close();
  }

  @Test
  void prepareFailureAlsoReleasesConnection() throws Exception {
    when(connection.prepareStatement(anyString())).thenThrow(new SQLException("prepare failure"));
    assertThrows(UncategorizedSQLException.class, () -> new VendedorRepoDao(source).listar());
    verify(connection).close();
  }

  @Test
  void missingOrOverflowingIdentityCannotBecomeAnIncorrectId() throws Exception {
    var dao = new VendedorRepoDao(source);
    when(result.next()).thenReturn(false);
    assertThrows(DataRetrievalFailureException.class, () -> dao.criar(new VendedorRepo(0, 0, null)));
    when(result.next()).thenReturn(true);
    when(result.getBigDecimal(1)).thenReturn(new BigDecimal("9223372036854775808"));
    assertThrows(ArithmeticException.class, () -> dao.criar(new VendedorRepo(0, 0, null)));
    verify(result, times(2)).close();
    verify(statement, times(2)).close();
    verify(connection, times(2)).close();
  }
}
