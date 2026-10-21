package br.com.totvs.advisormind;

import br.com.totvs.advisormind.dto.*;
import br.com.totvs.advisormind.entities.StatusReuniao;
import br.com.totvs.advisormind.service.*;
import java.time.*;
import java.util.*;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class Main {
  public static void main(String[] args) {
    try (var ctx =
            new SpringApplicationBuilder(AdvisorMindApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
        var sc = new Scanner(System.in)) {
      var vendedores = ctx.getBean(VendedorService.class);
      var clientes = ctx.getBean(ClienteService.class);
      var reunioes = ctx.getBean(ReuniaoService.class);
      var analises = ctx.getBean(AnaliseService.class);
      while (true) {
        System.out.println(
            "1 Reuniões | 2 Cadastrar reunião | 3 Estatísticas | 4 Cadastrar vendedor | 5"
                + " Vendedores | 6 Briefing | 7 Analytics | 8 VoC | 9 Atualizar vendedor | 10"
                + " Excluir vendedor | 11 Cliente | 12 Criar repositórios | 13 Clientes/repos | 0"
                + " Sair");
        String op = sc.nextLine();
        if (op.equals("0")) break;
        try {
          switch (op) {
            case "1" -> reunioes.listar().forEach(System.out::println);
            case "2" -> {
              System.out.println(
                  "clienteId, vendedorId, reuniaoRepoId, data ISO (yyyy-MM-ddTHH:mm:ss), status,"
                      + " transcrição: um por linha");
              long c = Long.parseLong(sc.nextLine()),
                  v = Long.parseLong(sc.nextLine()),
                  r = Long.parseLong(sc.nextLine());
              LocalDateTime date = LocalDateTime.parse(sc.nextLine());
              StatusReuniao status = StatusReuniao.valueOf(sc.nextLine());
              System.out.println(
                  reunioes.criar(new ReuniaoRequest(sc.nextLine(), date, status, c, v, r)));
            }
            case "3" -> ctx.getBean(RelatorioService.class).exibirEstatisticas(reunioes.listar());
            case "4", "9" -> {
              long id = 0;
              if (op.equals("9")) {
                System.out.println("ID:");
                id = Long.parseLong(sc.nextLine());
              }
              System.out.println("Nome, cargo, setor, totalReunioes, vendedorRepoId: um por linha");
              var r =
                  new VendedorRequest(
                      sc.nextLine(),
                      sc.nextLine(),
                      sc.nextLine(),
                      Integer.valueOf(sc.nextLine()),
                      Long.valueOf(sc.nextLine()));
              System.out.println(
                  op.equals("4") ? vendedores.criar(r) : vendedores.atualizar(id, r));
            }
            case "5" -> vendedores.listar().forEach(System.out::println);
            case "6", "7", "8" -> {
              System.out.println("ID da reunião:");
              long id = Long.parseLong(sc.nextLine());
              String tipo =
                  op.equals("6")
                      ? "smart-briefing"
                      : op.equals("7") ? "conversational-analytics" : "voc-tracker";
              System.out.println("Prévia completa, sem persistir; chamada ao provedor:");
              ConsoleFormatter.imprimir(tipo, analises.previa(id, tipo).resultado());
            }
            case "10" -> {
              System.out.println("ID:");
              vendedores.deletar(Long.parseLong(sc.nextLine()));
            }
            case "11" -> {
              System.out.println("Nome, empresa, nascimento yyyy-MM-dd: um por linha");
              System.out.println(
                  clientes.criar(
                      new ClienteRequest(
                          sc.nextLine(), sc.nextLine(), LocalDate.parse(sc.nextLine()))));
            }
            case "12" -> {
              System.out.println("Filtro/descrição do repositório de vendedores:");
              System.out.println(
                  ctx.getBean(VendedorRepoService.class)
                      .criar(new VendedorRepoRequest(sc.nextLine())));
              System.out.println(
                  ctx.getBean(ReuniaoRepoService.class).criar(new ReuniaoRepoRequest()));
            }
            case "13" -> {
              clientes.listar().forEach(System.out::println);
              ctx.getBean(VendedorRepoService.class).listar().forEach(System.out::println);
              ctx.getBean(ReuniaoRepoService.class).listar().forEach(System.out::println);
            }
            default -> System.out.println("Opção inválida");
          }
        } catch (br.com.totvs.advisormind.exception.ApiException e) {
          System.out.println(e.getMessage());
        } catch (Exception e) {
          System.out.println(
              "Operação não concluída. Confira dados e vínculos; nenhum detalhe interno será"
                  + " exibido.");
        }
      }
    }
  }
}
