import java.time.LocalDate;

public class Aluno {

    private String nome;
    private LocalDate dataNascimento;
    private String anoEscolar;
    private Responsavel responsavel;
    private NivelLeitura nivelLeitura;
    private boolean temNecessidadeEspecial;
    private String descricaoNecessidade;
    private StatusPagamento statusPagamento;

    public Aluno() {}

    public Aluno(String nome, LocalDate dataNascimento, String anoEscolar, Responsavel responsavel, NivelLeitura nivelLeitura, boolean temNecessidadeEspecial, String descricaoNecessidade, StatusPagamento statusPagamento) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.anoEscolar = anoEscolar;
        this.responsavel = responsavel;
        this.nivelLeitura = nivelLeitura;
        this.temNecessidadeEspecial = temNecessidadeEspecial;
        this.descricaoNecessidade = descricaoNecessidade;
        this.statusPagamento = statusPagamento;
    }


    // Getters e Setters essenciais
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getAnoEscolar() { return anoEscolar; }
    public void setAnoEscolar(String anoEscolar) { this.anoEscolar = anoEscolar; }

    public Responsavel getResponsavel() { return responsavel; }
    public void setResponsavel(Responsavel responsavel) { this.responsavel = responsavel; }

    public NivelLeitura getNivelLeitura() { return nivelLeitura; }
    public void setNivelLeitura(NivelLeitura nivelLeitura) { this.nivelLeitura = nivelLeitura; }

    public boolean isTemNecessidadeEspecial() { return temNecessidadeEspecial; }
    public void setTemNecessidadeEspecial(boolean temNecessidadeEspecial) { this.temNecessidadeEspecial = temNecessidadeEspecial; }

    public String getDescricaoNecessidade() { return descricaoNecessidade; }
    public void setDescricaoNecessidade(String descricaoNecessidade) { this.descricaoNecessidade = descricaoNecessidade; }

     public StatusPagamento getStatusPagamento() { return statusPagamento; }

    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }

    public void exibirDadosResumidos() {
        System.out.println("Aluno: " + nome + " | Ano: " + anoEscolar + " | Nível: " + nivelLeitura + " | Status: " + statusPagamento.getDescricao());
    }
}