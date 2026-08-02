package br.com.gymweb.dto;

import java.util.List;

public record TreinoResponseDTO(
        Long id,
        String nome,
        String descricao,
        UsuarioResumoDTO aluno,
        UsuarioResumoDTO instrutor,
        List<ExercicioTreinoResponseDTO> exercicios
) {
}
