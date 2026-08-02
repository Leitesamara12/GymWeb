package br.com.gymweb.dto;

/**
 * DTO de SAÍDA: nunca inclui a senha ou seu hash.
 */
public class LoginResponseDTO {
    private Long id;
    private String nome;
    private String cargo; // "DONO", "INSTRUTOR" ou "ALUNO"

    public LoginResponseDTO(Long id, String nome, String cargo) {
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCargo() {
        return cargo;
    }
}
