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