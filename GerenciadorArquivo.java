import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorArquivo {

    private static final String URL = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:postgresql://ep-ancient-firefly-acu5eu72-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "neondb_owner";
    private static final String PASS = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "npg_u1mvD7iLczJx";

    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // --- ALUNOS ---

    public static void salvarAlunos(ArrayList<Aluno> listaAlunos) {
    String sqlUpsert = "INSERT INTO alunos (matricula, nome, data_nascimento, ano_escolar, nivel_leitura, tem_necessidade, descricao_necessidade, responsavel_nome, responsavel_telefone, responsavel_endereco, status_pagamento, valor_contrato, ciclo_pagamento) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                       "ON CONFLICT (matricula) DO UPDATE SET " +
                       "nome = EXCLUDED.nome, data_nascimento = EXCLUDED.data_nascimento, ano_escolar = EXCLUDED.ano_escolar, " +
                       "nivel_leitura = EXCLUDED.nivel_leitura, tem_necessidade = EXCLUDED.tem_necessidade, descricao_necessidade = EXCLUDED.descricao_necessidade, " +
                       "responsavel_nome = EXCLUDED.responsavel_nome, responsavel_telefone = EXCLUDED.responsavel_telefone, responsavel_endereco = EXCLUDED.responsavel_endereco, " +
                       "status_pagamento = EXCLUDED.status_pagamento, valor_contrato = EXCLUDED.valor_contrato, ciclo_pagamento = EXCLUDED.ciclo_pagamento";

    try (Connection conn = conectar();
         PreparedStatement stmt = conn.prepareStatement(sqlUpsert)) {

        java.util.Map<String, Integer> contadorDatas = new java.util.HashMap<>();
        for (Aluno a : listaAlunos) {
             String matricula = a.getMatricula();
            if (matricula == null || matricula.isEmpty()) {
                matricula = gerarMatriculaBD(a, contadorDatas);
                a.setMatricula(matricula);
            }

            stmt.setString(1, matricula);
            stmt.setString(2, a.getNome());
            stmt.setDate(3, a.getDataNascimento() != null ? java.sql.Date.valueOf(a.getDataNascimento()) : null);
            stmt.setString(4, a.getAnoEscolar());
            stmt.setString(5, a.getNivelLeitura() != null ? a.getNivelLeitura().name() : null);
            stmt.setBoolean(6, a.isTemNecessidadeEspecial());
            stmt.setString(7, a.getDescricaoNecessidade());
            stmt.setString(8, a.getResponsavel() != null ? a.getResponsavel().nome() : null);
            stmt.setString(9, a.getResponsavel() != null ? a.getResponsavel().telefone() : null);
            stmt.setString(10, a.getResponsavel() != null ? a.getResponsavel().endereco() : null);
            stmt.setString(11, a.getStatusPagamento() != null ? a.getStatusPagamento().name() : "PENDENTE");
            stmt.setDouble(12, a.getValorContrato());
            stmt.setString(13, a.getCicloPagamento() != null ? a.getCicloPagamento() : "MENSAL");

            stmt.addBatch();
        }
        stmt.executeBatch();
        System.out.println("Dados de alunos sincronizados com o Neon!");
    } catch (SQLException e) {
        System.out.println("Erro ao salvar alunos no banco: " + e.getMessage());
    }
}

private static String gerarMatriculaBD(Aluno a, java.util.Map<String, Integer> contadorDatas) {
    String dataBaseStr = "00000000";
    if (a.getDataNascimento() != null) {
        dataBaseStr = a.getDataNascimento()
                .format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
    }
    int seq = contadorDatas.getOrDefault(dataBaseStr, 0) + 1;
    contadorDatas.put(dataBaseStr, seq);
    return dataBaseStr + String.format("%03d", seq);
}

    public static ArrayList<Aluno> carregarAlunos() {
        ArrayList<Aluno> lista = new ArrayList<>();
        String sql = "SELECT * FROM alunos";

        try (Connection conn = conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String nome = rs.getString("nome");
                LocalDate dataNasc = rs.getDate("data_nascimento").toLocalDate();
                String anoEscolar = rs.getString("ano_escolar");
                NivelLeitura nivelLeitura = NivelLeitura.valueOf(rs.getString("nivel_leitura"));
                boolean temNecessidade = rs.getBoolean("tem_necessidade");
                String descNecessidade = rs.getString("descricao_necessidade");
                String nomeResp = rs.getString("responsavel_nome");
                String telResp = rs.getString("responsavel_telefone");
                String endResp = rs.getString("responsavel_endereco");
                StatusPagamento statusPagamento = StatusPagamento.valueOf(rs.getString("status_pagamento"));
                double valorContrato = rs.getDouble("valor_contrato");
                String cicloPagamento = rs.getString("ciclo_pagamento");
                String matricula = rs.getString("matricula");

                Responsavel resp = new Responsavel(nomeResp, telResp, endResp);
                Aluno aluno = new Aluno(nome, dataNasc, anoEscolar, resp, nivelLeitura, temNecessidade,
                        descNecessidade, statusPagamento, valorContrato, cicloPagamento);
                          aluno.setMatricula(matricula);

                lista.add(aluno);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao carregar alunos do banco: " + e.getMessage());
        }
        return lista;
    }

    // --- AGENDAMENTOS ---

    public static void salvarAgendamentos(List<Agendamento> lista) {
        // No modelo relacional, podemos salvar os agendamentos diretamente upserting na
        // tabela
        String sqlUpsert = "INSERT INTO agendamentos (id, matricula_aluno, data, hora, pago, observacao, realizada) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "matricula_aluno = EXCLUDED.matricula_aluno, data = EXCLUDED.data, hora = EXCLUDED.hora, " +
                "pago = EXCLUDED.pago, observacao = EXCLUDED.observacao, realizada = EXCLUDED.realizada";

        try (Connection conn = conectar();
                PreparedStatement stmt = conn.prepareStatement(sqlUpsert)) {

            for (Agendamento a : lista) {
                stmt.setString(1, a.getId());
                stmt.setString(2, a.getMatriculaAluno());
                stmt.setDate(3, java.sql.Date.valueOf(a.getData()));
                stmt.setString(4, a.getHora());
                stmt.setBoolean(5, a.isPago());
                stmt.setString(6, a.getObservacao());
                stmt.setBoolean(7, a.isRealizada());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Erro ao salvar agendamentos no banco: " + e.getMessage());
        }
    }

    public static ArrayList<Agendamento> carregarAgendamentos() {
        ArrayList<Agendamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM agendamentos";

        try (Connection conn = conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String matricula = rs.getString("matricula_aluno");
                LocalDate data = rs.getDate("data").toLocalDate();
                String hora = rs.getString("hora");
                boolean pago = rs.getBoolean("pago");
                boolean realizada = rs.getBoolean("realizada");
                String obs = rs.getString("observacao");

                Agendamento a = new Agendamento(id, matricula, data, hora, pago, obs);
                a.setRealizada(realizada);
                lista.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar agendamentos do banco: " + e.getMessage());
        }

        return lista;
    }
}