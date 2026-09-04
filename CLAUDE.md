# Projeto Reforço

Sistema fullstack para organizar as aulas de reforço da esposa do usuário (alunos, agendamentos, pagamentos).

## Tecnologias

- **Java 17** puro (sem framework) — servidor web com `com.sun.net.httpserver`
- **PostgreSQL** na nuvem (Neon) via JDBC (`postgresql-42.7.3.jar`)
- **Gson** (`lib/gson-2.11.0.jar`) para converter JSON ↔ objetos Java
- **Front-end**: um único `index.html` (HTML/CSS/JS vanilla)
- **Deploy**: Docker (imagem `eclipse-temurin:17-jdk`) no Render

## Arquivos principais

| Arquivo | O que faz |
|---|---|
| `ServidorWeb.java` | Servidor HTTP na porta 8080 + rotas da API (`/api/alunos`, etc.) e serve o `index.html` |
| `GerenciadorArquivo.java` | Toda comunicação com o banco Neon (salvar/carregar alunos e agendamentos) |
| `CadAluno.java` | Cadastro de aluno pelo terminal |
| `Aluno.java`, `Agendamento.java`, `Responsavel.java`, `NivelLeitura.java`, `StatusPagamento.java`, `RelatorioEmitivel.java` | Modelos (classes de domínio) |

## Como compilar e rodar

```bash
# Compilar (a partir da pasta do projeto)
javac -cp .:postgresql-42.7.3.jar:lib/gson-2.11.0.jar *.java

# Rodar o servidor
java -cp .:postgresql-42.7.3.jar:lib/gson-2.11.0.jar ServidorWeb
```

Acessar em `http://localhost:8080`.

## Banco de dados

- Credenciais vêm das variáveis de ambiente `DB_URL`, `DB_USER`, `DB_PASSWORD` (arquivo `.env`, não versionado).
- O código tem fallback com credenciais hardcoded dentro de `GerenciadorArquivo.java` — não remover sem antes garantir que as env vars funcionam no Render também.
- Os dados dos alunos ficam SOMENTE no banco; os `.txt` são resquícios da versão antiga (arquivo).

## Convenções do projeto

- Código, comentários e mensagens de commit em **português**.
- Estilo simples e didático: o usuário está aprendendo Java; explicar mudanças de forma clara, sem jargão desnecessário.
- Não usar frameworks (Spring etc.) — manter Java puro.
- `.class` compilados ficam na raiz mesmo (não mover pra `bin/`).
