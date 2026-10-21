package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import br.com.totvs.advisormind.conexoes.BancoConfig;
import br.com.totvs.advisormind.conexoes.ConexaoDataSource;
import br.com.totvs.advisormind.conexoes.ConexaoFactory;
import org.springframework.test.util.ReflectionTestUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

class OracleConfigurationTest {
  @Test
  void oracleProfileUsesClassConfigurationWithoutConnectingOrRunningDdl() throws Exception {
    var profile = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-oracle.properties"));
    var runner = new ApplicationContextRunner()
        .withUserConfiguration(BancoConfig.class)
        .withConfiguration(AutoConfigurations.of(
            DataSourceAutoConfiguration.class, SqlInitializationAutoConfiguration.class));
    for (String name : profile.stringPropertyNames()) {
      runner = runner.withPropertyValues(name + "=" + profile.getProperty(name));
    }
    runner.withPropertyValues(
        "DB_URL=jdbc:oracle:thin:@//example.invalid:1521/TEST",
        "DB_USER=test-user", "DB_PASSWORD=test-only")
        .run(context -> {
          assertNull(context.getStartupFailure());
          var properties = context.getBean(DataSourceProperties.class);
          assertNull(properties.getUrl());
          assertNull(properties.getUsername());
          assertNull(properties.getPassword());
          assertEquals("oracle.jdbc.driver.OracleDriver", properties.getDriverClassName());
          var source = context.getBean(HikariDataSource.class);
          assertInstanceOf(ConexaoDataSource.class, source.getDataSource());
          var actual = ReflectionTestUtils.getField(source.getDataSource(), "factory");
          assertInstanceOf(ConexaoFactory.class, actual);
          var expected = new ConexaoFactory();
          for (String field : new String[] {"URL", "USUARIO", "SENHA"}) {
            assertTrue(java.util.Objects.equals(
                ReflectionTestUtils.getField(expected, field),
                ReflectionTestUtils.getField(actual, field)),
                "Configuração Oracle deve vir da própria factory, sem imprimir credenciais");
          }
          assertEquals("never", context.getEnvironment().getProperty("spring.sql.init.mode"));
          assertEquals("false", context.getEnvironment().getProperty("spring.h2.console.enabled"));
          assertNull(source.getHikariPoolMXBean(), "Não deve iniciar conexões Oracle no teste");
        });
    assertNull(profile.getProperty("spring.datasource.url"));
    assertNull(profile.getProperty("spring.datasource.username"));
    assertNull(profile.getProperty("spring.datasource.password"));
  }
}
