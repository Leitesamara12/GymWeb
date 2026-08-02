package br.com.gymweb.repository;

import br.com.gymweb.model.ExercicioTreino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso aos exercícios que compõem cada treino. */
public interface ExercicioTreinoRepository extends JpaRepository<ExercicioTreino, Long> {

    List<ExercicioTreino> findByTreinoIdOrderByOrdemAsc(Long treinoId);
}
