package br.com.totvs.advisormind.conexoes;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DataSourceProperties.class)
public class BancoConfig {
  @Bean
  @ConfigurationProperties("spring.datasource.hikari")
  public HikariDataSource dataSource(DataSourceProperties properties) {
    ConexaoFactory factory = "org.h2.Driver".equals(properties.determineDriverClassName())
        ? new ConexaoH2Factory(
            properties.determineUrl(),
            properties.determineUsername(),
            properties.determinePassword())
        : new ConexaoFactory();
    var source = new HikariDataSource();
    source.setDataSource(new ConexaoDataSource(factory));
    return source;
  }
}
