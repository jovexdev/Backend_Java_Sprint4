package br.com.totvs.advisormind.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.totvs.advisormind")
public class AdvisorMindApplication {
  public static void main(String[] args) {
    SpringApplication.run(AdvisorMindApplication.class, args);
  }
}
