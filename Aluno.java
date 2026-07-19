import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Aluno implements RelatorioEmitivel {
    private String nome;
    private LocalDate dataNascimento;
    private String anoEscolar;
    private String matricula;
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
    return data.format(formatador);
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
        System.out.println(("Aluno: " + this.nome + " - Idade: " + getIdade() + " anos" + " - Ano Escolar: "
                + this.anoEscolar + " - Responsável: " + this.responsavel.nome()));
    }
}