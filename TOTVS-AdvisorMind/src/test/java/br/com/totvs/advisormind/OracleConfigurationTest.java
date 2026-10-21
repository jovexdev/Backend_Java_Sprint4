package br.com.totvs.advisormind;

import static org.junit.jupiter.api.Assertions.*;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

class OracleConfigurationTest {
  @Test
  void oracleProfileResolvesExternalConfigurationWithoutConnectingOrRunningDdl() throws Exception {
    var profile = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-oracle.properties"));
    var runner = new ApplicationContextRunner()
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
          var source = context.getBean(HikariDataSource.class);
          assertEquals("jdbc:oracle:thin:@//example.invalid:1521/TEST", source.getJdbcUrl());
          assertEquals("test-user", source.getUsername());
          assertEquals("test-only", source.getPassword());
          assertEquals("oracle.jdbc.OracleDriver", source.getDriverClassName());
          assertEquals("never", context.getEnvironment().getProperty("spring.sql.init.mode"));
          assertEquals("false", context.getEnvironment().getProperty("spring.h2.console.enabled"));
          assertNull(source.getHikariPoolMXBean(), "Não deve iniciar conexões Oracle no teste");
        });
    assertEquals("${DB_URL}", profile.getProperty("spring.datasource.url"));
    assertEquals("${DB_USER}", profile.getProperty("spring.datasource.username"));
    assertEquals("${DB_PASSWORD}", profile.getProperty("spring.datasource.password"));
  }
}
