package br.com.totvs.advisormind.main;

import br.com.totvs.advisormind.dto.ClienteRequest;
import br.com.totvs.advisormind.dto.ReuniaoRepoRequest;
import br.com.totvs.advisormind.dto.ReuniaoRequest;
import br.com.totvs.advisormind.dto.VendedorRepoRequest;
import br.com.totvs.advisormind.dto.VendedorRequest;
import br.com.totvs.advisormind.entities.Cliente;
import br.com.totvs.advisormind.entities.Reuniao;
import br.com.totvs.advisormind.entities.ReuniaoRepo;
import br.com.totvs.advisormind.entities.StatusReuniao;
import br.com.totvs.advisormind.entities.Vendedor;
import br.com.totvs.advisormind.entities.VendedorRepo;
import br.com.totvs.advisormind.service.AnaliseService;
import br.com.totvs.advisormind.service.ClienteService;
import br.com.totvs.advisormind.service.RelatorioService;
import br.com.totvs.advisormind.service.ReuniaoRepoService;
import br.com.totvs.advisormind.service.ReuniaoService;
import br.com.totvs.advisormind.service.VendedorRepoService;
import br.com.totvs.advisormind.service.VendedorService;
import br.com.totvs.advisormind.util.ConsoleFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class Main {
  public static void main(String[] args) {
    try (ConfigurableApplicationContext ctx =
            new SpringApplicationBuilder(AdvisorMindApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
        Scanner sc = new Scanner(System.in)) {
      VendedorService vendedores = ctx.getBean(VendedorService.class);
      ClienteService clientes = ctx.getBean(ClienteService.class);
      ReuniaoService reunioes = ctx.getBean(ReuniaoService.class);
      AnaliseService analises = ctx.getBean(AnaliseService.class);
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
            case "1":
              for (Reuniao reuniao : reunioes.listar()) System.out.println(reuniao);
              break;
            case "2": {
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
              break;
            }
            case "3":
              ctx.getBean(RelatorioService.class).exibirEstatisticas(reunioes.listar());
              break;
            case "4":
            case "9": {
              long id = 0;
              if (op.equals("9")) {
                System.out.println("ID:");
                id = Long.parseLong(sc.nextLine());
              }
              System.out.println("Nome, cargo, setor, totalReunioes, vendedorRepoId: um por linha");
              VendedorRequest r =
                  new VendedorRequest(
                      sc.nextLine(),
                      sc.nextLine(),
                      sc.nextLine(),
                      Integer.valueOf(sc.nextLine()),
                      Long.valueOf(sc.nextLine()));
              System.out.println(
                  op.equals("4") ? vendedores.criar(r) : vendedores.atualizar(id, r));
              break;
            }
            case "5":
              for (Vendedor vendedor : vendedores.listar()) System.out.println(vendedor);
              break;
            case "6":
            case "7":
            case "8": {
              System.out.println("ID da reunião:");
              long id = Long.parseLong(sc.nextLine());
              String tipo =
                  op.equals("6")
                      ? "smart-briefing"
                      : op.equals("7") ? "conversational-analytics" : "voc-tracker";
              System.out.println("Prévia completa, sem persistir; chamada ao provedor:");
              ConsoleFormatter.imprimir(tipo, analises.previa(id, tipo).getResultado());
              break;
            }
            case "10": {
              System.out.println("ID:");
              vendedores.deletar(Long.parseLong(sc.nextLine()));
              break;
            }
            case "11": {
              System.out.println("Nome, empresa, nascimento yyyy-MM-dd: um por linha");
              System.out.println(
                  clientes.criar(
                      new ClienteRequest(
                          sc.nextLine(), sc.nextLine(), LocalDate.parse(sc.nextLine()))));
              break;
            }
            case "12": {
              System.out.println("Filtro/descrição do repositório de vendedores:");
              System.out.println(
                  ctx.getBean(VendedorRepoService.class)
                      .criar(new VendedorRepoRequest(sc.nextLine())));
              System.out.println(
                  ctx.getBean(ReuniaoRepoService.class).criar(new ReuniaoRepoRequest()));
              break;
            }
            case "13": {
              for (Cliente cliente : clientes.listar()) System.out.println(cliente);
              for (VendedorRepo repo : ctx.getBean(VendedorRepoService.class).listar()) System.out.println(repo);
              for (ReuniaoRepo repo : ctx.getBean(ReuniaoRepoService.class).listar()) System.out.println(repo);
              break;
            }
            default: System.out.println("Opção inválida");
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
