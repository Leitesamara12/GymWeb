package br.com.gymweb.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload para criação e edição de um treino completo.
 */
public class TreinoDTO {

    private String nome;
    private String descricao;
    private Long alunoId;
    private Long instrutorId;
    private List<ExercicioTreinoDTO> exercicios = new ArrayList<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Long getInstrutorId() {
        return instrutorId;
    }

    public void setInstrutorId(Long instrutorId) {
        this.instrutorId = instrutorId;
    }

    public List<ExercicioTreinoDTO> getExercicios() {
        return exercicios;
    }

    public void setExercicios(List<ExercicioTreinoDTO> exercicios) {
        this.exercicios = exercicios;
    }
}
