import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class CadAluno {
    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in, "UTF-8");
        ArrayList<Aluno> ListaAlunos = GerenciadorArquivo.carregarAlunos();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int opcao = 0;

        while (opcao != 5) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1 - Cadastrar Aluno");
            System.out.println("2 - Encontrar Aluno Cadastrado");
            System.out.println("3 - Remover Aluno");
            System.out.println("4 - Relatório");
            System.out.println("5 - Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(leitor.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Entrada inválida! Digite apenas números de 1 a 5.");
                continue; // Pula o switch e reinicia o menu
            }

            switch (opcao) {
                case 1:
                    System.out.println("\n=== CADASTRO DE ALUNO ===");
                    System.out.print("Digite o nome do aluno: ");
                    String nomeAluno = leitor.nextLine();

                    LocalDate dataNascimento = null;
                    while (dataNascimento == null) {
                        System.out.print("Digite a data de nascimento do aluno (dd/mm/aaaa): ");
                        String dataTexto = leitor.nextLine();
                        try {
                            dataNascimento = LocalDate.parse(dataTexto, formatador);
                        } catch (Exception e) {
                            System.out.println("Formato inválido! Use o padrão dd/mm/aaaa.");
                        }
                    }

                    if (alunoJaExiste(ListaAlunos, nomeAluno, dataNascimento)) {
                        System.out.println("⚠️ Aluno já cadastrado no sistema!");
                        break;
                    }

                    System.out.print("Digite em qual ano escolar o aluno está: ");
                    String anoAluno = leitor.nextLine();

                    System.out.print("Nome do(a) responsável: ");
                    String nomeResponsavel = leitor.nextLine();

                    System.out.print("Telefone para contato: ");
                    String telefoneResponsavel = leitor.nextLine();

                    System.out.print("Endereço: ");
                    String enderecoResponsavel = leitor.nextLine();

                    Responsavel responsavelAluno = new Responsavel(nomeResponsavel, telefoneResponsavel,
                            enderecoResponsavel);

                    NivelLeitura nivelSelecionado = null;
                    while (nivelSelecionado == null) {
                        System.out.println(
                                "Em qual nível de leitura o aluno se encontra?\n1 - ICONICO\n2 - GARATUJA\n3 - PRE_SILABICA\n4 - SILABICA_SEM_VALOR_SONORO\n5 - SILABICA_COM_VALOR_SONORO\n6 - SILABICA_ALFABETICA\n7 - ALFABETICA\n8 - ORTOGRAFICA");
                        int nivelLeitura = leitor.nextInt();
                        leitor.nextLine();

                        if (nivelLeitura >= 1 && nivelLeitura <= 8) {
                            nivelSelecionado = NivelLeitura.values()[nivelLeitura - 1];
                            System.out.println("Você escolheu: " + nivelSelecionado);
                        } else {
                            System.out.println("Opção inválida!");
                        }
                    }

                    boolean temNecessidadeEspecial = false;
                    String descricaoNecessidade = "";
                    int opNecessidade = 0;
                    while (opNecessidade != 1 && opNecessidade != 2) {
                        System.out.println("O(a) aluno(a) possui necessidade especial?\n1 - SIM\n2 - NÃO");
                        opNecessidade = leitor.nextInt();
                        leitor.nextLine();

                        if (opNecessidade == 1) {
                            temNecessidadeEspecial = true;
                            System.out.print("Digite laudo/descrição: ");
                            descricaoNecessidade = leitor.nextLine();
                        }
                    }

                    Aluno dadosAluno = new Aluno(nomeAluno, dataNascimento, anoAluno, responsavelAluno,
                            nivelSelecionado, temNecessidadeEspecial, descricaoNecessidade);
                    ListaAlunos.add(dadosAluno);
                    GerenciadorArquivo.salvarAlunos(ListaAlunos);
                    System.out.println("✅ Aluno cadastrado com sucesso!");
                    break;

                case 2:
                    System.out.println("\n=== ENCONTRAR ALUNO ===");
                    System.out.print("Digite o nome do aluno que busca: ");
                    String buscaNome = leitor.nextLine();
                    boolean encontrado = false;
                    for (Aluno a : ListaAlunos) {
                        if (a.getNome().equalsIgnoreCase(buscaNome)) {
                            a.exibirDadosResumidos();
                            encontrado = true;
                        }
                    }
                    if (!encontrado)
                        System.out.println("❌ Aluno não encontrado.");
                    break;

                case 3:
                    System.out.println("\n=== REMOVER ALUNO ===");
                    System.out.print("Digite o nome do aluno que deseja remover: ");
                    String nomeParaRemover = leitor.nextLine();
                    removerAluno(ListaAlunos, nomeParaRemover, leitor);
                    GerenciadorArquivo.salvarAlunos(ListaAlunos);
                    break;

                case 4:
                    System.out.println("\n--- RELATÓRIO RÁPIDO DOS ALUNOS CADASTRADOS ---");
                    if (ListaAlunos.isEmpty()) {
                        System.out.println("Nenhum aluno cadastrado.");
                    } else {
                        for (Aluno aluno : ListaAlunos) {
                            aluno.exibirDadosResumidos();
                        }
                    }
                    break;

                case 5:
                    System.out.println("Até logo, Lidiane! Salvando dados e encerrando...");
                    break;

                default:
                    System.out.println("Opção inválida! Escolha de 1 a 5.");
            }
        }

        leitor.close();
        GerenciadorArquivo.salvarAlunos(ListaAlunos);
    } // Fim do método main

    // RESTRUTURADOS ABAIXO DO MAIN:
    public static boolean alunoJaExiste(ArrayList<Aluno> lista, String nome, LocalDate dataNasc) {
        for (Aluno aluno : lista) {
            if (aluno.getNome().equalsIgnoreCase(nome) && aluno.getDataNascimento().equals(dataNasc)) {
                return true;
            }
        }
        return false;
    }

    public static void removerAluno(ArrayList<Aluno> lista, String nome, Scanner leitor) {
    // 1. Procura se o aluno existe na lista antes de tentar deletar
    Aluno alunoEncontrado = null;
    for (Aluno aluno : lista) {
        if (aluno.getNome().equalsIgnoreCase(nome)) {
            alunoEncontrado = aluno;
            break;
        }
    }

    if (alunoEncontrado == null) {
        System.out.println("❌ Aluno não encontrado.");
        return;
    }

    // 2. Se encontrou, pede a confirmação de segurança
    System.out.println("\n⚠️ ATENÇÃO: Você tem certeza que deseja remover o(a) aluno(a): " + alunoEncontrado.getNome() + "?");
    System.out.println("1 - SIM\n2 - NÃO");
    System.out.print("Escolha uma opção: ");
    
    int conf = 0;
    try {
        conf = Integer.parseInt(leitor.nextLine());
    } catch (NumberFormatException e) {
        // Se digitar letra, assume como não por segurança
    }

    if (conf == 1) {
        lista.remove(alunoEncontrado);
        System.out.println("🗑️ Aluno removido da lista com sucesso!");
    } else {
        System.out.println("❌ Operação cancelada. O aluno não foi removido.");
    }
}
} // Fim da classe CadAluno