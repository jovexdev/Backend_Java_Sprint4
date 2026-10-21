# TOTVS AdvisorMind - Sprint 4 FINAL (3.0.0)

Evolução da base Java 21 / Spring Boot 3.5.7 / Maven. As seis tabelas do SQL recebido são usadas por JDBC: VENDEDOR_REPO, CLIENTE, REUNIAO_REPO, VENDEDOR, REUNIAO e ANALISE_IA. Oracle permanece suportado por configuração; H2 é perfil local de avaliação. Sem arquivos JSON como fonte de reuniões.

Persistência alinhada ao exemplo `SpringbootAluno-main.zip`: Model (`entities`) → Service/BO → DAO com `Connection`, `PreparedStatement`, `ResultSet` e `ConexaoFactory`. A abertura física segue o método do professor: Class.forName("oracle.jdbc.driver.OracleDriver") e DriverManager.getConnection(URL, USUARIO, SENHA). ConexaoDataSource/BancoConfig integram essa factory ao pool e às transações Spring para API e console. Detalhes, comparação e validação desta revisão: [Alinhamento JDBC](docs/ALINHAMENTO-JDBC.md).

## Executar
JDK 21 e Maven 3.6.3+ (validação: Zulu 21.0.8, Maven 3.9.11). Abra no IntelliJ a pasta contendo pom.xml. Confirme `java -version` e `mvn -version`.

```bash
mvn clean verify
java -jar target/TOTVS-AdvisorMind-3.0.0.jar
```

O perfil local padrão cria schema H2 em `data/advisormind-final.mv.db` e inicia em http://127.0.0.1:8080. Não há carga automática de exemplos. O nome novo evita reutilizar silenciosamente o banco da versão 2.0.0. Não apaga o banco/JSON antigo nem migra dados sem datas/FKs conhecidas.

Crie nessa ordem via Postman: vendedor-repos, clientes, reuniao-repos, vendedores e reuniões. Use os IDs retornados pelo banco. A coleção docs/AdvisorMind-FINAL.postman_collection.json salva os IDs automaticamente; docs/requests.http usa variáveis que você preenche. Não use os corpos antigos com id manual, cliente embutido ou ticket persistido.

Execute AdvisorMindApplication para API; Main para menu de console no IntelliJ, com as mesmas configurações e serviços. Menu inclui cadastro de clientes e repositórios. Opções de IA do console geram prévia não persistida, explicitamente informada.

## Oracle
Nenhum script é executado automaticamente no perfil oracle. O banco deve estar em Oracle 12c+ com identidade, schema de teste autorizado e as seis tabelas disponíveis.
- sql/recebido.sql: transcrição formatada dos comandos fornecidos no chat; contém DDL/DML/DQL e as repetições finais. Não é idempotente e NÃO limpa tabelas apesar do cabeçalho. Não rodar em banco com dados reais.
- sql/oracle-create.sql: somente DDL de criação, separado; sem DROP. Rode UMA vez em schema vazio, ou use o schema oficial já criado. DDL Oracle pode efetivar commits implícitos; erros podem deixar criação parcial, portanto revisar antes de executar.
- sql/proposta-demo-oracle.sql: carga suplementar opcional com prefixo DEMO S4; procura chaves naturais e usa RETURNING, sem assumir IDs 1 a 5. Reexecução sequencial sem outros escritores não duplica os registros da demonstração. Não simula resultados de IA.
- sql/proposta-sincronizar-repos-oracle.sql: proposta de reparo dos contadores/JSON, Oracle 12.2+ para JSON_ARRAYAGG. Execute manualmente sem escritores concorrentes ou prefira PUT dos repositórios pela API.
- sql/dql-academico.sql: as cinco categorias de consultas recebidas, sem alteração semântica.

Configure a conta diretamente no construtor vazio de `src/main/java/br/com/totvs/advisormind/conexoes/ConexaoFactory.java`, apenas na sua cópia local:

```java
public ConexaoFactory() {
  this("jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl",
      "Seu usuario Oracle", "Sua Senha Oracle");
}
```

O perfil Oracle usa `new ConexaoFactory()`: valores DB_USER/DB_PASSWORD ou spring.datasource.username/password não substituem esse construtor. Os placeholders do repositório devem ser preenchidos localmente; não faça commit da classe com senha real. O construtor de três parâmetros permanece disponível para testes e uso programático.

No IntelliJ, ative `SPRING_PROFILES_ACTIVE=oracle` na configuração de execução e rode AdvisorMindApplication (API) ou Main (console). Para executar o JAR após editar a classe, recompile:

```powershell
mvn clean package
$env:SPRING_PROFILES_ACTIVE='oracle'
java -jar target/TOTVS-AdvisorMind-3.0.0.jar
```

`.env.example` é referência e não é carregado automaticamente pelo Spring. DB_URL/DB_USER/DB_PASSWORD nele são somente para os probes Oracle independentes; a API usa a configuração da classe. OPENAI_API_KEY continua externa.

## Datas, comprimentos e persistência
Cliente usa LocalDate. Reunião usa LocalDateTime sem fuso, até segundos, no formato yyyy-MM-ddTHH:mm:ss: Oracle DATE preserva hora/minuto/segundo. UNIQUE(cliente,data) é por instante local completo, não por dia. UTC é a convenção de datahoraExecucao de IA, armazenada sem offset em DATE. Não há timestamp com fuso no DDL.

Reunião exige vendedor, cliente e repositório persistidos, data, status e transcrição de até 4000 caracteres/bytes UTF-8. Campos VARCHAR2 são validados também por bytes, conservadoramente para AL32UTF8/NLS_LENGTH_SEMANTICS=BYTE. Nenhum texto é truncado. Charset, collation e NLS efetivos do Oracle ainda precisam ser verificados.

As tabelas relacionais são a fonte de verdade. REUNIAO_REPO.ARQUIVO_JSON é índice derivado {"reunioes":[ids]} em CLOB, reconstruído na mesma transação da alteração de reuniões; TOTAL_REUNIOES também é derivado. VENDEDOR_REPO.TOTAL_VENDEDORES é derivado de sua FK, FILTRO é descritivo e não um filtro executável. GET não repara carga externa incoerente; PUT sincroniza. Operações JDBC diretas fora da aplicação exigem sincronização dos caches. As linhas pai são bloqueadas para coordenar as alterações feitas pela API.

VENDEDOR.TOTAL_REUNIOES é o contador histórico declarado da Sprint 3; não é atualizado automaticamente ao cadastrar uma reunião. A ação registrar-reuniao o incrementa explicitamente. As consultas acadêmicas distinguem esse contador da contagem de linhas REUNIAO.

Sem cascatas ocultas: exclusão com dependentes retorna 409. Ticket médio não existe no banco final. Simule comissão usando GET /api/vendedores/{id}/indicadores?ticketMedio=1500; valor não é persistido e fórmula é acadêmica. Sem ticket, comissão é null com motivo. Modelo, regras de senioridade, perfil, temperatura e duração foram preservados no que é compatível.

## IA
Configure OPENAI_API_KEY e opcionalmente OPENAI_MODEL (gpt-4o-mini herdado). Os três prompts completos foram preservados. Endpoint principal gera versão compacta e salva resultado, modelo e usage.total_tokens reais. O limite RESULTADO=250 é estrito; exceder retorna 502 sem salvar nem cortar. Nenhum token ausente vira zero. Resposta precisa conter modelo, uso válido e finish_reason=stop.

POST /api/reunioes/{id}/analises/{tipo} cria uma análise (201). Tipo: smart-briefing (1), conversational-analytics (2), voc-tracker (3). Uma análise por tipo/reunião; repetição retorna 409. Endpoint /previa mantém resposta completa sem gravar. Ambas são chamadas novas ao provedor e podem gerar custo. Sem chave: 503. Não há retry automático.

A gravação ocorre em transação após HTTP, com releitura bloqueada da reunião e comparação dos dados originais. Atualizar transcrição/cliente/vendedor/data de reunião já analisada exige excluir análises primeiro. Resultados são imutáveis via API; podem ser consultados ou excluídos. Recomendação formal separada: ampliar RESULTADO para CLOB se a equipe quiser persistir análises completas; esta entrega NÃO modifica o DDL para isso.

## Testes
```bash
mvn clean verify
python3 scripts/smoke_http.py
```

Testes locais usam H2, esquemas temporários e provedor HTTP controlado; não chamam OpenAI real. O script inicia o JAR três vezes, usa porta 18080 e pasta temporária própria, e comprova retenção das seis tabelas. `JAVA_HOME`/PATH deve apontar ao Java 21.

Oracle: dois testes opcionais não executados neste ambiente. Somente leitura:
```bash
RUN_ORACLE_READONLY=true mvn -Dtest=OracleReadOnlyTest test
```
Probe transacional de JDBC em schema de TESTE explicitamente autorizado:
```bash
RUN_ORACLE_TRANSACTIONAL=true mvn -Dtest=OracleTransactionalProbeTest test
```
Para verificar também os seis DAOs reais dentro de uma transação Spring, no mesmo schema de teste autorizado:
```bash
RUN_ORACLE_TRANSACTIONAL=true mvn -Dtest=OracleDaoIntegrationTest test
```
Use DB_URL/DB_USER/DB_PASSWORD válidos para OracleReadOnlyTest e OracleTransactionalProbeTest. OracleDaoIntegrationTest usa a conta configurada localmente em ConexaoFactory(). O probe NÃO executa DDL ou limpa tabelas: cria dados com prefixo TEST_ dentro de transação, verifica identidades/DATE/CLOB/constraints/CRUD e sempre faz rollback. Pode consumir valores da identidade, mesmo com rollback, e não substitui a validação completa da API em Oracle. Testes locais não devem ser reconfigurados para apontar ao Oracle: contêm limpeza/DDL exclusivos dos schemas H2 descartáveis.

## Material de entrega
PDF final, docs/API.md, docs/DECISOES.md, docs/REVISAO-SQL.md, docs/RELATORIO-TESTES.md e docs/RELATORIO-ALTERACOES.md. Diagramas editáveis/exportados em docs/diagramas. Front-End/prints não foram fornecidos. Não existe integração visual ou com modelos Data Science alegada. API acadêmica local, sem autenticação definida; exposição pública requer projeto de segurança.
