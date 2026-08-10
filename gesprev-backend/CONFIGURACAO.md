# Configuracao local

O backend nao armazena credenciais no codigo. Antes de iniciar no PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/postgres"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="sua-senha"
$env:DB_SCHEMA="gesprev"
$env:JWT_SECRET="use-uma-chave-com-pelo-menos-32-caracteres"
.\mvnw.cmd spring-boot:run
```

Para iniciar com o servidor Llama, acrescente o perfil e a URL:

```powershell
$env:LLAMA_BASE_URL="http://servidor:8000/v1"
$env:LLAMA_LLM_MODEL="nome-do-modelo"
$env:LLAMA_VLM_MODEL="nome-do-modelo-visual"
$env:AI_VLM_MAX_PAGES="30"
$env:RAG_SERVICE_URL="http://localhost:8001"
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=ai-llama'
```

O endpoint configurado em `LLAMA_BASE_URL` deve expor uma API compativel com OpenAI em `/v1`,
incluindo chat completions. O modelo textual precisa aceitar tool calling; o modelo visual precisa
aceitar multiplas imagens na mesma requisicao. O RAG legislativo nao depende do modelo remoto.

Nunca grave chaves reais em `application.properties` ou em arquivos versionados.

## Testes automatizados

Os testes usam o schema isolado `gesprev_test` no mesmo PostgreSQL:

```powershell
$env:TEST_DB_PASSWORD="sua-senha-local"
.\mvnw.cmd test
```

O Flyway cria as tabelas e os usuarios de teste automaticamente. O fluxo de aceitacao remove
o processo e os arquivos que cria.
