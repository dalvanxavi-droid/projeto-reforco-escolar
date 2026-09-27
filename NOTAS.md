# Notas de Desenvolvimento — Projeto Reforço

Documento de referência técnica do projeto. Registra decisões, correções e aprendizados.

---

## 📌 Sobre o projeto

MVP de gestão para professores de aula de reforço. Criado para uso real (esposa do desenvolvedor) com potencial de comercialização.

**Stack:**
- **Back-end:** Java puro (com.sun.net.httpserver) + JDBC
- **Banco:** PostgreSQL (Neon)
- **Front-end:** HTML/JS/CSS estático (servido pelo próprio back)
- **Hospedagem:** Render (free tier)
- **Repositório:** https://github.com/dalvanxavi-droid/projeto-reforco-escolar

**Estrutura de dados:**
- Matrícula = data de nascimento (DDMMYYYY) + sufixo numérico de 3 dígitos
- Ex: aluno nascido em 22/10/1993 → matrícula `22101993001`
- O sufixo resolve colisões de mesma data de nascimento

---

## 🐛 Correção: bug de exclusão (25/09/2026)

### Sintoma
Alunos e agendamentos "voltavam" após serem excluídos. Em especial, o aluno "teste" (João Mateus) sempre reaparecia. Um deploy no Render fazia a agenda voltar.

### Diagnóstico
Três causas sobrepostas:

1. **`GerenciadorArquivo.java` não tinha método de DELETE.** Todos os métodos eram `SELECT` ou `INSERT ... ON CONFLICT ... DO UPDATE` (upsert). Nunca um `DELETE FROM`.
2. **`ServidorWeb.java` chamava `salvarAlunos()` no endpoint DELETE** — que só faz upsert dos que sobraram, sem deletar os removidos.
3. **A matrícula exibida no front era gerada dinamicamente** (baseada na data de nascimento), não a matrícula real do banco. Se a data de nascimento fosse editada depois do cadastro, a matrícula calculada divergia da armazenada, e a busca por matrícula no DELETE falhava silenciosamente.

### Correções aplicadas

**`GerenciadorArquivo.java`:**
- Adicionado método `excluirAluno(String matricula)` → `DELETE FROM alunos WHERE matricula = ?`
- Adicionado método `excluirAgendamento(String id)` → `DELETE FROM agendamentos WHERE id = ?`

**`ServidorWeb.java`:**
- `DELETE /api/alunos`: agora chama `GerenciadorArquivo.excluirAluno(...)` antes de salvar a lista
- `DELETE /api/alunos`: também coleta os IDs dos agendamentos do aluno e chama `excluirAgendamento(...)` para cada um
- `DELETE /api/agendamentos`: em todos os 3 formatos de entrada (query, array JSON, objeto JSON), chama `excluirAgendamento(...)` antes de remover da lista em memória
- `GET /api/alunos`: passou a usar `a.getMatricula()` (matrícula real do banco) em vez de `gerarMatricula(...)`
- `PUT /api/alunos/status`: mesma correção — usa `a.getMatricula()` em vez de matrícula gerada
- Método `gerarMatricula(...)` removido (código morto)

### Resultado
Exclusão funciona em produção. Aluno e agendamento removidos não voltam após F5, nem após reinício do servidor no Render. Neon e logs confirmam as operações.

---

---

## 🔐 Tela de login e sessão (26/09/2026)

### O que foi feito
Tela de acesso que protege todo o painel. A partir dela:
- Toda rota `/api/...` exige cookie de sessão válido (401 senão)
- Usuário é levado para a tela de login quando não autenticado
- Botão **🚪 Sair** encerra a sessão e volta à tela de login
- Sessão expira em 8h; reiniciar o servidor derruba todos os logins ativos

### Arquitetura (fluxo em 6 passos)
1. Usuário digita senha na tela de login
2. Navegador envia `POST /api/login` com `{"senha": "..."}`
3. Servidor compara com `SENHA_ESPERADA` (variável de ambiente ou fallback hardcoded)
4. Se bater, `Auth.criarSessao()` gera um token aleatório de 32 bytes (64 chars hex) e responde com `Set-Cookie: sessao=<token>; Path=/; HttpOnly; SameSite=Lax`
5. A partir daí, o navegador envia o cookie **automaticamente** em toda requisição pra mesma origem — as 13 chamadas `fetch()` existentes no `index.html` não precisaram ser modificadas
6. Toda rota `/api/*` tem uma guarda na primeira linha do `handle()`:
   ```java
   if (!Auth.sessaoValida(Auth.tokenDaRequisicao(exchange))) {
       Auth.negar(exchange);
       return;
   }
   ```

### Arquivos tocados
- **`Auth.java`** (novo, ~160 linhas) — validação de senha, criação/validação de sessão, parsing do cookie, construção de cabeçalhos
- **`ServidorWeb.java`** — 3 rotas novas (`/api/login` GET+POST, `/api/logout`), guard nas 3 rotas existentes (`/api/alunos`, `/api/agendamentos`, `/api/alunos/status`), diagnóstico mascarado, `Access-Control-Allow-Credentials: true` em todas as rotas
- **`index.html`** — `<section class="login-overlay">`, funções `entrar()`, `sair()`, guard em `navegar()`, `inicializarSistema()` que checa sessão antes de carregar dados

### Decisões e porquês

**Por que senha em variável de ambiente e não tabela no banco?**
Uso único (1 pessoa), não precisa suportar múltiplos usuários nem trocar de senha pelo sistema. Se precisar mais tarde, migração é trocar `SENHA_ESPERADA` por `SELECT ... FROM usuarios`.

**Por que fallback hardcoded no `Auth.java`?**
Mesmo padrão que o `GerenciadorArquivo.java` usa para as credenciais do banco. Senha hardcoded vira redundância quando `SENHA_ACESSO` estiver nas env vars do Render — mas garante que ninguém fica bloqueado se o Render perder a env var.
```java
private static final String SENHA_ESPERADA =
        System.getenv("SENHA_ACESSO") != null
                ? System.getenv("SENHA_ACESSO")
                : "************";
```

**Por que `SecureRandom` e não `UUID.randomUUID()` ou `Math.random()`?**
- `Math.random()` — 48 bits, previsível com a semente. **Nunca usar em sessão.**
- `UUID.randomUUID()` — 122 bits, bom, mas dá mais ruído do que precisa
- `SecureRandom.nextBytes(32)` — **256 bits**, padrão usado em criptografia

**Por que `ConcurrentHashMap` e não `HashMap`?**
O servidor atende requisições em threads diferentes. `HashMap` em escrita concorrente corrompe internamente.

**Por que `HttpOnly` no cookie?**
O JavaScript do navegador **não consegue ler** o token. Se um dia houver XSS no `index.html`, o atacante não consegue roubar a sessão via `document.cookie`.

**Por que sessão em memória e não no banco?**
Zero dado sensível no Neon (que é pago e tem retenção). Custo: redeploy no Render derruba os logins ativos. Para 1 usuário, o trade-off é favorável.

**Por que mensagens indistinguíveis?**
Senha errada, sessão expirada e config ausente têm mensagens diferentes (`Senha incorreta`, `Nao autenticado`, `Servidor sem SENHA_ACESSO`). Um atacante não consegue distinguir casos — princípio básico de segurança.

### ⚠️ O que **NÃO** foi feito (e vale saber)
- **Não há hash de senha.** A senha é guardada em texto puro na env var. Aceitável para 1 usuário interno; para sistema multiusuário, usar `BCrypt` ou `PBKDF2` com salt.
- **Não há proteção contra força bruta.** Alguém pode tentar infinitamente senhas erradas. Se o link do Render for vazado, vale adicionar rate limit.
- **Não há CSRF token.** Mitigado parcialmente pelo `SameSite=Lax` no cookie, mas um sistema mais crítico exigiria token por request.
- **`.env` não é lido pelo código.** Nenhuma das rotas abre o arquivo — as env vars vêm de `System.getenv()` que lê do ambiente do processo (Render, ou export do shell). O `.env` no repositório é só documentação.

### Variáveis de ambiente necessárias

Valores reais ficam no arquivo `.env` local e nas variáveis do painel do Render. Mantidos mascarados neste documento para não vazar em eventuais forks públicos.

```
DB_URL       = ************
DB_USER      = ************
DB_PASSWORD  = ************
SENHA_ACESSO = ************
```

### Testes que passaram
- Servidor sem `SENHA_ACESSO` → diagnóstico avisa, login devolve erro explicativo (500)
- Sem cookie → `/api/alunos` e `/api/agendamentos` retornam 401
- Login com senha errada → 401
- Login correto → 200 + cookie de 64 chars hex
- Cookie válido → 200 com dados reais
- Logout → mesmo cookie antigo passa a retornar 401
- `GET /` (o `index.html`) → continua 200 (a UI não tem segredo, a API é que protege)
- `DELETE /api/alunos` com sessão válida → 200 (as rotas existentes continuam funcionando)

---

## 🔧 Configurações importantes

### `.vscode/settings.json`
Necessário para o driver JDBC do PostgreSQL carregar ao rodar localmente:
```json
{
    "java.project.referencedLibraries": [
        "lib/**/*.jar",
        "postgresql-42.7.3.jar"
    ]
}

## 💡 Aprendizados

- **UPSERT ≠ DELETE.** ...
- **Sempre use `PreparedStatement` com `?`** ...
- **Não gere identificadores "fantasmas"** ...
- **Teste o ciclo completo:** ...
- **Commit antes de deploy.** ...