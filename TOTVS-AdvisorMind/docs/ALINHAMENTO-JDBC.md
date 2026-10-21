# Alinhamento ao exemplo do professor

Revisão de 20/10/2026, a partir do commit `f274be0` do backend. Foram inspecionados os arquivos reais do repositório e as classes Aluno, AlunoDAO, AlunoBo, AlunoController e ConexaoFactory do arquivo SpringbootAluno-main.zip antes de concluir a implementação. O ZIP não faz parte do repositório e nenhuma credencial do exemplo foi copiada.

## Camadas e persistência

| Exemplo do professor | TOTVS AdvisorMind |
| --- | --- |
| Aluno (Model) | As seis entidades existentes em `entities`, sem mudança dos campos ou regras |
| AlunoDAO | ClienteDao, VendedorDao, VendedorRepoDao, ReuniaoRepoDao, ReuniaoDao e AnaliseIADao |
| AlunoBo | Services existentes: validações, regras e coordenação transacional |
| AlunoController | Controllers REST existentes, com os mesmos endpoints e contratos |
| ConexaoFactory | Factory Oracle com Class.forName e DriverManager.getConnection; adaptação transacional separada |

O backend mantém as camadas e o CRUD existentes com Connection, PreparedStatement e ResultSet. Esta revisão substitui a antiga factory vinculada ao DataSource por uma factory Oracle com abertura via DriverManager, como no professor. A adaptação ao Spring fica em classes separadas; os DAOs recebem o DataSource adaptado. ReuniaoDao permanece no pacote dao. JdbcTemplate continua somente nas fixtures dos testes de integração anteriores.

Todos os valores são parâmetros de PreparedStatement. Os nomes de tabelas, colunas e trechos de SQL são constantes internas dos DAOs; o texto de entrada nunca vira SQL. Inserções pedem explicitamente a coluna de identidade para evitar o ROWID devolvido por algumas formas de geração de chaves do Oracle. IDs numéricos são convertidos sem arredondamento/overflow silencioso. Datas de nascimento usam setDate; reuniões e análises usam setTimestamp, preservando segundos em Oracle DATE. O índice JSON usa character stream para CLOB.

## Transações e exceções

ConexaoFactory agora contém a abertura JDBC do professor: Class.forName("oracle.jdbc.driver.OracleDriver") e DriverManager.getConnection(URL, USUARIO, SENHA), propagando ClassNotFoundException/SQLException. A classe não depende de Spring, DataSource ou DataSourceUtils. BancoConfig fornece as configurações e cria o pool Hikari com ConexaoDataSource, cujo getConnection() chama factory.conexao(). Assim, a factory é usada nas conexões físicas reais da API e do console. ConexaoH2Factory separa o driver local/teste. JdbcSupport adquire/libera a conexão via DataSourceUtils, conforme o [contrato do Spring](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/jdbc/datasource/DataSourceUtils.html). Dentro de @Transactional ou TransactionTemplate, os DAOs compartilham a conexão da transação. Fora dela, a conexão é devolvida ao pool ao final de cada operação. Não há conexão armazenada em campo dos DAOs e eles não executam commit ou rollback individualmente.

PreparedStatement e ResultSet são fechados em try-with-resources, inclusive quando preparação, execução, mapeamento ou leitura da identidade falham. SQLException é traduzida para a hierarquia DataAccessException do Spring: violações de integridade continuam retornando HTTP 409 pelo handler existente; falhas internas continuam sem SQL, senha ou stack trace na resposta. As exceções de persistência são unchecked, preservando rollback dos Services.

Permanecem os bloqueios SELECT FOR UPDATE, a ordem dos bloqueios de pais, os contadores/JSON sincronizados na mesma transação, a proibição de excluir pais com dependentes e a proteção contra resultados de IA obsoletos. A chamada HTTP de IA continua fora da transação de gravação.

## Funcionalidades preservadas

- SmartBriefing AI, Conversational Analytics e VoC Tracker: prompts completos, prévia, persistência compacta, metadados reais, prevenção de duplicidades e limites sem truncamento.
- CRUD de clientes, vendedores, reuniões e repositórios, consultas e exclusão de análises. Resultados de IA continuam imutáveis pela API; não foi adicionado update manual de resultados do provedor.
- Relacionamentos e restrições das seis tabelas, nomes de colunas, identidades, datas, limites VARCHAR2 e CLOB. Nenhuma migração ou alteração do DDL.
- Regras de perfil comercial, comissão, senioridade, temperatura, duração, contador histórico manual e estatísticas; console e API compartilham os Services.
- Configuração H2 local/teste e configuração Oracle externa. Nenhuma alteração no frontend.

## Oracle FIAP e configuração segura

URL/USUARIO/SENHA são campos finais de cada factory, recebidos por construtor; o construtor vazio usa DB_USER/DB_PASSWORD do ambiente e a URL FIAP fornecida no exemplo, opcionalmente substituída por DB_URL. O método conexao() tem as mesmas duas chamadas do professor. As credenciais continuam externas para cumprir o requisito de não versionar senhas.

O perfil oracle mantém `spring.sql.init.mode=never`, desabilita explicitamente o console H2 e exige DB_USER e DB_PASSWORD externos; DB_URL pode substituir a URL FIAP padrão. Use a URL de conexão FIAP fornecida pela disciplina, sem usuário/senha na própria URL. O schema deve conter as seis tabelas compatíveis com `sql/oracle-create.sql`. Não foi feita conexão ao Oracle FIAP nesta revisão.

Exemplo PowerShell com placeholders, preenchidos apenas no ambiente local:

```powershell
$env:SPRING_PROFILES_ACTIVE='oracle'
$env:DB_URL='jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl'
$env:DB_USER='SEU_USUARIO'
$env:DB_PASSWORD='SUA_SENHA'
java -jar target/TOTVS-AdvisorMind-3.0.0.jar
```

`.env.example` é referência; o Spring não o carrega automaticamente. O .gitignore da raiz protege também .env e variantes privadas, .idea, .iml, target, logs e bancos locais em qualquer nível. O banco H2 e os arquivos .idea anteriormente rastreados na raiz foram removidos do índice Git. Isso não reescreve o histórico anterior e não representa uma migração do banco local.

## Validação desta revisão

- Base anterior: `mvn clean verify` passou, com 46 testes executados e 3 opcionais ignorados.
- Versão JDBC: `mvn clean verify` passou, com 51 testes executados e 3 opcionais Oracle ignorados (54 descobertos), sem falhas ou erros.
- JAR real: 52 verificações HTTP passaram em três inicializações, incluindo retenção das seis tabelas após reiniciar. Foram feitas quatro chamadas ao provedor local controlado, sem OpenAI real. O script usa WRITE_DELAY=0 somente no banco H2 temporário do teste para tornar os commits duráveis também quando terminate() encerra abruptamente no Windows, e exporta a evidência em UTF-8.
- Ambiente de execução disponível: OpenJDK 26.0.2.1 e Maven 3.9.11, compilação com release Java 21 definido no pom.xml. Não foi alegada execução local com JDK 21.
- ConexaoFactoryTest: uso real de DriverManager com driver JDBC de teste, parâmetros URL/usuário/senha, propagação de SQLException, tratamento de driver ausente no adaptador e conexões H2 independentes; sem Oracle real.
- JdbcContractTest: chaves Oracle por coluna, números, datas, SQL parametrizado com texto contendo comandos, null, CLOB, tradução de erros Oracle simulados e fechamento de recursos em falhas.
- JdbcTransactionRegressionTest: pool criado sobre ConexaoDataSource/ConexaoH2Factory e limitado a uma conexão, reutilização transacional, commit e rollback dos seis DAOs, contadores/JSON, repetições de sucesso e falha sem esgotar o pool.
- OracleConfigurationTest: carrega as propriedades reais do perfil e BancoConfig, verifica o adaptador da factory e a configuração do driver Oracle, resolve configuração externa e confirma ausência de conexão/DDL; não usa credenciais reais.
- Suíte anterior: 21 testes de API relacional, validações, restrições, CRUD, IA simulada, limite de bytes, CLOB longo e falhas durante atualização dos contadores; testes de domínio e do cliente HTTP preservados.

Resumo extraído dos relatórios Surefire e da execução HTTP: [jdbc-resumo.json](evidencias/jdbc-resumo.json).

Os testes Oracle reais são opt-in. Em schema de teste autorizado, configure DB_URL/DB_USER/DB_PASSWORD e execute:

```powershell
$env:RUN_ORACLE_READONLY='true'
mvn '-Dtest=OracleReadOnlyTest' test
# Somente em schema de teste autorizado; insere fixtures e faz rollback:
$env:RUN_ORACLE_TRANSACTIONAL='true'
mvn '-Dtest=OracleTransactionalProbeTest,OracleDaoIntegrationTest' test
```

OracleDaoIntegrationTest exercita os seis DAOs de produção na mesma transação Spring com o driver Oracle, identidades, DATE, CLOB, vínculos e rollback obrigatório, sem DDL. Identidades podem consumir números mesmo com rollback. OracleTransactionalProbeTest mantém o probe JDBC independente anterior. Nenhum deles foi executado contra Oracle nesta revisão. H2 e mocks não comprovam comportamento de NLS/charset/collation, locks, permissões, conectividade ou versão do servidor FIAP. A validação real ainda depende do acesso do usuário.

Os relatórios e o PDF da versão anterior são históricos; este documento registra o escopo e as evidências da refatoração JDBC atual.
