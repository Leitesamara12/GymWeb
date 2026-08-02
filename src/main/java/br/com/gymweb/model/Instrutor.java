package br.com.gymweb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "instrutor")
@Getter
@Setter
@NoArgsConstructor
public class Instrutor extends UsuarioBase {

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Size(max = 50)
    @Column(length = 50)
    private String especialidade;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    public Instrutor(
            String nome,
            String cpf,
            String senha,
            String email,
            String telefone,
            String especialidade,
            String cargo
    ) {
        super(null, nome, cpf, senha, (cargo == null || cargo.isBlank()) ? "INSTRUTOR" : cargo);
        this.email = email;
        this.telefone = telefone;
        this.especialidade = especialidade;
    }

    @PrePersist
    protected void onCreate() {
        if (getCargo() == null || getCargo().isBlank()) {
            setCargo("INSTRUTOR");
        }
        LocalDateTime agora = LocalDateTime.now();
        this.dataCadastro = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
