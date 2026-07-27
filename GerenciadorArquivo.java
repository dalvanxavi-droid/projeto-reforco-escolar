import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class GerenciadorArquivo {
    private static final String CAMINHO_ARQUIVO = "alunos.txt";

    public static void salvarAlunos(ArrayList<Aluno> listaAlunos) {
        java.time.format.DateTimeFormatter formatadorTxt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (FileWriter fw = new FileWriter(CAMINHO_ARQUIVO);
                PrintWriter pw = new PrintWriter(fw)) {

            for (Aluno aluno : listaAlunos) {
                // Junta os dados do aluno separados por ponto e vírgula
                pw.println(
                        aluno.getNome() + ";" +
                                aluno.getDataNascimento().format(formatadorTxt) + ";" +
                                aluno.getAnoEscolar() + ";" +
                                aluno.getNivelLeitura() + ";" +
                                aluno.isTemNecessidadeEspecial() + ";" +
                                aluno.getDescricaoNecessidade() + ";" +
                                aluno.getResponsavel().nome() + ";" +
                                aluno.getResponsavel().telefone() + ";" +
                                aluno.getResponsavel().endereco() + ";" +
                                aluno.getStatusPagamento().name());
                                
            }
            System.out.println("💾 Dados salvos com sucesso em " + CAMINHO_ARQUIVO);

        } catch (IOException e) {
            System.out.println("❌ Erro ao salvar os dados: " + e.getMessage());
        }
    }

    public static ArrayList<Aluno> carregarAlunos() {
        ArrayList<Aluno> lista = new ArrayList<>();
        java.io.File arquivo = new java.io.File("alunos.txt");

        if (!arquivo.exists()) {
            return lista;
        }

        // Criamos o formatador brasileiro aqui para a leitura
        java.time.format.DateTimeFormatter formatadorTxt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader(arquivo, java.nio.charset.StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 10) { // Ajustado para 10 campos salvos

                    String nome = partes[0];
                    LocalDate dataNasc = LocalDate.parse(partes[1], formatadorTxt);
                    String anoEscolar = partes[2];
                    NivelLeitura nivelLeitura = NivelLeitura.valueOf(partes[3]);
                    boolean temNecessidade = Boolean.parseBoolean(partes[4]);
                    String descNecessidade = partes[5]; 
                    String nomeResp = partes[6];
                    String telResp = partes[7];
                    String endResp = partes[8];
                    StatusPagamento statusPagamento = StatusPagamento.valueOf(partes[9]);
                    Responsavel resp = new Responsavel(nomeResp, telResp, endResp);

                    Aluno aluno = new Aluno(nome, dataNasc, anoEscolar, resp, nivelLeitura, temNecessidade,
                            descNecessidade, statusPagamento);

                    lista.add(aluno);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar os dados: " + e.getMessage());
        }
        return lista;
    }

    public static void salvarAgendamentos(List<Agendamento> lista) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("agendamentos.txt"))) {
            for (Agendamento a : lista) {
                pw.println(a.getId() + ";" +
                        a.getMatriculaAluno() + ";" +
                        a.getData() + ";" +
                        a.getHora() + ";" +
                        a.isPago() + ";" +
                        a.getObservacao());
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar agendamentos: " + e.getMessage());
        }

    }

    public static ArrayList<Agendamento> carregarAgendamentos() {
        ArrayList<Agendamento> lista = new ArrayList<>();
        java.io.File arquivo = new java.io.File("agendamentos.txt");

        if (!arquivo.exists()) {
            return lista;
        }

        try (Scanner scanner = new Scanner(arquivo)) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                String[] dados = linha.split(";");

                if (dados.length == 6) {
                    String id = dados[0];
                    String matricula = dados[1];
                    LocalDate data = LocalDate.parse(dados[2]);
                    String hora = dados[3];
                    boolean pago = Boolean.parseBoolean(dados[4]);
                    String obs = dados[5];

                    Agendamento a = new Agendamento(id, matricula, data, hora, pago, obs);
                    lista.add(a);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar agendamentos: " + e.getMessage());
        }

        return lista;
    }

}