import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Aluno implements RelatorioEmitivel {
    private String nome;
    private LocalDate dataNascimento;
    private String anoEscolar;
    private String matricula;
    private static int contadorSequencial = 1;
    private Responsavel responsavel;
    private NivelLeitura nivelLeitura;
    private boolean temNecessidadeEspecial;
    private String descricaoNecessidade;

    // Construtor
    public Aluno(String nome, LocalDate dataNascimento, String anoEscolar, Responsavel responsavel,
            NivelLeitura nivelLeitura, boolean temNecessidadeEspecial, String descricaoNecessidade) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.anoEscolar = anoEscolar;
        this.responsavel = responsavel;
        this.nivelLeitura = nivelLeitura;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
        this.descricaoNecessidade = descricaoNecessidade;
        this.matricula = gerarMatricula(dataNascimento); // Para gerar a matrícula com base na data de nascimento
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    // Segundo construtor para o banco de dados em arquivo
    public Aluno(String nome, LocalDate dataNascimento, String anoEscolar) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.anoEscolar = anoEscolar;
        this.matricula = gerarMatricula(dataNascimento); // Garante que gera a matrícula sequencial!

        // Inicializa os outros campos vazios/padrão por enquanto
        this.responsavel = null;
        this.nivelLeitura = null;
        this.temNecessidadeEspecial = false;
        this.descricaoNecessidade = "";
    }

    // === GETTERS (Para buscar as informações) ===
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // O "getIdade" agora calcula dinamicamente, não precisa de setter!
    public int getIdade() {
        return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    // Getter para a data de nascimento (caso o Main precise ler a data bruta)
    public LocalDate getDataNascimento() {
        return this.dataNascimento;
    }

    // Getter para a matrícula (o Main vai usar para exibir a matrícula gerada)
    public String getMatricula() {
        return this.matricula;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
        this.matricula = gerarMatricula(dataNascimento);
    }

    private String gerarMatricula(LocalDate data) {
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("ddMMyyyy");
        String dataFormatada = data.format(formatador);

        // Formata o número com 3 dígitos (ex: 1 vira "001")
        String sufixo = String.format("%03d", contadorSequencial);

        // Incrementa o contador global para o próximo aluno
        contadorSequencial++;

        return dataFormatada + sufixo;
    }

    public String getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(String anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public Responsavel getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Responsavel responsavel) {
        this.responsavel = responsavel;
    }

    public NivelLeitura getNivelLeitura() {
        return nivelLeitura;
    }

    public void setNivelLeitura(NivelLeitura nivelLeitura) {
        this.nivelLeitura = nivelLeitura;
    }

    public boolean isTemNecessidadeEspecial() {
        return temNecessidadeEspecial;
    }

    public void setTemNecessidadeEspecial(boolean temNecessidadeEspecial) {
        this.temNecessidadeEspecial = temNecessidadeEspecial;
    }

    public String getDescricaoNecessidade() {
        return descricaoNecessidade;
    }

    public void setDescricaoNecessidade(String descricaoNecessidade) {
        this.descricaoNecessidade = descricaoNecessidade;
    }

    @Override
    public void exibirDadosResumidos() {
        // Aqui dentro você coloca o System.out.println para mostrar os dados básicos do
        // aluno
        System.out.println("Matrícula: " + this.getMatricula() + " - Aluno: " + this.nome + " - Idade: "
                + this.getIdade() + " anos" + " - Responsável: " + this.responsavel.nome() +
                " - Contato: " + this.responsavel.telefone());
    }
}