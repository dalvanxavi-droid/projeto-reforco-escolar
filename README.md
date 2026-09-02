# 📚 Sementes do Saber - Sistema de Gerenciamento de Reforço Escolar

MVP (Minimum Viable Product) desenvolvido em Java para gerenciar o cadastro de alunos, acompanhamento e agendamento de aulas de reforço escolar. O projeto passou por uma evolução arquitetural recente, saindo de arquivos locais (`.txt`) para uma arquitetura moderna baseada em nuvem.

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Java 17
- **Banco de Dados:** PostgreSQL (hospedado na nuvem via [Neon](https://neon.tech/))
- **Conexão:** JDBC nativo
- **Containerização:** Docker
- **Hospedagem / Deploy:** Render

## ⚙️ Funcionalidades

- Cadastro completo de Alunos (com dados pessoais, histórico de leitura, necessidades especiais e informações do responsável).
- Gerenciamento de Contratos e Ciclos de Pagamento.
- Sistema de Agendamento de Aulas vinculado aos alunos com exclusão em cascata.
- Persistência de dados 100% em banco relacional na nuvem.

## 🛠️ Como rodar o projeto localmente

1. Clone o repositório:
   ```bash
   git clone [https://github.com/seu-usuario/nome-do-repositorio.git](https://github.com/seu-usuario/nome-do-repositorio.git)
   cd nome-do-repositorio