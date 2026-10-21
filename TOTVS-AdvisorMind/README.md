# TOTVS AdvisorMind — Sprint 4

O AdvisorMind auxilia vendedores na análise de reuniões comerciais. O sistema cadastra clientes, vendedores e reuniões e transforma transcrições em informações para a equipe comercial da TOTVS.

- **SmartBriefing AI:** resumo executivo, decisões e próximos passos.
- **Conversational Analytics:** análise da conversa, sentimento e participação estimada.
- **VoC Tracker:** reclamações, elogios, sugestões e oportunidades comerciais.

O backend usa Java 21, Spring Boot para a API REST e Oracle FIAP com JDBC direto. A organização e a persistência seguem o exemplo `SpringbootAluno-main.zip` fornecido pelo professor: classes comuns, construtores, getters/setters, Service/BO e SQL com `Connection`, `PreparedStatement` e `ResultSet`.

## Organização

```text
src/
└── main/
    ├── java/
    │   └── br/com/totvs/advisormind/
    │       ├── main/
    │       ├── conexoes/
    │       ├── controller/
    │       ├── dao/
    │       ├── dto/
    │       ├── entities/
    │       ├── exception/
    │       ├── repository/
    │       ├── service/
    │       └── util/
    └── resources/
        └── application.properties
```

O papel de cada pacote está explicado em [docs/ESTRUTURA.md](docs/ESTRUTURA.md).

## Abrir e executar no IntelliJ

1. Abra o `pom.xml` desta pasta como projeto Maven e selecione JDK 21.
2. Em `src/main/java/br/com/totvs/advisormind/conexoes/ConexaoFactory.java`, preencha o construtor vazio com sua conta Oracle **somente na sua cópia local**:

```java
public ConexaoFactory() {
    this("jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl",
         "Seu usuario Oracle", "Sua Senha Oracle");
}
```

3. Execute `main.TesteConexao` para conferir a conexão.
4. Execute `main.AdvisorMindApplication` para iniciar a API em `http://127.0.0.1:8080`.
5. Para usar o menu de console, execute `main.Main`.

As classes de execução agora ficam dentro do pacote **main**. Se houver uma configuração de execução antiga no IntelliJ, selecione novamente a classe no pacote novo. A conexão Oracle é a padrão; não é necessário selecionar perfil de banco nem cadastrar DB_USER/DB_PASSWORD para a aplicação.

O repositório contém apenas campos de exemplo. Não faça commit da classe com sua senha real. O `.gitignore` protege configurações do IntelliJ, arquivos locais, logs e resultados de compilação.

## Banco de dados

O schema Oracle deve conter as seis tabelas: `CLIENTE`, `VENDEDOR`, `REUNIAO`, `ANALISE_IA`, `VENDEDOR_REPO` e `REUNIAO_REPO`.

- `sql/oracle-create.sql`: DDL das seis tabelas. Revise e use somente se ainda não existirem no schema. A aplicação não executa DDL automaticamente.
- `sql/recebido.sql`: material SQL originalmente fornecido, com carga e consultas; contém repetições e não deve ser executado indiscriminadamente sobre tabelas com dados.
- `sql/dql-academico.sql`: consultas acadêmicas preservadas.

H2 e os perfis de banco local foram retirados desta versão. O aplicativo usa a conta Oracle configurada em `ConexaoFactory`.

Os DAOs contêm SQL completo e parâmetros explícitos. O Service valida os dados e, quando altera várias tabelas, passa a mesma conexão aos DAOs, usa `setAutoCommit(false)` e conclui com `commit()` ou `rollback()`. Não há pool ou gerenciador de transações adicional.

Os relacionamentos, identidades e restrições do schema foram preservados. Exclusão de registros com dependentes retorna conflito. O índice JSON e os contadores dos agrupamentos são atualizados na mesma transação dos cadastros. O JSON é um índice no banco, não um arquivo local de reuniões.

## Usar a API

Cadastre nesta ordem: agrupamento de vendedores, clientes, agrupamento de reuniões, vendedores e reuniões. Use os IDs retornados pelo Oracle.

As rotas e os corpos estão em [docs/API.md](docs/API.md), [docs/requests.http](docs/requests.http) e na [coleção Postman](docs/AdvisorMind-FINAL.postman_collection.json).

As regras comerciais foram mantidas: perfil do cliente, senioridade, simulação de comissão, temperatura da reunião, estimativa de duração e estatísticas. `VENDEDOR.TOTAL_REUNIOES` continua sendo o contador histórico/manual; a ação `registrar-reuniao` faz o incremento explícito.

## Análises de IA

Configure `OPENAI_API_KEY` nas variáveis de ambiente da execução. `OPENAI_MODEL` é opcional. A chave da IA não fica no código. Os três prompts originais foram preservados.

- `POST /api/reunioes/{id}/analises/{tipo}` gera e salva uma análise.
- `POST /api/reunioes/{id}/analises/{tipo}/previa` mostra a resposta completa sem salvar.
- Tipos: `smart-briefing`, `conversational-analytics`, `voc-tracker`.

A coluna `RESULTADO` do SQL original tem limite de 250 caracteres/bytes. A gravação usa uma resposta compacta e rejeita resultados maiores; não há truncamento. A prévia mantém o conteúdo completo. Tokens e modelo vêm da resposta do provedor. Sem chave, a API retorna 503.

A chamada HTTP ocorre antes da transação de gravação. A reunião é relida com bloqueio antes de salvar, evitando persistir uma análise de transcrição alterada. Há uma análise por tipo/reunião. Análises podem ser consultadas ou excluídas, sem edição manual do resultado.

## Compilar e testar

Na pasta do `pom.xml`:

```text
mvn clean verify
java -jar target/TOTVS-AdvisorMind-3.0.0.jar
```

Os testes automáticos locais usam respostas JDBC controladas e servidor HTTP de IA de teste; não abrem banco alternativo nem fazem chamadas pagas. Eles verificam DAOs, validação, JSON, regras, fechamento de recursos, commit/rollback e os três módulos de IA. Isso não substitui o teste no Oracle FIAP.

Há três testes Oracle opcionais, somente para um schema de teste autorizado. Eles recebem DB_URL/DB_USER/DB_PASSWORD no ambiente do teste. O teste transacional insere fixtures e faz rollback; valores de identidade podem ser consumidos. Nenhum desses testes foi executado contra a conta do usuário nesta revisão.

Veja os resultados e comandos em [docs/TESTES.md](docs/TESTES.md). A pasta `src/test` é separada do código principal.
