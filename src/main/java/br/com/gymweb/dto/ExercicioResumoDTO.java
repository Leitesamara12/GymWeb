package br.com.gymweb.dto;

public record ExercicioResumoDTO(
        Long id,
        String nome,
        String descricao,
        String grupoMuscular
) {
}
