# Testes da reformulação

Validação em 20/10/2026: `mvn clean verify` passou. Foram descobertos 44 testes: 41 passaram e 3 testes Oracle opcionais foram ignorados, sem falhas ou erros.

Ambiente local: OpenJDK 26.0.2.1, Maven 3.9.11; o projeto compila com release Java 21. Não foi alegada execução local com JDK 21.

| Grupo | O que foi verificado |
| --- | --- |
| ConexaoFactoryTest | Abertura por DriverManager com driver de teste e propagação de SQLException |
| DaoJdbcTest | CRUD dos seis DAOs, SQL parametrizado, identidade por nome da coluna, datas, CLOB, fechamento de recursos e registros ausentes |
| TransacoesServiceTest | Uma conexão por operação, commit e rollback explícitos, falha no contador/índice, preservação do erro original, regras de IA e descarte de análise obsoleta |
| ApiContratoTest | Inicialização e varredura dos pacotes, contratos JSON, cadastros/listagens, validações, tratamento HTTP e ausência de chave nos três módulos |
| DomainRulesTest | Perfil comercial, senioridade, comissão, temperatura, duração e tipos de análise |
| OpenAIClientTest | Respostas e metadados controlados, timeout, ausência de chave e falhas do provedor |

Os testes locais não usam um banco alternativo. O JDBC é simulado por respostas controladas: eles não certificam execução SQL, restrições, concorrência ou rollback no servidor Oracle. O HTTP de IA usa um servidor de teste local e não faz chamadas pagas.

O JAR real foi iniciado e respondeu 404 para uma rota inexistente. O menu `main.Main` também iniciou e encerrou com a entrada 0. O JAR foi inspecionado: contém a nova classe principal e não contém as bibliotecas do banco local, do pool ou de Spring JDBC/transações.

Foram conferidos os dez pacotes pedidos, as rotas dos seis controllers, os três prompts originais e os três scripts SQL oficiais preservados. Os resultados resumidos estão em [resultados-testes.json](resultados-testes.json).

## Testes Oracle opcionais

Somente em um schema de teste autorizado, configure DB_URL, DB_USER e DB_PASSWORD no ambiente dos testes. Esses parâmetros não alteram a configuração da aplicação em ConexaoFactory.java.

```powershell
$env:RUN_ORACLE_READONLY='true'
mvn '-Dtest=OracleReadOnlyTest' test

$env:RUN_ORACLE_TRANSACTIONAL='true'
mvn '-Dtest=OracleTransactionalProbeTest,OracleDaoIntegrationTest' test
```

OracleReadOnlyTest consulta as colunas sem alteração de dados. Os outros dois probes inserem fixtures, usam uma transação e fazem rollback obrigatório, sem DDL nem limpeza geral. A identidade pode consumir números mesmo com rollback. Os três testes foram ignorados nesta execução porque não houve acesso ao Oracle real.
