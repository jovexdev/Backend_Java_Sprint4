# Estrutura e conceitos

A organização em `src/main/java/br/com/totvs/advisormind` segue os dez pacotes solicitados.

| Pacote | Responsabilidade | Exemplos |
| --- | --- | --- |
| main | Iniciar a aplicação e executar testes manuais | AdvisorMindApplication, Main, TesteConexao |
| conexoes | Abrir conexão Oracle com DriverManager | ConexaoFactory |
| controller | Receber requisições HTTP e devolver respostas | ClienteController, ReuniaoController, AnaliseController |
| dao | Executar SQL parametrizado e montar entidades a partir do ResultSet | ClienteDao, VendedorDao, ReuniaoDao e os outros três DAOs |
| dto | Transportar os dados das requisições e respostas específicas | ClienteRequest, ReuniaoRequest, AnalisePreview |
| entities | Representar as seis tabelas e suas regras comerciais | Cliente, Vendedor, Reuniao, AnaliseIA, VendedorRepo, ReuniaoRepo |
| exception | Representar erros e devolver mensagens adequadas à API | ApiException, PersistenciaException, ApiErrorHandler |
| repository | Organizar o agrupamento de reuniões persistidas e seu índice | ReuniaoRepository |
| service | Validar dados, aplicar regras e coordenar DAOs | ClienteService, VendedorService, ReuniaoService, AnaliseService |
| util | Funções de apoio | ConsoleFormatter, JsonUtil |

## Comparação com o exemplo do professor

- `entities` corresponde ao Model: classes com atributos privados, construtores, getters e setters.
- Os DAOs seguem o padrão de AlunoDAO: usam uma Connection, escrevem o SQL completo e preenchem PreparedStatement com setString/setInt/setLong/setDate/setTimestamp.
- Os Services fazem o papel de AlunoBo: validações e regras de negócio antes de concluir as operações.
- Os Controllers seguem o padrão REST de AlunoController: GET, POST, PUT e DELETE.
- `ConexaoFactory.conexao()` carrega o driver Oracle com Class.forName e chama DriverManager.getConnection(URL, USUARIO, SENHA).

Cada DAO pode abrir sua conexão pelo construtor vazio, como no exemplo, ou receber uma conexão de um Service. O segundo construtor permite que uma operação envolvendo vendedor e agrupamento, por exemplo, seja confirmada ou desfeita por inteiro.

## Exemplo de fluxo: cadastrar reunião

1. ReuniaoController recebe um ReuniaoRequest.
2. ReuniaoService valida transcrição, data, status e IDs.
3. O Service abre uma conexão Oracle e inicia a transação com setAutoCommit(false).
4. ClienteDao e VendedorDao conferem os vínculos usando essa conexão.
5. ReuniaoDao insere a reunião com PreparedStatement.
6. ReuniaoRepository atualiza o índice e o contador do agrupamento pelo ReuniaoRepoDao, na mesma conexão.
7. O Service chama commit; se houver erro, chama rollback. A conexão é fechada ao final.

A transação não reverte uma chamada HTTP externa já efetuada. Por isso a análise de IA é solicitada antes de abrir a transação curta que salva o resultado.

## Tema e regras mantidas

O trabalho continua sendo o TOTVS AdvisorMind: apoio à análise de reuniões comerciais, com SmartBriefing AI, Conversational Analytics e VoC Tracker. Não foram criados modelos de Data Science ou resultados artificiais como se fossem respostas reais da IA.

O schema original e os limites das colunas permanecem iguais. Os prompts, cadastros, relacionamentos, regras comerciais e endpoints foram preservados. O índice JSON de reuniões é derivado das linhas Oracle; não é uma fonte paralela de dados.

A reformulação removeu o DAO genérico, os adaptadores de DataSource, o pool, as transações declarativas e os records. Os documentos e scripts que descreviam a arquitetura anterior foram retirados da entrega atual; permanecem recuperáveis no histórico Git.
