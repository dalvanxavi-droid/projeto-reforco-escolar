public record Responsavel (String nome, String telefone, String endereco) implements RelatorioEmitivel  {
    @Override
    public void exibirDadosResumidos() {
        System.out.println("Responsável: " + this.nome);
    }
}
