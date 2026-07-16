public class Aluno {
    private String nome;
    private String idade;
    private String anoEscolar;
    private Responsavel responsavel;
    private NivelLeitura nivelLeitura;
    private boolean temNecessidadeEspecial;
    private String descricaoNecessidade;

    // Construtor
    public Aluno(String nome, String idade, String anoEscolar, Responsavel responsavel, 
                 NivelLeitura nivelLeitura, boolean temNecessidadeEspecial, String descricaoNecessidade) {
        this.nome = nome;
        this.idade = idade;
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
    public String getIdade() { 
        return idade; 
    }
     public void setIdade(String idade) {
        this.idade = idade;
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
}