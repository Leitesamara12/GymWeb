package br.com.gymweb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aluno")
@Getter
@Setter
@NoArgsConstructor
public class Aluno extends UsuarioBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrutor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Instrutor instrutor;

    public Aluno(String nome, String cpf, String senha) {
        super(null, nome, cpf, senha, "ALUNO");
    }

    @PrePersist
    protected void definirCargoAluno() {
        if (getCargo() == null || getCargo().isBlank()) {
            setCargo("ALUNO");
        }
    }
}
