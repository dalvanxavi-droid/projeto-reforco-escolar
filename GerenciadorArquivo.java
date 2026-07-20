import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalDate;

public class GerenciadorArquivo {
    private static final String CAMINHO_ARQUIVO = "alunos.txt";

    public static void salvarAlunos(ArrayList<Aluno> listaAlunos) {
        java.time.format.DateTimeFormatter formatadorTxt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (FileWriter fw = new FileWriter(CAMINHO_ARQUIVO);
                PrintWriter pw = new PrintWriter(fw)) {

            for (Aluno aluno : listaAlunos) {
                // Junta os dados do aluno separados por ponto e vírgula
                pw.println(aluno.getMatricula() + ";" +
                        aluno.getNome() + ";" +
                        aluno.getDataNascimento().format(formatadorTxt) + ";" +
                        aluno.getAnoEscolar() + ";" +
                        aluno.getNivelLeitura() + ";" +
                        aluno.isTemNecessidadeEspecial() + ";" +
                        aluno.getDescricaoNecessidade() + ";" +
                        aluno.getResponsavel().nome() + ";" +
                        aluno.getResponsavel().telefone() + ";" +
                        aluno.getResponsavel().endereco());
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
                if (partes.length >= 10) {
                    String matricula = partes[0];
                    String nome = partes[1];

                    LocalDate dataNasc = LocalDate.parse(partes[2], formatadorTxt);
                    String anoEscolar = partes[3];
                    NivelLeitura nivelLeitura = NivelLeitura.valueOf(partes[4]);
                    boolean temNecessidade = Boolean.parseBoolean(partes[5]);
                    String descNecessidade = partes[6];

                    String nomeResp = partes[7];
                    String telResp = partes[8];
                    String endResp = partes[9];
                    Responsavel resp = new Responsavel(nomeResp, telResp, endResp);

                    Aluno aluno = new Aluno(nome, dataNasc, anoEscolar, resp, nivelLeitura, temNecessidade,
                            descNecessidade);
                    aluno.setMatricula(matricula);

                    lista.add(aluno);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar os dados: " + e.getMessage());
        }

        return lista;
    }
}