public enum StatusPagamento {
    EM_DIA("✅ Em dia"),
    PENDENTE("⏳ Pendente (no prazo)"),
    ATRASADO("❌ Atrasado");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}