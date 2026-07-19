import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class CadAluno {
    public static void main(String[] args) {
        // Inicializando o leitor com o UTF-8 para aceitar os caracteres no terminal do
        // Linux
        Scanner leitor = new Scanner(System.in, "UTF-8");
        ArrayList<Aluno> ListaAlunos = new ArrayList<>();
        int continuar = 1;

        // Criando o formatador para ler a data digitada pelo usuário
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        while (continuar == 1) {
            System.out.println("=== TESTE DE CADASTRO DE ALUNO ===");
            System.out.print("Digite o nome do aluno: ");
            String nomeAluno = leitor.nextLine();

            // Validação da Data de Nascimento para não quebrar o programa
            LocalDate dataNascimento = null;
            while (dataNascimento == null) {
                System.out.print("Digite a data de nascimento do aluno (dd/mm/aaaa): ");
                String dataTexto = leitor.nextLine();

                try {
                    dataNascimento = LocalDate.parse(dataTexto, formatador);
                } catch (Exception e) {
                    System.out.println("Formato inválido! Você precisa digitar com as barras. Exemplo: 22/10/2020");
                }
            }

            System.out.print("Digite em qual ano escolar o aluno está: ");
            String anoAluno = leitor.nextLine();

            System.out.print("Nome do(a) responsável: ");
            String nomeResponsavel = leitor.nextLine();

            System.out.print("Telefone para contato: ");
            String telefoneResponsavel = leitor.nextLine();

            System.out.print("Endereço: ");
            String enderecoResponsavel = leitor.nextLine();

            // Criando o objeto Responsavel com a ordem corrigida: nome, telefone, endereco
            Responsavel responsavelAluno = new Responsavel(nomeResponsavel, telefoneResponsavel, enderecoResponsavel);

            NivelLeitura nivelSelecionado = null;
            while (nivelSelecionado == null) {
                System.out.print("Em qual nível de leitura o aluno se encontra? Escolha uma das opções a seguir. \n");
                System.out.print("Digite o número da opção escolhida: \n");
                System.out.println("""
                        1 - ICONICO
                        2 - GARATUJA
                        3 - PRE_SILABICA
                        4 - SILABICA_SEM_VALOR_SONORO
                        5 - SILABICA_COM_VALOR_SONORO
                        6 - SILABICA_ALFABETICA
                        7 - ALFABETICA
                        8 - ORTOGRAFICA """);

                int nivelLeitura = leitor.nextInt();
                leitor.nextLine(); // Limpa o buffer do teclado

                if (nivelLeitura == 1) {
                    nivelSelecionado = NivelLeitura.ICONICO;
                    System.out.println("Você escolheu a opção " + NivelLeitura.ICONICO);
                } else if (nivelLeitura == 2) {
                    nivelSelecionado = NivelLeitura.GARATUJA;
                    System.out.println("Você escolheu a opção " + NivelLeitura.GARATUJA);
                } else if (nivelLeitura == 3) {
                    nivelSelecionado = NivelLeitura.PRE_SILABICA;
                    System.out.println("Você escolheu a opção " + NivelLeitura.PRE_SILABICA);
                } else if (nivelLeitura == 4) {
                    nivelSelecionado = NivelLeitura.SILABICA_SEM_VALOR_SONORO;
                    System.out.println("Você escolheu a opção " + NivelLeitura.SILABICA_SEM_VALOR_SONORO);
                } else if (nivelLeitura == 5) {
                    nivelSelecionado = NivelLeitura.SILABICA_COM_VALOR_SONORO;
                    System.out.println("Você escolheu a opção " + NivelLeitura.SILABICA_COM_VALOR_SONORO);
                } else if (nivelLeitura == 6) {
                    nivelSelecionado = NivelLeitura.SILABICA_ALFABETICA;
                    System.out.println("Você escolheu a opção " + NivelLeitura.SILABICA_ALFABETICA);
                } else if (nivelLeitura == 7) {
                    nivelSelecionado = NivelLeitura.ALFABETICA;
                    System.out.println("Você escolheu a opção " + NivelLeitura.ALFABETICA);
                } else if (nivelLeitura == 8) {
                    nivelSelecionado = NivelLeitura.ORTOGRAFICA;
                    System.out.println("Você escolheu a opção " + NivelLeitura.ORTOGRAFICA);
                } else {
                    System.out.println("Opção inválida! Por favor, escolha uma opção de 1 à 8. ");
                }
            }

            boolean temNecessidadeEspecial = false;
            String descricaoNecessidade = "";
            int opcao = 0;
            while (opcao != 1 && opcao != 2) {
                System.out.print("O(a) aluno(a) possui necessidade especial de aprendizagem? \n");
                System.out.println("""
                        1 - SIM
                        2 - NÃO""");

                opcao = leitor.nextInt();
                leitor.nextLine(); // Limpa o buffer do teclado

                if (opcao == 1) {
                    temNecessidadeEspecial = true;
                    System.out.println("Digite a necessidade especial do(a) aluno(a)");
                    descricaoNecessidade = leitor.nextLine();
                    System.out.println("Aluno cadastrado com sucesso");
                } else if (opcao == 2) {
                    temNecessidadeEspecial = false;
                    System.out.println("Aluno cadastrado com sucesso");
                } else {
                    System.out.println("Opção inválida! Por favor, escolha a opção entre 1 ou 2. ");
                }
            }

            // Criando o objeto Aluno passando dataNascimento no lugar da antiga idadeAluno
            // string
            Aluno dadosAluno = new Aluno(nomeAluno, dataNascimento, anoAluno, responsavelAluno, nivelSelecionado,
                    temNecessidadeEspecial, descricaoNecessidade);
            ListaAlunos.add(dadosAluno);

            System.out.println("Deseja cadastrar mais um aluno? (1 - SIM / 2 - NÃO): ");
            continuar = leitor.nextInt();
            leitor.nextLine(); // Limpa o buffer do teclado

            while (continuar != 1 && continuar != 2) {
                System.out.println("Opção inválida! Por favor, escolha entre 1 ou 2.");
                System.out.println("Deseja cadastrar mais um aluno? (1 - SIM / 2 - NÃO):");
                continuar = leitor.nextInt();
                leitor.nextLine();
            }
        }

        System.out.println("Até logo, Lidiane! Cadastro encerrado.");
        System.out.println("--- RELATÓRIO RÁPIDO DOS ALUNOS CADASTRADOS ---");
        for (Aluno aluno : ListaAlunos) {
            aluno.exibirDadosResumidos();

        }
        leitor.close();
    }
}