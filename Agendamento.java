import java.time.LocalDate;
import java.util.List;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Agendamento {
    private String id;
    private String matriculaAluno;
    private LocalDate data;
    private String hora;
    private boolean pago;
    private String observacao;

    public Agendamento(String id, String matriculaAluno, LocalDate data, String hora, boolean pago, String observacao) {
        this.id = id;
        this.matriculaAluno = matriculaAluno;
        this.data = data;
        this.hora = hora;
        this.pago = pago;
        this.observacao = observacao;
    }

    public String getId() {
        return id;
    }

    public String getMatriculaAluno() {
        return matriculaAluno;
    }

    public LocalDate getData() {
        return data;
    }

    public String getHora() {
        return hora;
    }

    public boolean isPago() {
        return pago;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMatriculaAluno(String matriculaAluno) {
        this.matriculaAluno = matriculaAluno;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

}
