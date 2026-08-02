package br.com.gymweb.dto;

/**
 * DTO de ENTRADA: JSON enviado pelo front-end (auth.js) na tela de login.
 * Exemplo de body: { "cpf": "12345678", "senha": "admin" } — os valores
 * aqui são apenas ilustrativos do contrato da API, não credenciais reais.
 */
public class LoginDTO {
    private String cpf;
    private String senha;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
