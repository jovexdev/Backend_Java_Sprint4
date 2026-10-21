package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import br.com.totvs.advisormind.conexoes.BancoConfig;
import br.com.totvs.advisormind.conexoes.ConexaoDataSource;
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
  void oracleProfileUsesFactoryWithoutConnectingOrRunningDdl() throws Exception {
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
          assertEquals("jdbc:oracle:thin:@//example.invalid:1521/TEST", properties.getUrl());
          assertEquals("test-user", properties.getUsername());
          assertEquals("test-only", properties.getPassword());
          assertEquals("oracle.jdbc.driver.OracleDriver", properties.getDriverClassName());
          var source = context.getBean(HikariDataSource.class);
          assertInstanceOf(ConexaoDataSource.class, source.getDataSource());
          assertEquals("never", context.getEnvironment().getProperty("spring.sql.init.mode"));
          assertEquals("false", context.getEnvironment().getProperty("spring.h2.console.enabled"));
          assertNull(source.getHikariPoolMXBean(), "Não deve iniciar conexões Oracle no teste");
        });
    assertEquals("${DB_URL:jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl}", profile.getProperty("spring.datasource.url"));
    assertEquals("${DB_USER}", profile.getProperty("spring.datasource.username"));
    assertEquals("${DB_PASSWORD}", profile.getProperty("spring.datasource.password"));
  }
}
