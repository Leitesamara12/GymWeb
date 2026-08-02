package br.com.gymweb.dto;

public record ExercicioTreinoResponseDTO(
        Long id,
        ExercicioResumoDTO exercicio,
        Integer ordem,
        Integer series,
        Integer repeticoes,
        Double carga,
        Boolean concluido
) {
}
